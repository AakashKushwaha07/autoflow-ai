package com.autoflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoflow.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByWorkflowIdOrderByCreatedAtAsc(Long workflowId);
}