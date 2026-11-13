package com.corporate.visitor.model;

import java.time.Duration;
import java.time.LocalDateTime;

public record Visit(
        long id,
        String visitorName,
        String company,
        String hostName,
        String phone,
        LocalDateTime checkedInAt,
        LocalDateTime checkedOutAt,
        int allowedMinutes,
        boolean reminderSent
) {
    public boolean isActive() {
        return checkedOutAt == null;
    }

    public boolean isOverdue(LocalDateTime now) {
        return isActive() && !now.isBefore(checkedInAt.plusMinutes(allowedMinutes));
    }

    public long overdueMinutes(LocalDateTime now) {
        return Math.max(0, Duration.between(checkedInAt.plusMinutes(allowedMinutes), now).toMinutes());
    }
}
