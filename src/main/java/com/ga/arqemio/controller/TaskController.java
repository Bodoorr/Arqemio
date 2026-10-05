package com.ga.arqemio.controller;

import com.ga.arqemio.model.Task;
import com.ga.arqemio.model.request.TaskRequest;
import com.ga.arqemio.model.response.TaskResponse;
import com.ga.arqemio.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



}
