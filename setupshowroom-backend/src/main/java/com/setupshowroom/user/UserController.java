package com.setupshowroom.user;

import com.setupshowroom.product.dto.FavoriteProductForm;
import com.setupshowroom.product.dto.FavoriteProductGroupForm;
import com.setupshowroom.product.dto.FavoriteProductGroupInfo;
import com.setupshowroom.product.dto.FavoriteProductInfo;
import com.setupshowroom.setup.SetupFacade;
import com.setupshowroom.setup.dto.SetupInfo;
import com.setupshowroom.shared.dto.Response;
import com.setupshowroom.systeminfo.dto.SystemRequirementForm;
import com.setupshowroom.user.dto.PasswordChangeForm;
import com.setupshowroom.user.dto.UserForm;
import com.setupshowroom.user.dto.UserInfo;
import com.setupshowroom.user.profile.dto.ProfileInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/user")
@RestController
@RequiredArgsConstructor
public class UserController {
  private final @NotNull UserService userService;
  private final @NotNull SetupFacade setupFacade;

  @GetMapping("/{userId}/setups/favorite")
  public @NotNull ResponseEntity<Response<List<SetupInfo>>> getAllFavorites(
      @PathVariable final @NotNull String userId, @PageableDefault final Pageable pageable) {
    final var setups = this.setupFacade.getSetupFavoriteSetups(userId, pageable);

    final Response<List<SetupInfo>> response =
        Response.success("retrieved", setups, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{userId}/reset-password")
  public @NotNull ResponseEntity<Response<Object>> resetPassword(
      @PathVariable final @NotNull String userId,
      @RequestBody final @NotNull PasswordChangeForm form) {
    this.userService.resetPassword(userId, form);
    return ResponseEntity.ok(Response.success("Password reset email sent successfully"));
  }

  @PutMapping("/{userId}")
  public @NotNull ResponseEntity<Response<UserInfo>> update(
      @RequestBody @Valid final @NotNull UserForm updateForm,
      @PathVariable final @NotNull String userId) {
    final UserInfo userInfo = this.userService.update(updateForm, userId);

    return ResponseEntity.ok(Response.success("updated", userInfo, HttpStatus.OK.value()));
  }

  @DeleteMapping("/{userId}")
  public @NotNull ResponseEntity<Void> deactivateAccount(
      @PathVariable final @NotNull String userId) {
    this.userService.deactivateAccount(userId);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{userId}")
  public @NotNull ResponseEntity<Response<UserInfo>> get(
      @PathVariable final @NotNull String userId) {
    final UserInfo userInfo = this.userService.getById(userId);

    final Response<UserInfo> response =
        Response.success("retrieved", userInfo, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{username}/profile/info")
  public @NotNull ResponseEntity<Response<ProfileInfo>> getUserByUsername(
      @PathVariable final @NotNull String username) {
    final var userInfo = this.userService.findUserProfileByUsername(username);

    final Response<ProfileInfo> response =
        Response.success("retrieved", userInfo, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{userId}/verify-email")
  public @NotNull ResponseEntity<Response<Object>> verifyEmail(
      @RequestParam final @NotNull String code, @PathVariable final @NotNull String userId) {
    final var verified = this.userService.verifyEmail(userId, code);

    if (verified) {
      return ResponseEntity.ok(Response.success("Email verified successfully"));
    }

    return ResponseEntity.badRequest()
        .body(Response.error("Invalid verification code", HttpStatus.BAD_REQUEST.value()));
  }

  @PostMapping("/{userId}/send-otp")
  public @NotNull ResponseEntity<Response<Object>> sendVerificationEmail(
      @PathVariable final @NotNull String userId) {
    this.userService.sendVerificationEmail(userId);
    return ResponseEntity.ok(Response.success("Email verified successfully"));
  }

  @GetMapping("/{userId}/profile")
  public @NotNull ResponseEntity<Response<ProfileInfo>> getUserProfile(
      @PathVariable final @NotNull String userId) {
    final ProfileInfo profileInfo = this.userService.findUserProfile(userId);

    final Response<ProfileInfo> response =
        Response.success("retrieved", profileInfo, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}/setups")
  public @NotNull ResponseEntity<Response<List<SetupInfo>>> getSetups(
      @PathVariable final @NotNull String userId, @PageableDefault final Pageable pageable) {
    final var user = this.userService.findById(userId);
    final var setups = this.setupFacade.findAllSetupsByUser(user.getId(), pageable);

    final Response<List<SetupInfo>> response =
        Response.success("retrieved", setups, HttpStatus.OK.value());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{userId}/profile/system-requirements")
  public @NotNull ResponseEntity<Response<ProfileInfo>> updateSystemRequirements(
      @RequestPart("system_specs")
          final @Valid @NotNull SystemRequirementForm systemRequirementForm,
      @RequestPart(value = "images", required = false) final List<MultipartFile> images,
      @PathVariable final @NotNull String userId) {
    final var profileInfo =
        this.userService.upsertSystemRequirement(systemRequirementForm, images, userId);

    return ResponseEntity.ok(
        Response.success(
            "System requirements updated successfully", profileInfo, HttpStatus.OK.value()));
  }

  @PostMapping("/{userId}/profile/favorite-products/group")
  public @NotNull ResponseEntity<Response<FavoriteProductGroupInfo>> createProductGroup(
      @RequestBody @Valid final @NotNull FavoriteProductGroupForm form,
      @PathVariable final @NotNull String userId) {
    final var productGroupInfo = this.userService.createProductGroup(form, userId);

    return ResponseEntity.ok(
        Response.success(
            "Product group created successfully", productGroupInfo, HttpStatus.OK.value()));
  }

  @DeleteMapping("/{userId}/profile/favorite-products/group/{groupId}")
  public @NotNull ResponseEntity<Response<Void>> deleteProductGroup(
      @PathVariable final @NotNull String userId, @PathVariable final @NotNull String groupId) {
    this.userService.deleteProductGroup(userId, groupId);

    return ResponseEntity.ok(
        Response.success(
            "Product group deleted successfully", null, HttpStatus.NO_CONTENT.value()));
  }

  @PostMapping("/{userId}/profile/favorite-products/group/{groupId}")
  public @NotNull ResponseEntity<Response<FavoriteProductInfo>> createProduct(
      @RequestBody @Valid final @NotNull FavoriteProductForm form,
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String groupId) {
    final var productGroupInfo = this.userService.createProduct(form, userId, groupId);

    return ResponseEntity.ok(
        Response.success("Product created successfully", productGroupInfo, HttpStatus.OK.value()));
  }

  @DeleteMapping("/{userId}/profile/favorite-products/group/{groupId}/product/{productId}")
  public @NotNull ResponseEntity<Response<Void>> createProductGroup(
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String groupId,
      @PathVariable final @NotNull String productId) {
    this.userService.deleteProduct(userId, productId, groupId);

    return ResponseEntity.ok(
        Response.success("Product deleted successfully", null, HttpStatus.NO_CONTENT.value()));
  }

  @PutMapping("/{userId}/profile/favorite-products/group/{groupId}/product/{productId}")
  public @NotNull ResponseEntity<Response<FavoriteProductInfo>> updateProduct(
      @RequestBody @Valid final @NotNull FavoriteProductForm form,
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String groupId,
      @PathVariable final @NotNull String productId) {
    final var productGroupInfo = this.userService.updateProduct(form, userId, groupId, productId);

    return ResponseEntity.ok(
        Response.success("Product updated successfully", productGroupInfo, HttpStatus.OK.value()));
  }

  @PutMapping("/{userId}/profile/favorite-products/group/{groupId}")
  public @NotNull ResponseEntity<Response<Void>> updateProductGroup(
      @RequestBody @Valid final @NotNull FavoriteProductGroupForm form,
      @PathVariable final @NotNull String userId,
      @PathVariable final @NotNull String groupId) {
    this.userService.updateProductGroup(form, userId, groupId);

    return ResponseEntity.ok(
        Response.success("Product group updated successfully", null, HttpStatus.OK.value()));
  }
}
