package com.setupshowroom.shared.contentmoderation.dto;

import lombok.Getter;

@Getter
public enum InappropriateReason {
  NUDITY("detected nudity"),
  SELF_HARM("detected self-harm"),
  MEDICAL("detected medical"),
  DRUGS("detected drugs");

  private final String reason;

  InappropriateReason(final String reason) {
    this.reason = reason;
  }
}
