package com.autoflow.service;

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
import java.util.Optional;

@Service
public class TaskExecutionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final AuditLogRepository auditLogRepository;

    public TaskExecutionService(WorkflowRepository workflowRepository,
                                WorkflowTaskRepository workflowTaskRepository,
                                AuditLogRepository auditLogRepository) {
        this.workflowRepository = workflowRepository;
        this.workflowTaskRepository = workflowTaskRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public String executeWorkflow(Long workflowId) {
        Optional<Workflow> workflowOptional = workflowRepository.findById(workflowId);

        if (workflowOptional.isEmpty()) {
            return "Workflow not found";
        }

        Workflow workflow = workflowOptional.get();
        List<WorkflowTask> tasks = workflowTaskRepository.findByWorkflowId(workflowId);

        workflow.setStatus(WorkflowStatus.IN_PROGRESS);
        workflow.setUpdatedAt(LocalDateTime.now());
        workflowRepository.save(workflow);

        createAuditLog(
                "Orchestrator Agent",
                "START_WORKFLOW",
                "Workflow execution started by orchestrator.",
                workflow,
                null
        );

        for (WorkflowTask task : tasks) {
            executeSingleTask(task, workflow);
        }

        boolean hasFailure = tasks.stream().anyMatch(task ->
                task.getStatus() == TaskStatus.FAILED || task.getStatus() == TaskStatus.ESCALATED
        );

        workflow.setStatus(hasFailure ? WorkflowStatus.FAILED : WorkflowStatus.COMPLETED);
        workflow.setUpdatedAt(LocalDateTime.now());
        workflowRepository.save(workflow);

        createAuditLog(
                "Orchestrator Agent",
                "COMPLETE_WORKFLOW",
                hasFailure
                        ? "Workflow finished with one or more failed/escalated tasks."
                        : "Workflow completed successfully.",
                workflow,
                null
        );

        return hasFailure ? "Workflow executed with some failures." : "Workflow executed successfully.";
    }

    @Transactional
    public String retryTask(Long taskId) {
        Optional<WorkflowTask> taskOptional = workflowTaskRepository.findById(taskId);

        if (taskOptional.isEmpty()) {
            return "Task not found";
        }

        WorkflowTask task = taskOptional.get();
        Workflow workflow = task.getWorkflow();

        int currentRetryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
        task.setRetryCount(currentRetryCount + 1);
        task.setStatus(TaskStatus.RETRYING);
        task.setUpdatedAt(LocalDateTime.now());
        workflowTaskRepository.save(task);

        createAuditLog(
                "Recovery Agent",
                "RETRY_TASK",
                "Retry triggered for failed task.",
                workflow,
                task
        );

        boolean success = simulateTaskExecution(task);

        if (success) {
            task.setStatus(TaskStatus.SUCCESS);
            task.setErrorMessage(null);
            task.setUpdatedAt(LocalDateTime.now());
            workflowTaskRepository.save(task);

            createAuditLog(
                    "Recovery Agent",
                    "TASK_RECOVERED",
                    "Task recovered successfully after retry.",
                    workflow,
                    task
            );
        } else {
            task.setStatus(TaskStatus.ESCALATED);
            task.setErrorMessage("Retry failed. Escalated to human operator.");
            task.setUpdatedAt(LocalDateTime.now());
            workflowTaskRepository.save(task);

            createAuditLog(
                    "Recovery Agent",
                    "TASK_ESCALATED",
                    "Retry failed. Escalated to human operator.",
                    workflow,
                    task
            );
        }

        updateWorkflowStatusAfterRetry(workflow);

        return "Retry flow completed for task " + taskId;
    }

    private void executeSingleTask(WorkflowTask task, Workflow workflow) {
        task.setStatus(TaskStatus.RUNNING);
        task.setUpdatedAt(LocalDateTime.now());
        workflowTaskRepository.save(task);

        createAuditLog(
                task.getAssignedAgent(),
                "START_TASK",
                "Task execution started.",
                workflow,
                task
        );

        boolean success = simulateTaskExecution(task);

        if (success) {
            task.setStatus(TaskStatus.SUCCESS);
            task.setErrorMessage(null);

            createAuditLog(
                    task.getAssignedAgent(),
                    "TASK_SUCCESS",
                    "Task completed successfully.",
                    workflow,
                    task
            );
        } else {
            int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
            task.setRetryCount(retries + 1);
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("Simulated task failure occurred.");

            createAuditLog(
                    "Monitoring Agent",
                    "TASK_FAILURE_DETECTED",
                    "Task failed during execution and marked for recovery.",
                    workflow,
                    task
            );
        }

        task.setUpdatedAt(LocalDateTime.now());
        workflowTaskRepository.save(task);
    }

    private boolean simulateTaskExecution(WorkflowTask task) {
        String taskName = task.getTaskName().toLowerCase();
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();

        // Demo logic:
        // Laptop task fails first time, succeeds on retry
        if (taskName.contains("laptop")) {
            return retryCount > 0;
        }

        // All other tasks succeed
        return true;
    }

    private void updateWorkflowStatusAfterRetry(Workflow workflow) {
        List<WorkflowTask> updatedTasks = workflowTaskRepository.findByWorkflowId(workflow.getId());

        boolean hasFailure = updatedTasks.stream().anyMatch(task ->
                task.getStatus() == TaskStatus.FAILED || task.getStatus() == TaskStatus.ESCALATED
        );

        boolean allSuccess = updatedTasks.stream().allMatch(task ->
                task.getStatus() == TaskStatus.SUCCESS
        );

        if (allSuccess) {
            workflow.setStatus(WorkflowStatus.COMPLETED);
            workflow.setUpdatedAt(LocalDateTime.now());
            workflowRepository.save(workflow);

            createAuditLog(
                    "Orchestrator Agent",
                    "WORKFLOW_RECOVERED",
                    "All tasks are now successful after recovery. Workflow marked as completed.",
                    workflow,
                    null
            );
        } else if (hasFailure) {
            workflow.setStatus(WorkflowStatus.FAILED);
            workflow.setUpdatedAt(LocalDateTime.now());
            workflowRepository.save(workflow);
        }
    }

    private void createAuditLog(String agentName,
                                String action,
                                String reason,
                                Workflow workflow,
                                WorkflowTask task) {
        AuditLog log = new AuditLog();
        log.setAgentName(agentName);
        log.setAction(action);
        log.setDecisionReason(reason);
        log.setCreatedAt(LocalDateTime.now());
        log.setWorkflow(workflow);
        log.setTask(task);
        auditLogRepository.save(log);
    }
}