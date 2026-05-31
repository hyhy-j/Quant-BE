package com.example.quantserver.ai.entity;

import com.example.quantserver.global.common.BaseEntity;
import com.example.quantserver.ai.enums.AgentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_activity_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE agent_activity_logs SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class AgentActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String agentType;

    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status;

    @Column(columnDefinition = "TEXT")
    private String detail;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Builder
    public AgentActivityLog(String agentType, String action, AgentStatus status, LocalDateTime startedAt) {
        this.agentType = agentType;
        this.action = action;
        this.status = status;
        this.startedAt = startedAt;
    }

    public void complete(AgentStatus status, String detail) {
        this.status = status;
        this.detail = detail;
        this.finishedAt = LocalDateTime.now();
    }
}