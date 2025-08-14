package com.setupshowroom.notification.dto;

import com.setupshowroom.notification.NotificationType;
import com.setupshowroom.user.dto.UserInfo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationInfo {
  private String id;
  private String title;
  private String description;
  private NotificationType type;
  private String action;
  private UserInfo user;
  private boolean read;
  private Instant createdAt;
}
