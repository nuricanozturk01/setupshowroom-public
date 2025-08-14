package com.setupshowroom.shared.contentmoderation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("all")
public class Frame {
  public Nudity nudity;

  @JsonProperty("recreational_drug")
  public RecreationalDrug recreationalDrug;

  public Medical medical;

  @JsonProperty("self-harm")
  public SelfHarm selfHarm;
}
