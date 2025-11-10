package com.ssafy.Dito.domain.log.fcm.document;

import com.ssafy.Dito.domain.log.common.MongoBaseDocument;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB document for FCM notification send logs
 * Tracks all FCM send attempts from /api/fcm/send endpoint
 * Used for monitoring notification delivery, success rates, and troubleshooting
 */
@Document(collection = "fcm_logs")
@CompoundIndexes({
    @CompoundIndex(name = "user_sent_idx", def = "{'user_id': 1, 'sent_at': -1}"),
    @CompoundIndex(name = "personal_sent_idx", def = "{'personal_id': 1, 'sent_at': -1}"),
    @CompoundIndex(name = "success_sent_idx", def = "{'success': 1, 'sent_at': -1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmLogDocument extends MongoBaseDocument {

    @Field("user_id")
    @Indexed
    private Long userId;

    @Field("personal_id")
    @Indexed
    private String personalId;

    @Field("title")
    private String title;

    @Field("message")
    private String message;

    @Field("mission_id")
    private Long missionId;

    @Field("has_mission")
    private Boolean hasMission;

    @Field("fcm_token")
    private String fcmToken;

    @Field("success")
    @Indexed
    private Boolean success;

    @Field("firebase_message_id")
    private String firebaseMessageId;

    @Field("error_code")
    private String errorCode;

    @Field("error_message")
    private String errorMessage;

    @Field("sent_at")
    @Indexed
    private LocalDateTime sentAt;

    @Builder
    private FcmLogDocument(Long userId, String personalId, String title, String message,
                           Long missionId, Boolean hasMission, String fcmToken,
                           Boolean success, String firebaseMessageId,
                           String errorCode, String errorMessage, LocalDateTime sentAt) {
        this.userId = userId;
        this.personalId = personalId;
        this.title = title;
        this.message = message;
        this.missionId = missionId;
        this.hasMission = hasMission;
        this.fcmToken = fcmToken;
        this.success = success;
        this.firebaseMessageId = firebaseMessageId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.sentAt = sentAt;
    }

    /**
     * Update log with success result
     * @param firebaseMessageId Firebase response message ID
     */
    public void markSuccess(String firebaseMessageId) {
        this.success = true;
        this.firebaseMessageId = firebaseMessageId;
    }

    /**
     * Update log with failure result
     * @param errorCode Error code from FirebaseMessagingException
     * @param errorMessage Error message
     */
    public void markFailure(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
