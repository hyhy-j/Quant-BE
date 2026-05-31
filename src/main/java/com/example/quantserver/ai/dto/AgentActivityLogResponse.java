package com.example.quantserver.ai.dto;

import com.example.quantserver.ai.entity.AgentActivityLog;
import com.example.quantserver.ai.enums.AgentStatus;

import java.time.LocalDateTime;

public record AgentActivityLogResponse(
        Long id,
        String agentType,
        String action,
        AgentStatus status,
        String detail,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
    public static AgentActivityLogResponse from(AgentActivityLog log) {
        return new AgentActivityLogResponse(
                log.getId(),
                log.getAgentType(),
                log.getAction(),
                log.getStatus(),
                log.getDetail(),
                log.getStartedAt(),
                log.getFinishedAt()
        );
    }
}