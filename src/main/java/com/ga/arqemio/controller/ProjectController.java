package com.ga.arqemio.controller;

import com.ga.arqemio.model.Project;
import com.ga.arqemio.model.request.ProjectRequest;
import com.ga.arqemio.model.response.InvitationResponse;
import com.ga.arqemio.model.response.ProjectResponse;
import com.ga.arqemio.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
@AllArgsConstructor
public class ProjectController {
    private ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest projectRequest){
        Project project= projectService.createProject(projectRequest);
        ProjectResponse projectResponse=new ProjectResponse(
                true,
                "Project created successfully.",
                project.getId(),
                project.getCompany().getId(),
                project.getCompany().getName(),
                project.getName(),
                project.getDescription(),
                project.getLocation(),
                project.getStartDate(),
                project.getExpectedEndDate(),
                project.getBudget(),
                project.getStatus()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);

    }

}
