package com.autoflow.service;

import com.autoflow.dto.CreateWorkflowRequest;
import com.autoflow.model.AuditLog;
import com.autoflow.model.TaskStatus;
import com.autoflow.model.Workflow;
import com.autoflow.model.WorkflowStatus;
import com.autoflow.model.WorkflowTask;
import com.autoflow.repository.AuditLogRepository;
import com.autoflow.repository.WorkflowRepository;
import com.autoflow.repository.WorkflowTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final AuditLogRepository auditLogRepository;

    public WorkflowService(WorkflowRepository workflowRepository,
                           WorkflowTaskRepository workflowTaskRepository,
                           AuditLogRepository auditLogRepository) {
        this.workflowRepository = workflowRepository;
        this.workflowTaskRepository = workflowTaskRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public Workflow createWorkflow(CreateWorkflowRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Workflow workflow = new Workflow();
        workflow.setEmployeeName(request.getEmployeeName());
        workflow.setEmployeeEmail(request.getEmployeeEmail());
        workflow.setDepartment(request.getDepartment());
        workflow.setDesignation(request.getDesignation());
        workflow.setStatus(WorkflowStatus.PENDING);
        workflow.setCreatedAt(now);
        workflow.setUpdatedAt(now);

        Workflow savedWorkflow = workflowRepository.save(workflow);

        WorkflowTask task1 = buildTask("Create Email Account", "Orchestrator Agent", savedWorkflow, now);
        WorkflowTask task2 = buildTask("Allocate Laptop", "IT Ops Agent", savedWorkflow, now);
        WorkflowTask task3 = buildTask("Assign Training Modules", "Learning Agent", savedWorkflow, now);

        workflowTaskRepository.saveAll(List.of(task1, task2, task3));

        AuditLog auditLog = new AuditLog();
        auditLog.setAgentName("Workflow Service");
        auditLog.setAction("CREATE_WORKFLOW");
        auditLog.setDecisionReason("New employee onboarding workflow created and default tasks generated automatically.");
        auditLog.setCreatedAt(now);
        auditLog.setWorkflow(savedWorkflow);
        auditLog.setTask(null);

        auditLogRepository.save(auditLog);

        return savedWorkflow;
    }

    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    public List<WorkflowTask> getTasksByWorkflowId(Long workflowId) {
        return workflowTaskRepository.findByWorkflowId(workflowId);
    }

    public List<AuditLog> getAuditLogsByWorkflowId(Long workflowId) {
        return auditLogRepository.findByWorkflowIdOrderByCreatedAtAsc(workflowId);
    }

    @Transactional
    public String deleteWorkflow(Long workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId).orElse(null);

        if (workflow == null) {
            return "Workflow not found";
        }

        List<WorkflowTask> tasks = workflowTaskRepository.findByWorkflowId(workflowId);
        workflowTaskRepository.deleteAll(tasks);

        List<AuditLog> logs = auditLogRepository.findByWorkflowIdOrderByCreatedAtAsc(workflowId);
        auditLogRepository.deleteAll(logs);

        workflowRepository.delete(workflow);

        return "Workflow deleted successfully";
    }

    private WorkflowTask buildTask(String taskName, String assignedAgent, Workflow workflow, LocalDateTime now) {
        WorkflowTask task = new WorkflowTask();
        task.setTaskName(taskName);
        task.setAssignedAgent(assignedAgent);
        task.setStatus(TaskStatus.PENDING);
        task.setRetryCount(0);
        task.setErrorMessage(null);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setWorkflow(workflow);
        return task;
    }
}