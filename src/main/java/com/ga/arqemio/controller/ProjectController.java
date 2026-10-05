package com.ga.arqemio.controller;

import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.Project;
import com.ga.arqemio.model.request.ProjectRequest;
import com.ga.arqemio.model.response.ProjectMemberResponse;
import com.ga.arqemio.model.response.ProjectResponse;
import com.ga.arqemio.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/company/projects")
@AllArgsConstructor
public class ProjectController {
    private ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest projectRequest){
        Project project= projectService.createProject(projectRequest);
        ProjectResponse projectResponse=new ProjectResponse(
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

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<Project> projects= projectService.getAllProjects();
        List<ProjectResponse> projectResponses= new ArrayList<>();
        for (Project project: projects){
            ProjectResponse projectResponse= new ProjectResponse(
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
            projectResponses.add(projectResponse);
        }

         return ResponseEntity.ok(projectResponses);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long projectId){
        Project project= projectService.getProjectById(projectId);

        ProjectResponse projectResponse=new ProjectResponse(
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
        return ResponseEntity.ok(projectResponse);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId, @RequestBody ProjectRequest projectRequest){
        Project project= projectService.updateProject(projectId, projectRequest);

        ProjectResponse projectResponse=new ProjectResponse(
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
          return ResponseEntity.ok(projectResponse);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> archiveProject(
            @PathVariable Long projectId) {

        Project project = projectService.archiveProject(projectId);

        ProjectResponse projectResponse = new ProjectResponse(
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

        return ResponseEntity.ok(projectResponse);
    }

    @PostMapping("/{projectId}/managers/{membershipId}")
    public ResponseEntity<ProjectMemberResponse> assignManager(@PathVariable Long projectId, @PathVariable Long membershipId){
        CompanyMembership manager = projectService.assignManager(projectId,membershipId);

        ProjectMemberResponse projectMemberResponse=new ProjectMemberResponse(
                projectId,
                manager.getCompany().getName(),
                manager.getId(),
                manager.getUser().getName(),
                manager.getRole()
        );

        return ResponseEntity.ok(projectMemberResponse);
    }

}
