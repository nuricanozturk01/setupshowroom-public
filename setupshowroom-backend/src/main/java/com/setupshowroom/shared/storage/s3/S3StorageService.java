package com.setupshowroom.shared.storage.s3;

import com.google.common.collect.Lists;
import com.setupshowroom.shared.storage.common.StoragePath;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

@Slf4j
public class S3StorageService {
  private final @NotNull S3Client client;
  private final @NotNull String basePath;
  private final @NotNull String trashPath;
  private final @NotNull String bucket;
  private final URI endpoint;
  private static final int MAX_OBJECTS_TO_DELETE = 1_000;

  public S3StorageService(final @NotNull S3StorageBackendConfigProps config) {
    this.bucket = config.getBucket();
    this.basePath = config.getBasePath();
    this.trashPath = config.getTrashPath();
    this.client =
        S3Client.builder()
            .forcePathStyle(true)
            .region(Region.US_EAST_1)
            .endpointOverride(URI.create(config.getEndpoint()))
            .credentialsProvider(() -> new CustomAwsCredentials(config))
            .build();
    this.endpoint = URI.create(config.getEndpoint());
  }

  @SneakyThrows
  public String write(
      final @NotNull StoragePath storagePath,
      final @NotNull InputStream inputStream,
      final long contentLength,
      final @NotNull String contentType) {
    final String key = this.basePath + "/" + storagePath.getPath();

    final PutObjectRequest putRequest =
        PutObjectRequest.builder()
            .bucket(this.bucket)
            .key(key)
            .contentLength(contentLength)
            .contentType(contentType)
            .build();

    this.client.putObject(putRequest, RequestBody.fromInputStream(inputStream, contentLength));

    return this.resolveResourceUrl(storagePath);
  }

  private @NotNull String resolveResourceUrl(final @NotNull StoragePath storagePath) {
    final String key = this.basePath + "/" + storagePath.getPath();

    final String baseUrl = this.endpoint.toString();

    return String.format("%s/%s/%s", baseUrl, this.bucket, key);
  }

  public void softDeleteDirectory(final @NotNull StoragePath storagePath) {
    final String baseDir = this.resolveDir(this.basePath, storagePath.getPath());
    final String relativePath = String.join("/", storagePath.getPath());
    final String trashDir = this.resolveDir(this.trashPath, relativePath);

    final List<S3Object> allObjects = this.getS3Objects(storagePath);

    // Copy all objects to trash path
    allObjects.parallelStream()
        .forEach(
            s3Object -> {
              final String sourceKey = s3Object.key();
              final String destinationKey = sourceKey.replaceFirst(baseDir, trashDir);

              final CopyObjectRequest copyRequest =
                  CopyObjectRequest.builder()
                      .sourceBucket(this.bucket)
                      .sourceKey(sourceKey)
                      .destinationBucket(this.bucket)
                      .destinationKey(destinationKey)
                      .build();

              this.client.copyObject(copyRequest);
            });

    // Then, remove all of them from repo.

    this.deleteObjects(allObjects);
  }

  public void hardDelete(final @NotNull StoragePath storagePath) {
    final String resourcePath = String.join("/", this.basePath, storagePath.getPath());

    final DeleteObjectRequest deleteRequest =
        DeleteObjectRequest.builder().bucket(this.bucket).key(resourcePath).build();

    this.client.deleteObject(deleteRequest);
  }

  private @NotNull String resolveDir(final @NotNull String path, final @NotNull String dir) {
    return path + "/" + dir + "/";
  }

  private void deleteObjects(final @NotNull List<S3Object> allObjects) {
    final List<ObjectIdentifier> objectsToDelete = this.getObjectsToDelete(allObjects);

    if (objectsToDelete.isEmpty()) {
      return;
    }

    Lists.partition(objectsToDelete, MAX_OBJECTS_TO_DELETE)
        .forEach(
            objectGroups -> {
              final DeleteObjectsRequest deleteRequest =
                  DeleteObjectsRequest.builder()
                      .bucket(this.bucket)
                      .delete(Delete.builder().objects(objectsToDelete).build())
                      .build();

              this.client.deleteObjects(deleteRequest);
            });
  }

  private @NotNull List<S3Object> getS3Objects(final @NotNull StoragePath storagePath) {
    final String baseDir = this.resolveDir(this.basePath, storagePath.getPath());

    final List<S3Object> allObjects = new ArrayList<>();
    final ListObjectsV2Request listRequest =
        ListObjectsV2Request.builder().bucket(this.bucket).prefix(baseDir).build();

    final ListObjectsV2Iterable responses = this.client.listObjectsV2Paginator(listRequest);
    responses.stream().map(ListObjectsV2Response::contents).forEach(allObjects::addAll);

    return allObjects;
  }

  private @NotNull List<ObjectIdentifier> getObjectsToDelete(
      final @NotNull List<S3Object> allObjects) {
    return allObjects.stream()
        .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
        .collect(Collectors.toList());
  }

  private record CustomAwsCredentials(@NotNull S3StorageBackendConfigProps config)
      implements AwsCredentials {
    @Override
    public String accessKeyId() {
      return this.config.getAccessKey();
    }

    @Override
    public String secretAccessKey() {
      return this.config.getSecretKey();
    }
  }
}
