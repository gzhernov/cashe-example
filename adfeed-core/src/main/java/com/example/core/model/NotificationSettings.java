package com.example.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationSettings {
    private Long clientId;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean pushNotifications;
    private String notificationFrequency;
    private Boolean dailyDigest;
    private Boolean weeklyReport;
}