package com.camrs.dto;

import com.camrs.entity.AuditLog;
import java.time.LocalDateTime;

public class AuditLogResponse {
    private Long id;
    private String username;
    private String action;
    private String entityType;
    private Integer entityId;
    private String ipAddress;
    private LocalDateTime timestamp;

    public AuditLogResponse(AuditLog log) {
        this.id = log.getId();
        this.username = log.getUser() != null ? log.getUser().getUsername() : "SYSTEM";
        this.action = log.getAction();
        this.entityType = log.getEntityType();
        this.entityId = log.getEntityId();
        this.ipAddress = log.getIpAddress();
        this.timestamp = log.getTimestamp();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Integer getEntityId() { return entityId; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
