package com.setupshowroom.notification;

import com.setupshowroom.notification.dto.NotificationInfo;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
  @Query(
      """
      select n.id as id,
             n.title as title,
             n.description as description,
             n.type as type,
             n.action as action,
             n.user as user,
             n.read as read,
             n.timestamps.createdAt as createdAt
      from Notification n
      where n.user.id = :userId
        and n.read = true
        and n.deleted = false
      """)
  @NotNull
  List<NotificationInfo> findAllByUserIdAndReadTrueAndDeletedFalse(
      @NotNull String userId, @NotNull Pageable pageable);

  @NotNull
  @Query(
      """
      select new com.setupshowroom.notification.dto.NotificationInfo(
             n.id as id,
             n.title as title,
             n.description as description,
             n.type as type,
             n.action as action,
             new com.setupshowroom.user.dto.UserInfo(
                    n.user.id,
                    n.user.fullName,
                    n.user.email,
                    n.user.username,
                    n.user.profession,
                    n.user.enabled),
             n.read as read,
             n.timestamps.createdAt)
      from Notification n
      where n.to = :userId
        and n.read = false
        and n.deleted = false
        order by n.timestamps.createdAt desc
      """)
  List<NotificationInfo> findAllByUserIdAndReadFalseAndDeletedFalse(
      @NotNull String userId, @NotNull Pageable pageable);

  @Modifying
  @Query("UPDATE Notification n SET n.read = true WHERE n.id = :notificationId and n.to = :userId")
  void markAsRead(@NotNull String notificationId, @NotNull String userId);

  @Modifying
  @Query("UPDATE Notification n SET n.read = false WHERE n.id = :notificationId and n.to = :userId")
  void markAsUnread(@NotNull String notificationId, @NotNull String userId);

  @Modifying
  @Query(
      "UPDATE Notification n SET n.deleted = true WHERE n.id = :notificationId and n.to = :userId")
  void deleteNotification(@NotNull String notificationId, @NotNull String userId);

  @Modifying
  @Query("UPDATE Notification n SET n.deleted = true WHERE n.to = :userId")
  void deleteAllNotifications(@NotNull String userId);

  @Query(
      """
      select new com.setupshowroom.notification.dto.NotificationInfo(
          n.id,
          n.title,
          n.description,
          n.type,
          n.action,
          new com.setupshowroom.user.dto.UserInfo(
                    n.user.id,
                    n.user.fullName,
                    n.user.email,
                    n.user.username,
                    n.user.profession,
                    n.user.enabled),
          n.read,
          n.timestamps.createdAt
      )
      from Notification n
      where n.to= :userId
        and n.deleted = false
        order by n.timestamps.createdAt desc
      """)
  @NotNull
  List<NotificationInfo> findAllByUserIdAndDeletedFalse(
      @NotNull String userId, @NotNull Pageable pageable);

  @Modifying
  @Query("UPDATE Notification n SET n.read = true WHERE n.to = :userId")
  void markAsReadAll(@NotNull String userId);
}
