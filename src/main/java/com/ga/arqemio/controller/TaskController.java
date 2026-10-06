package com.ga.arqemio.controller;

import com.ga.arqemio.model.Task;
import com.ga.arqemio.model.request.TaskRequest;
import com.ga.arqemio.model.request.TaskStatusRequest;
import com.ga.arqemio.model.response.TaskResponse;
import com.ga.arqemio.service.TaskService;
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
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest taskRequest){
        Task task= taskService.createTask(taskRequest);
        TaskResponse taskResponse=new TaskResponse(
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

        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponse);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(){
        List<Task> tasks = taskService.getAllTasks();
        List<TaskResponse> taskResponses = new ArrayList<>();

        for (Task task: tasks){
            TaskResponse taskResponse= new TaskResponse(
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

            taskResponses.add(taskResponse);
        }
        return ResponseEntity.ok(taskResponses);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);

        TaskResponse taskResponse = new TaskResponse(
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

        return ResponseEntity.ok(taskResponse);
    }


    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long taskId, @RequestBody TaskRequest taskRequest) {

        Task task = taskService.updateTask(taskId, taskRequest);
        TaskResponse taskResponse = new TaskResponse(
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

        return ResponseEntity.ok(taskResponse);
    }


    @DeleteMapping("/{taskId}")
    public ResponseEntity<TaskResponse> archiveTask(@PathVariable Long taskId) {

        Task task = taskService.archiveTask(taskId);
        TaskResponse taskResponse = new TaskResponse(
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

        return ResponseEntity.ok(taskResponse);
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long taskId, @RequestBody TaskStatusRequest taskStatusRequest){
        Task task= taskService.updateTaskStatus(taskId, taskStatusRequest.getStatus());
        TaskResponse taskResponse= new TaskResponse(
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

        return ResponseEntity.ok(taskResponse);
    }

}
