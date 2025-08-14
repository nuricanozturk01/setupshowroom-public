package com.setupshowroom.notification.dto;

import com.setupshowroom.notification.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationForm {
  private String title;
  private String description;
  private NotificationType type;
  private String action;
  private String to;
}
