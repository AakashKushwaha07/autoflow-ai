package com.autoflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoflow.model.WorkflowTask;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
    List<WorkflowTask> findByWorkflowId(Long workflowId);
}
