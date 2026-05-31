package com.example.quantserver.ai.repository;

import com.example.quantserver.ai.entity.AgentActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentActivityLogRepository extends JpaRepository<AgentActivityLog, Long> {

    List<AgentActivityLog> findAllByOrderByCreatedAtDesc();

    List<AgentActivityLog> findAllByAgentTypeOrderByCreatedAtDesc(String agentType);
}