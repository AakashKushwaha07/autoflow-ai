package com.autoflow.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String agentName;
    private String action;

    @Column(length = 2000)
    private String decisionReason;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private WorkflowTask task;

    public AuditLog() {
    }

    public Long getId() {
        return id;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public WorkflowTask getTask() {
        return task;
    }

    public void setTask(WorkflowTask task) {
        this.task = task;
    }
}