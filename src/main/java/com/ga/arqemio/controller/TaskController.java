package com.ga.arqemio.controller;

import com.ga.arqemio.model.Task;
import com.ga.arqemio.model.request.TaskRequest;
import com.ga.arqemio.model.request.TaskStatusRequest;
import com.ga.arqemio.model.response.TaskResponse;
import com.ga.arqemio.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/company/projects/tasks")
@AllArgsConstructor
public class TaskController {
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest){
        Task task= taskService.createTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(task));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(){
        List<Task> tasks = taskService.getAllTasks();
        List<TaskResponse> taskResponses = new ArrayList<>();

        for (Task task: tasks){
            taskResponses.add(toResponse(task));
        }
        return ResponseEntity.ok(taskResponses);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(toResponse(task));
    }


    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskRequest taskRequest) {
        Task task = taskService.updateTask(taskId, taskRequest);
        return ResponseEntity.ok(toResponse(task));
    }


    @DeleteMapping("/{taskId}")
    public ResponseEntity<TaskResponse> archiveTask(@PathVariable Long taskId) {
        Task task = taskService.archiveTask(taskId);
        return ResponseEntity.ok(toResponse(task));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long taskId, @Valid @RequestBody TaskStatusRequest taskStatusRequest){
        Task task= taskService.updateTaskStatus(taskId, taskStatusRequest.getStatus());
        return ResponseEntity.ok(toResponse(task));
    }

    private TaskResponse toResponse(Task task){
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDateTime(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
