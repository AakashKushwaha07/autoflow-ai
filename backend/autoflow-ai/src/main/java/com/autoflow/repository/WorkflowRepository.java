package com.autoflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoflow.model.Workflow;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
}