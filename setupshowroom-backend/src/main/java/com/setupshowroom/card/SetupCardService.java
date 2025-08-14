package com.setupshowroom.card;

import com.samskivert.mustache.Mustache;
import com.setupshowroom.setup.SetupCategory;
import com.setupshowroom.shared.exceptionhandler.exception.ItemNotFoundException;
import com.setupshowroom.systeminfo.SystemRequirement;
import com.setupshowroom.user.User;
import com.setupshowroom.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class SetupCardService {
  private static final String SVG_TEMPLATE_PATH = "templates/svg/%s.mustache";
  private static final String SETUP_OWNER_PLACEHOLDER = "%s's Setup";
  private static final int MIN_WIDTH = 350;
  private static final int MAX_TEXT_LENGTH = 30;
  private static final int TEXT_PREVIEW_LENGTH = 17;
  private static final int CATEGORY_NAME_MULTIPLIER = 6;
  private static final int CATEGORY_PADDING = 12;
  private static final int CATEGORY_SPACING = 8;
  private static final int CATEGORY_WIDTH_MULTIPLIER = 6;
  private static final int CATEGORY_WIDTH_PADDING = 20;
  private static final int TEXT_LENGTH_MULTIPLIER = 7;
  private static final int MAX_TEXT_WIDTH = 150;
  private final ResourceLoader resourceLoader;
  private final Mustache.Compiler compiler;
  private final UserRepository userRepository;

  @Value("${app.base-url}")
  private String baseUrl;

  public SetupCardService(
      final @NotNull ResourceLoader resourceLoader, final @NotNull UserRepository userRepository) {
    this.resourceLoader = resourceLoader;
    this.userRepository = userRepository;
    this.compiler = Mustache.compiler().defaultValue("").nullValue("").escapeHTML(false);
  }

  public String getUserSysInfoCard(final @NotNull String userId, final @NotNull String type) {
    final User user =
        this.userRepository
            .findUserById(userId)
            .orElseThrow(() -> new ItemNotFoundException("userNotFound"));

    return this.generateSvgFromTemplate(user, type);
  }

  @SneakyThrows
  private String generateSvgFromTemplate(final @NotNull User user, final @NotNull String type) {
    final SystemRequirement sysInfo = user.getSystemRequirement();
    final Resource resource =
        this.resourceLoader.getResource(
            String.format("classpath:%s", String.format(SVG_TEMPLATE_PATH, type)));
    final String template =
        new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    final Map<String, Object> context = new HashMap<>();

    final int contentWidth = this.calculateContentWidth(sysInfo, type);
    context.put("svgWidth", Math.max(MIN_WIDTH, contentWidth));

    final int rightColumnX = Math.max(300, contentWidth / 2);
    context.put("rightColumnX", rightColumnX);

    context.put(
        SystemComponents.SETUP_TITLE, String.format(SETUP_OWNER_PLACEHOLDER, user.getUsername()));
    this.addIfPresent(context, SystemComponents.CPU, sysInfo.getCpu());
    this.addIfPresent(context, SystemComponents.GPU, sysInfo.getGpu());
    this.addIfPresent(context, SystemComponents.RAM, sysInfo.getRam());
    this.addIfPresent(context, SystemComponents.STORAGE, sysInfo.getStorage());
    this.addIfPresent(context, SystemComponents.MOTHERBOARD, sysInfo.getMotherboard());
    this.addIfPresent(context, SystemComponents.PSU, sysInfo.getPsu());
    this.addIfPresent(context, SystemComponents.CASE, sysInfo.getSetupCase());
    this.addIfPresent(context, SystemComponents.MONITOR, sysInfo.getMonitor());
    this.addIfPresent(context, SystemComponents.SETUP_OWNER, user.getUsername());

    this.addCategoriesToContext(context, sysInfo.getCategories().stream().toList());
    context.putAll(this.getThemeColors(type.toLowerCase()));

    return this.compiler.compile(template).execute(context);
  }

  private void addCategoriesToContext(
      final Map<String, Object> context, final List<SetupCategory> categories) {
    final boolean hasCategories = categories != null && !categories.isEmpty();
    context.put("hasCategories", hasCategories);

    if (hasCategories) {
      final List<Map<String, Object>> formattedCategories = new ArrayList<>();
      int xOffset = 0;

      for (SetupCategory category : categories) {
        if (StringUtils.hasText(category.name())) {
          final Map<String, Object> categoryMap = new HashMap<>();
          categoryMap.put("name", category);
          categoryMap.put("xOffset", xOffset);

          final int width =
              (category.name().length() * CATEGORY_NAME_MULTIPLIER) + CATEGORY_PADDING;
          categoryMap.put("width", width);
          formattedCategories.add(categoryMap);
          xOffset += width + CATEGORY_SPACING;
        }
      }
      context.put("categories", formattedCategories);
    }
  }

  private void addIfPresent(
      final Map<String, Object> context, final String key, final String value) {
    if (StringUtils.hasText(value)) {
      String variable = value;
      if (value.length() > MAX_TEXT_LENGTH) {
        variable = value.substring(0, TEXT_PREVIEW_LENGTH) + "...";
      }
      context.put(key, variable);
    }
  }

  private Map<String, String> getThemeColors(final @NotNull String theme) {
    return switch (theme) {
      case SetupCardType.GAMING ->
          Map.of(
              "backgroundColor", "#0F172A",
              "primaryColor", "#22D3EE",
              "textColor", "#E2E8F0");
      case SetupCardType.ROOM ->
          Map.of(
              "backgroundColor", "#1E293B",
              "primaryColor", "#38BDF8",
              "textColor", "#F1F5F9");
      case SetupCardType.WORKSPACE ->
          Map.of(
              "backgroundColor", "#064E3B",
              "primaryColor", "#2DD4BF",
              "textColor", "#ECFDF5");
      case SetupCardType.RGB ->
          Map.of(
              "backgroundColor", "#0F0F0F",
              "primaryColor", "#00FFFF",
              "textColor", "#FFFFFF");
      case SetupCardType.MINIMALIST ->
          Map.of(
              "backgroundColor", "#0F1729",
              "primaryColor", "#94A3B8",
              "textColor", "#E2E8F0");
      case SetupCardType.DEVELOPMENT ->
          Map.of(
              "backgroundColor", "#030712",
              "primaryColor", "#10B981",
              "textColor", "#E5E7EB");
      case SetupCardType.PRODUCTIVITY ->
          Map.of(
              "backgroundColor", "#0C4A6E",
              "primaryColor", "#0EA5E9",
              "textColor", "#F0F9FF");
      case SetupCardType.PC ->
          Map.of(
              "backgroundColor", "#020617",
              "primaryColor", "#60A5FA",
              "textColor", "#F1F5F9");
      default ->
          Map.of(
              "backgroundColor", "#0F172A",
              "primaryColor", "#3B82F6",
              "textColor", "#F3F4F6");
    };
  }

  private int calculateContentWidth(final @NotNull SystemRequirement sysInfo, final String type) {
    final int imageWidth = 120;
    final int padding = 20;
    final int spacing = 20;
    final int minColumnWidth = 200;

    int leftColMaxContent, rightColMaxContent;

    switch (type.toLowerCase()) {
      case "workspace", "minimalist", "productivity" -> {
        leftColMaxContent =
            getMaxTextLength(
                sysInfo.getCpu(), sysInfo.getMonitor(), sysInfo.getKeyboard(), sysInfo.getMouse());
        rightColMaxContent =
            getMaxTextLength(
                sysInfo.getOther(), sysInfo.getOther(), sysInfo.getOther(), sysInfo.getOther());
      }
      default -> {
        leftColMaxContent =
            getMaxTextLength(
                sysInfo.getCpu(), sysInfo.getGpu(), sysInfo.getRam(), sysInfo.getStorage());
        rightColMaxContent =
            getMaxTextLength(
                sysInfo.getMotherboard(),
                sysInfo.getPsu(),
                sysInfo.getSetupCase(),
                sysInfo.getMonitor());
      }
    }

    leftColMaxContent = Math.max(leftColMaxContent, minColumnWidth);
    rightColMaxContent = Math.max(rightColMaxContent, minColumnWidth);

    final int categoriesWidth = this.calculateCategoriesWidth(sysInfo);

    return imageWidth
        + padding
        + Math.max(leftColMaxContent + spacing + rightColMaxContent, categoriesWidth)
        + padding;
  }

  private int calculateCategoriesWidth(final SystemRequirement sysInfo) {
    if (sysInfo.getCategories() == null || sysInfo.getCategories().isEmpty()) {
      return 0;
    }
    return sysInfo.getCategories().stream()
        .mapToInt(cat -> (cat.name().length() * CATEGORY_WIDTH_MULTIPLIER) + CATEGORY_WIDTH_PADDING)
        .sum();
  }

  private int getMaxTextLength(final String... texts) {
    return Stream.of(texts)
        .filter(StringUtils::hasText)
        .mapToInt(s -> Math.min(s.length() * TEXT_LENGTH_MULTIPLIER, MAX_TEXT_WIDTH))
        .max()
        .orElse(0);
  }

  public @NotNull List<String> getCardList(final @NotNull String userId) {
    final User user =
        this.userRepository
            .findUserById(userId)
            .orElseThrow(() -> new ItemNotFoundException("userNotFound"));

    if (this.isEmptySysInfo(user.getSystemRequirement())) {
      return List.of();
    }

    final String struct = "<img src=\"%s/public/card/user/%s/sys-card?type=%s\"/>";

    return SetupCardType.getAllCategories().stream()
        .map(c -> struct.formatted(this.baseUrl, userId, c))
        .toList();
  }

  private boolean isEmptySysInfo(final @NotNull SystemRequirement systemRequirement) {
    return systemRequirement.getCategories() == null || systemRequirement.getCategories().isEmpty();
  }
}
