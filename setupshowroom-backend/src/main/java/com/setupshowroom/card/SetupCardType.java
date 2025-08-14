package com.setupshowroom.card;

import java.util.List;

public class SetupCardType {
  public static final String DEVELOPMENT = "development";
  public static final String GAMING = "gaming";
  public static final String MINIMALIST = "minimalist";
  public static final String PC = "pc";
  public static final String PRODUCTIVITY = "productivity";
  public static final String RGB = "rgb";
  public static final String ROOM = "room";
  public static final String WORKSPACE = "workspace";

  public static List<String> getAllCategories() {
    return List.of(GAMING, ROOM, WORKSPACE, RGB, MINIMALIST, DEVELOPMENT, PRODUCTIVITY, PC);
  }
}
