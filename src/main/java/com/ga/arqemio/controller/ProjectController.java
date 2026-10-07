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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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


    @GetMapping("/search")
    public ResponseEntity<?> getProjects(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, @RequestParam(defaultValue = "id") String sortBy,
                                         @RequestParam(defaultValue = "true") boolean ascending, @RequestParam(required = false) String name, @RequestParam(required = false) String status){
        Sort sort;
        if (ascending){
            sort= Sort.by(sortBy).ascending();
        } else {
            sort= Sort.by(sortBy).descending();
        }

        Pageable pageable= PageRequest.of(page,size,sort);
        Page<Project> projects= projectService.getProjects(pageable,name,status);

        return ResponseEntity.ok(projects);
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

    @DeleteMapping("/{projectId}/managers/{membershipId}")
    public ResponseEntity<ProjectMemberResponse> removeManager(@PathVariable Long projectId, @PathVariable Long membershipId){
        CompanyMembership manager= projectService.removeManager(projectId,membershipId);

        ProjectMemberResponse projectMemberResponse= new ProjectMemberResponse(
                projectId,
                manager.getCompany().getName(),
                manager.getId(),
                manager.getUser().getName(),
                manager.getRole()
        );

        return ResponseEntity.ok(projectMemberResponse);
    }

    @PostMapping("/{projectId}/workers/{membershipId}")
    public ResponseEntity<ProjectMemberResponse> assignWorker(@PathVariable Long projectId, @PathVariable Long membershipId){
        CompanyMembership worker = projectService.assignWorker(projectId,membershipId);

        ProjectMemberResponse projectMemberResponse=new ProjectMemberResponse(
                projectId,
                worker.getCompany().getName(),
                worker.getId(),
                worker.getUser().getName(),
                worker.getRole()
        );

        return ResponseEntity.ok(projectMemberResponse);
    }

    @DeleteMapping("/{projectId}/workers/{membershipId}")
    public ResponseEntity<ProjectMemberResponse> removeWorker(@PathVariable Long projectId, @PathVariable Long membershipId){
        CompanyMembership worker= projectService.removeWorker(projectId,membershipId);

        ProjectMemberResponse projectMemberResponse= new ProjectMemberResponse(
                projectId,
                worker.getCompany().getName(),
                worker.getId(),
                worker.getUser().getName(),
                worker.getRole()
        );

        return ResponseEntity.ok(projectMemberResponse);
    }



}


