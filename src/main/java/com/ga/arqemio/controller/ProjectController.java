package com.ga.arqemio.controller;

import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.Project;
import com.ga.arqemio.model.request.ProjectRequest;
import com.ga.arqemio.model.response.ProjectMemberResponse;
import com.ga.arqemio.model.response.ProjectResponse;
import com.ga.arqemio.service.ProjectService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest projectRequest){
        Project project= projectService.createProject(projectRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(project));

    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<Project> projects= projectService.getAllProjects();
        List<ProjectResponse> projectResponses= new ArrayList<>();
        for (Project project: projects){
            projectResponses.add(toResponse(project));
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
        return ResponseEntity.ok(toResponse(project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId, @Valid @RequestBody ProjectRequest projectRequest){
        Project project= projectService.updateProject(projectId, projectRequest);
          return ResponseEntity.ok(toResponse(project));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> archiveProject(
            @PathVariable Long projectId) {
        Project project = projectService.archiveProject(projectId);
        return ResponseEntity.ok(toResponse(project));
    }

    @PostMapping("/{projectId}/managers/{membershipId}")
    public ResponseEntity<ProjectMemberResponse> assignManager(@PathVariable Long projectId, @PathVariable Long membershipId){
        CompanyMembership manager = projectService.assignManager(projectId,membershipId);
        return ResponseEntity.ok(toMemberResponse(manager, projectId));
    }

    @DeleteMapping("/{projectId}/managers/{membershipId}")
    public ResponseEntity<ProjectMemberResponse> removeManager(@PathVariable Long projectId, @PathVariable Long membershipId){
        CompanyMembership manager= projectService.removeManager(projectId,membershipId);
        return ResponseEntity.ok(toMemberResponse(manager, projectId));
    }

    @PostMapping("/{projectId}/workers/{membershipId}")
    public ResponseEntity<ProjectMemberResponse> assignWorker(@PathVariable Long projectId, @PathVariable Long membershipId){
        CompanyMembership worker = projectService.assignWorker(projectId,membershipId);
        return ResponseEntity.ok(toMemberResponse(worker, projectId));
    }

    @DeleteMapping("/{projectId}/workers/{membershipId}")
    public ResponseEntity<ProjectMemberResponse> removeWorker(@PathVariable Long projectId, @PathVariable Long membershipId){
        CompanyMembership worker= projectService.removeWorker(projectId,membershipId);
        return ResponseEntity.ok(toMemberResponse(worker, projectId));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(@PathVariable Long projectId) {
        Project project = projectService.getProjectById(projectId);
        List<ProjectMemberResponse> members = new ArrayList<>();

        for (CompanyMembership manager : project.getManagers()) {
            members.add(toMemberResponse(manager, projectId));
        }

        for (CompanyMembership worker : project.getWorkers()) {
            members.add(toMemberResponse(worker, projectId));
        }
        return ResponseEntity.ok(members);
    }


    @GetMapping("/{projectId}/available-members")
    public ResponseEntity<List<ProjectMemberResponse>> getAvailableProjectMembers(@PathVariable Long projectId) {
        List<CompanyMembership> members = projectService.getAvailableProjectMembers(projectId);
        List<ProjectMemberResponse> memberResponses = new ArrayList<>();

        for (CompanyMembership member : members) {
            memberResponses.add(toMemberResponse(member, projectId));
        }
        return ResponseEntity.ok(memberResponses);
    }


    private ProjectResponse toResponse(Project project){
        return new ProjectResponse(
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
    }

    private ProjectMemberResponse toMemberResponse(CompanyMembership member, Long projectId){
        return new ProjectMemberResponse(
                projectId,
                member.getCompany().getName(),
                member.getId(),
                member.getUser().getName(),
                member.getRole()
        );
    }




}


