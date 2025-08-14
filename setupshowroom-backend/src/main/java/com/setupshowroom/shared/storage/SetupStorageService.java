package com.setupshowroom.shared.storage;

import com.github.f4b6a3.ulid.UlidCreator;
import com.setupshowroom.shared.storage.common.StoragePath;
import com.setupshowroom.shared.storage.s3.S3StorageService;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SetupStorageService {
  public static final String VIDEO_PATH = "videos";
  public static final String IMAGE_PATH = "images";

  private final @NotNull S3StorageService storageService;

  @SneakyThrows
  public List<String> writeContents(
      final @NotNull String userId,
      final @NotNull String setupId,
      final @NotNull List<MultipartFile> contents,
      final @NotNull String folder) {
    if (contents == null || contents.isEmpty()) {
      return List.of();
    }

    final List<String> contentUrls = new ArrayList<>();

    for (final MultipartFile content : contents) {
      final String fileExtension =
          Objects.requireNonNull(content.getOriginalFilename())
              .substring(content.getOriginalFilename().lastIndexOf('.'));
      final String relativePath =
          Paths.get(setupId, folder, UlidCreator.getUlid().toString() + fileExtension).toString();
      final StoragePath sp = StoragePath.of(userId, relativePath);

      final String url =
          this.storageService.write(
              sp,
              content.getInputStream(),
              content.getSize(),
              Objects.requireNonNull(content.getContentType()));
      contentUrls.add(url);
    }

    return contentUrls;
  }

  @SneakyThrows
  public void deleteSetup(final @NotNull String userId, final @NotNull String setupId) {
    this.storageService.softDeleteDirectory(StoragePath.of(userId, setupId));
  }

  @SneakyThrows
  public void deleteContent(
      final @NotNull String userId,
      final @NotNull String setupId,
      final @NotNull String contentName,
      final @NotNull String contentFolder) {
    final StoragePath sp =
        StoragePath.of(userId, Paths.get(setupId, contentFolder, contentName).toString());
    this.storageService.hardDelete(sp);
  }
}
