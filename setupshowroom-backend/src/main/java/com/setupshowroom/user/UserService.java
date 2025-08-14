package com.setupshowroom.user;

import static com.setupshowroom.shared.storage.SetupStorageService.IMAGE_PATH;

import com.github.f4b6a3.ulid.UlidCreator;
import com.setupshowroom.product.FavoriteProduct;
import com.setupshowroom.product.FavoriteProductGroup;
import com.setupshowroom.product.FavoriteProductGroupRepository;
import com.setupshowroom.product.FavoriteProductRepository;
import com.setupshowroom.product.converter.FavoriteProductConverter;
import com.setupshowroom.product.dto.FavoriteProductForm;
import com.setupshowroom.product.dto.FavoriteProductGroupForm;
import com.setupshowroom.product.dto.FavoriteProductGroupInfo;
import com.setupshowroom.product.dto.FavoriteProductInfo;
import com.setupshowroom.setup.SetupCategory;
import com.setupshowroom.shared.contentmoderation.ContentModerationService;
import com.setupshowroom.shared.exceptionhandler.exception.EmailCannotChangeException;
import com.setupshowroom.shared.exceptionhandler.exception.ItemNotFoundException;
import com.setupshowroom.shared.exceptionhandler.exception.UsernameAlreadyExistsException;
import com.setupshowroom.shared.storage.SetupStorageService;
import com.setupshowroom.systeminfo.SystemRequirementRepository;
import com.setupshowroom.systeminfo.converter.SystemInfoConverter;
import com.setupshowroom.systeminfo.dto.SystemInfo;
import com.setupshowroom.systeminfo.dto.SystemRequirementForm;
import com.setupshowroom.user.converter.UserConverter;
import com.setupshowroom.user.dto.PasswordChangeForm;
import com.setupshowroom.user.dto.UserForm;
import com.setupshowroom.user.dto.UserInfo;
import com.setupshowroom.user.profile.UserProfile;
import com.setupshowroom.user.profile.converter.ProfileConverter;
import com.setupshowroom.user.profile.dto.ProfileInfo;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
  private final @NotNull SystemInfoConverter systemInfoConverter;
  private final @NotNull FavoriteProductConverter favoriteProductConverter;
  private final @NotNull UserConverter userConverter;
  private final @NotNull ProfileConverter profileConverter;
  private final @NotNull UserRepository userRepository;
  private final @NotNull FavoriteProductGroupRepository favoriteProductGroupRepository;
  private final @NotNull FavoriteProductRepository favoriteProductRepository;
  private final @NotNull SystemRequirementRepository systemRequirementRepository;
  private final @NotNull SetupStorageService setupStorageService;
  private final @NotNull ContentModerationService contentModerationService;

  public @NotNull UserInfo update(
      final @NotNull UserForm updateForm, final @NotNull String userId) {
    final User user = this.findById(userId);

    user.setProfession(updateForm.getProfession());

    if (!user.getEmail().equals(updateForm.getEmail())) {
      if (user.getProviderId() != null) {
        throw new EmailCannotChangeException("emailCannotChange");
      }
      if (this.userRepository.existsByEmailAndLockedFalseAndEnabledTrue(updateForm.getEmail())) {
        user.setEmail(updateForm.getEmail());
      } else {
        throw new UsernameAlreadyExistsException("emailExists");
      }
    }

    if (!user.getUsername().equals(updateForm.getUsername())) {
      if (!this.userRepository.existsByUsernameAndLockedFalseAndEnabledTrue(
          updateForm.getUsername())) {
        user.setUsername(updateForm.getUsername());
      } else {
        throw new UsernameAlreadyExistsException("usernameExists");
      }
    }

    user.setFullName(updateForm.getFullName());

    return this.userConverter.toUserInfo(this.userRepository.save(user));
  }

  public void deactivateAccount(final @NotNull String userId) {
    final User user = this.findById(userId);

    user.setEnabled(false);

    this.userRepository.save(user);
  }

  public @NotNull UserInfo getById(final @NotNull String userId) {
    return this.userConverter.toUserInfo(this.findById(userId));
  }

  @SuppressWarnings("unused")
  public void sendVerificationEmail(final @NotNull String userId) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public boolean verifyEmail(final @NotNull String userId, final @NotNull String code) {
    final User user = this.findById(userId);

    if (user.getEmailVerifiedCode() == null) {
      return true;
    }

    if (user.getEmailVerifiedCode().equals(code)) {
      user.setEmailVerifiedCode(null);
      this.userRepository.save(user);
      return true;
    }

    return false;
  }

  public @NotNull User findById(final @NotNull String userId) {
    return this.userRepository
        .findUserById(userId)
        .orElseThrow(() -> new ItemNotFoundException("userNotFound"));
  }

  public @NotNull User findByUsername(final @NotNull String username) {
    return this.userRepository
        .findByUsernameOrEmail(username)
        .orElseThrow(() -> new ItemNotFoundException("userNotFound"));
  }

  public @NotNull ProfileInfo findUserProfile(final @NotNull String userId) {
    final User user = this.findById(userId);

    final UserProfile profile = user.getUserProfile();

    final var productGroups =
        this.favoriteProductGroupRepository.findAllByUserIdAndDeletedFalse(userId).stream()
            .map(group -> this.toFavoriteProductGroupInfo(group, userId))
            .toList();

    final var systemInfo = this.systemInfoConverter.toSystemInfo(user.getSystemRequirement());

    return this.profileConverter.toProfileInfo(profile, productGroups, systemInfo, user);
  }

  public @NotNull ProfileInfo findUserProfileByUsername(final @NotNull String username) {
    final User user = this.findByUsername(username);

    final UserProfile profile = user.getUserProfile();

    final var productGroups =
        this.favoriteProductGroupRepository.findAllByUserIdAndDeletedFalse(user.getId()).stream()
            .map(group -> this.toFavoriteProductGroupInfo(group, user.getId()))
            .toList();

    final var systemInfo = this.systemInfoConverter.toSystemInfo(user.getSystemRequirement());

    return this.profileConverter.toProfileInfo(profile, productGroups, systemInfo, user);
  }

  private @NotNull FavoriteProductGroupInfo toFavoriteProductGroupInfo(
      final @NotNull FavoriteProductGroup group, final @NotNull String userId) {
    final SortedSet<FavoriteProduct> products =
        this.favoriteProductRepository
            .findAllByFavoriteProductGroupIdAndFavoriteProductGroupUserIdAndDeletedFalse(
                group.getId(), userId);

    final var productInfoList =
        products.stream().map(this.favoriteProductConverter::toFavoriteProductInfo).toList();

    return this.favoriteProductConverter.toFavoriteProductGroupInfo(group, productInfoList);
  }

  public ProfileInfo upsertSystemRequirement(
      final @NotNull SystemRequirementForm form,
      final @NotNull List<MultipartFile> images,
      final @NotNull String userId) {
    User user = this.findById(userId);

    final String sysInfoId =
        user.getSystemRequirement() != null
            ? user.getSystemRequirement().getId()
            : UlidCreator.getUlid().toString();

    final SortedSet<String> currentImages =
        user.getSystemRequirement() != null
            ? user.getSystemRequirement().getImages()
            : new TreeSet<>();

    if (images != null && !images.isEmpty()) {
      this.contentModerationService.checkContentValidity(user, images, List.of());
    }

    final SortedSet<String> newImageUrls =
        (images != null && !images.isEmpty())
            ? new TreeSet<>(
                this.setupStorageService.writeContents(user.getId(), sysInfoId, images, IMAGE_PATH))
            : new TreeSet<>();

    final SortedSet<String> finalImages =
        Stream.concat(form.getExistingImages().stream(), newImageUrls.stream())
            .collect(Collectors.toCollection(TreeSet::new));

    // Delete unused images
    final SortedSet<String> removedImages = new TreeSet<>(currentImages);
    removedImages.removeAll(finalImages);

    for (final var imageContent : removedImages) {
      final String fileName = imageContent.substring(imageContent.lastIndexOf('/') + 1);
      this.setupStorageService.deleteContent(user.getId(), sysInfoId, fileName, IMAGE_PATH);
    }

    final var systemRequirement = this.systemInfoConverter.toSystemRequirement(form);
    systemRequirement.setImages(finalImages);

    final SortedSet<SetupCategory> categories =
        form.getCategories().stream()
            .map(SetupCategory::valueOf)
            .collect(Collectors.toCollection(TreeSet::new));
    systemRequirement.setCategories(categories);

    if (user.getSystemRequirement() != null) {
      final var currentSysInfo = user.getSystemRequirement();
      systemRequirement.setId(currentSysInfo.getId());
      systemRequirement.setDeleted(currentSysInfo.isDeleted());
      systemRequirement.setUser(user);
    } else {
      systemRequirement.setId(sysInfoId);
      systemRequirement.setUser(user);
    }

    final var savedSysInfo = this.systemRequirementRepository.save(systemRequirement);
    user.setSystemRequirement(savedSysInfo);
    user = this.userRepository.save(user);

    final var productGroups =
        this.favoriteProductGroupRepository.findAllByUserIdAndDeletedFalse(userId).stream()
            .map(group -> this.toFavoriteProductGroupInfo(group, userId))
            .toList();

    final SystemInfo systemInfo = this.systemInfoConverter.toSystemInfo(systemRequirement);
    log.warn("User: {} updated system info", user.getUsername());
    return this.profileConverter.toProfileInfo(
        user.getUserProfile(), productGroups, systemInfo, user);
  }

  public @NotNull FavoriteProductGroupInfo createProductGroup(
      final @NotNull FavoriteProductGroupForm form, final @NotNull String userId) {
    final User user = this.findById(userId);
    final var groupOpt =
        this.favoriteProductGroupRepository.findByUserIdAndNameAndDeletedFalse(
            user.getId(), form.getName());

    if (groupOpt.isPresent()) {
      throw new IllegalArgumentException("groupNameAlreadyExists");
    }

    final var group =
        FavoriteProductGroup.builder()
            .id(UlidCreator.getUlid().toString())
            .name(form.getName())
            .user(user)
            .build();

    final var savedGroup = this.favoriteProductGroupRepository.save(group);

    return this.toFavoriteProductGroupInfo(savedGroup, userId);
  }

  public @NotNull FavoriteProductInfo createProduct(
      final @NotNull FavoriteProductForm form,
      final @NotNull String userId,
      final @NotNull String groupId) {
    final FavoriteProductGroup productGroup =
        this.favoriteProductGroupRepository
            .findByUserIdAndIdAndDeletedFalse(userId, groupId)
            .orElseThrow(() -> new ItemNotFoundException("productGroupNotFound"));

    final FavoriteProduct product = this.favoriteProductConverter.toFavoriteProduct(form);
    product.setFavoriteProductGroup(productGroup);

    final var savedProduct = this.favoriteProductRepository.save(product);

    final var productInfo = this.favoriteProductConverter.toFavoriteProductInfo(savedProduct);
    productInfo.setGroupId(groupId);

    return productInfo;
  }

  public void deleteProduct(
      final @NotNull String userId,
      final @NotNull String productId,
      final @NotNull String groupId) {
    final FavoriteProduct product =
        this.favoriteProductRepository
            .findByIdAndFavoriteProductGroupIdAndFavoriteProductGroupUserId(
                productId, groupId, userId)
            .orElseThrow(() -> new ItemNotFoundException("productNotFound"));

    product.setDeleted(true);
    this.favoriteProductRepository.save(product);
  }

  public void deleteProductGroup(final @NotNull String userId, final @NotNull String groupId) {
    final FavoriteProductGroup group =
        this.favoriteProductGroupRepository
            .findByUserIdAndIdAndDeletedFalse(userId, groupId)
            .orElseThrow(() -> new ItemNotFoundException("productGroupNotFound"));

    group.getProducts().forEach(product -> product.setDeleted(true));
    this.favoriteProductRepository.saveAll(group.getProducts());

    group.setDeleted(true);
    this.favoriteProductGroupRepository.save(group);
  }

  public void resetPassword(final @NotNull String userId, final @NotNull PasswordChangeForm form) {
    final var user =
        this.userRepository
            .findUserById(userId)
            .orElseThrow(() -> new ItemNotFoundException("userNotFound"));

    final var salt = RandomStringUtils.secure().nextAlphanumeric(16);
    final var password = DigestUtils.sha256Hex(salt + form.getNewPassword());

    user.setSalt(salt);
    user.setHashedPassword(password);
    user.getEmbeddedTimestamps().setUpdatedAt(Instant.now());

    this.userRepository.save(user);
  }

  public FavoriteProductInfo updateProduct(
      final @NotNull FavoriteProductForm form,
      final @NotNull String userId,
      final @NotNull String groupId,
      final @NotNull String productId) {
    final var user = this.findById(userId);
    final var product =
        this.favoriteProductRepository
            .findByIdAndFavoriteProductGroupIdAndFavoriteProductGroupUserId(
                productId, groupId, user.getId())
            .orElseThrow(() -> new ItemNotFoundException("productNotFound"));

    product.setName(form.getName());
    product.setUrl(form.getUrl());

    final var savedProduct = this.favoriteProductRepository.save(product);

    return this.favoriteProductConverter.toFavoriteProductInfo(savedProduct);
  }

  public void updateProductGroup(
      final @NotNull FavoriteProductGroupForm form,
      final @NotNull String userId,
      final @NotNull String groupId) {
    final var user = this.findById(userId);

    final var group =
        this.favoriteProductGroupRepository
            .findByUserIdAndIdAndDeletedFalse(user.getId(), groupId)
            .orElseThrow(() -> new ItemNotFoundException("productGroupNotFound"));

    group.setName(form.getName());

    this.favoriteProductGroupRepository.save(group);
  }
}
