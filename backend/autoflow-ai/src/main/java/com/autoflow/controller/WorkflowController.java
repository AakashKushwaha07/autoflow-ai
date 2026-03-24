package com.autoflow.controller;

import com.autoflow.dto.CreateWorkflowRequest;
import com.autoflow.model.AuditLog;
import com.autoflow.model.Workflow;
import com.autoflow.model.WorkflowTask;
import com.autoflow.service.TaskExecutionService;
import com.autoflow.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin
public class WorkflowController {

    private final WorkflowService workflowService;
    private final TaskExecutionService taskExecutionService;

    public WorkflowController(WorkflowService workflowService,
                              TaskExecutionService taskExecutionService) {
        this.workflowService = workflowService;
        this.taskExecutionService = taskExecutionService;
    }

    @PostMapping
    public Workflow createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        return workflowService.createWorkflow(request);
    }

    @GetMapping
    public List<Workflow> getAllWorkflows() {
        return workflowService.getAllWorkflows();
    }

    @GetMapping("/{id}/tasks")
    public List<WorkflowTask> getTasks(@PathVariable Long id) {
        return workflowService.getTasksByWorkflowId(id);
    }

    @GetMapping("/{id}/logs")
    public List<AuditLog> getLogs(@PathVariable Long id) {
        return workflowService.getAuditLogsByWorkflowId(id);
    }

    @PostMapping("/{id}/execute")
    public String executeWorkflow(@PathVariable Long id) {
        return taskExecutionService.executeWorkflow(id);
    }

    @PostMapping("/tasks/{taskId}/retry")
    public String retryTask(@PathVariable Long taskId) {
        return taskExecutionService.retryTask(taskId);
    }

    @DeleteMapping("/{id}")
    public String deleteWorkflow(@PathVariable Long id) {
        return workflowService.deleteWorkflow(id);
    }
}