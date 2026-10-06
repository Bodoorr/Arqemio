package com.ga.arqemio.controller;

import com.ga.arqemio.model.ProjectUpdate;
import com.ga.arqemio.model.request.ProjectUpdateRequest;
import com.ga.arqemio.model.request.ProjectUpdateReviewRequest;
import com.ga.arqemio.model.response.ProjectUpdateResponse;
import com.ga.arqemio.service.ProjectUpdateService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/company/projects/updates")
@AllArgsConstructor
public class ProjectUpdateController {
    private ProjectUpdateService projectUpdateService;

    @PostMapping
    public ResponseEntity<ProjectUpdateResponse> createProjectUpdate(@RequestBody ProjectUpdateRequest projectUpdateRequest){
        ProjectUpdate projectUpdate=projectUpdateService.createProjectUpdate(projectUpdateRequest);
        Long reviewedByMembershipId = null;
        String reviewedByName = null;

        if (projectUpdate.getReviewedBy() != null) {
            reviewedByMembershipId = projectUpdate.getReviewedBy().getId();
            reviewedByName = projectUpdate.getReviewedBy().getUser().getName();
        }
        ProjectUpdateResponse projectUpdateResponse=new ProjectUpdateResponse(
                projectUpdate.getId(),
                projectUpdate.getProject().getId(),
                projectUpdate.getProject().getName(),
                projectUpdate.getUpdatedBy().getId(),
                projectUpdate.getUpdatedBy().getUser().getName(),
                projectUpdate.getTitle(),
                projectUpdate.getDescription(),
                projectUpdate.getStatus(),
                reviewedByMembershipId,
                reviewedByName
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(projectUpdateResponse);
    }

    @GetMapping("/project/{projectId}")
    public List<ProjectUpdateResponse> getAllProjectUpdates(@PathVariable Long projectId){
        List<ProjectUpdate> projectUpdates= projectUpdateService.getProjectUpdates(projectId);
        List<ProjectUpdateResponse> projectUpdateResponses=new ArrayList<>();

        for (ProjectUpdate projectUpdate:projectUpdates){
            Long reviewedByMembershipId = null;
            String reviewedByName = null;

            if (projectUpdate.getReviewedBy() != null) {
                reviewedByMembershipId = projectUpdate.getReviewedBy().getId();
                reviewedByName = projectUpdate.getReviewedBy().getUser().getName();
            }
            ProjectUpdateResponse projectUpdateResponse=new ProjectUpdateResponse(
                    projectUpdate.getId(),
                    projectUpdate.getProject().getId(),
                    projectUpdate.getProject().getName(),
                    projectUpdate.getUpdatedBy().getId(),
                    projectUpdate.getUpdatedBy().getUser().getName(),
                    projectUpdate.getTitle(),
                    projectUpdate.getDescription(),
                    projectUpdate.getStatus(),
                    reviewedByMembershipId,
                    reviewedByName
            );
            projectUpdateResponses.add(projectUpdateResponse);
        }
        return projectUpdateResponses;
    }

    @GetMapping("/{updateId}")
    public ProjectUpdateResponse getProjectUpdateById(@PathVariable Long updateId){
        ProjectUpdate projectUpdate= projectUpdateService.getProjectUpdateById(updateId);
        Long reviewedByMembershipId = null;
        String reviewedByName = null;

        if (projectUpdate.getReviewedBy() != null) {
            reviewedByMembershipId = projectUpdate.getReviewedBy().getId();
            reviewedByName = projectUpdate.getReviewedBy().getUser().getName();
        }
        ProjectUpdateResponse projectUpdateResponse=new ProjectUpdateResponse(
                projectUpdate.getId(),
                projectUpdate.getProject().getId(),
                projectUpdate.getProject().getName(),
                projectUpdate.getUpdatedBy().getId(),
                projectUpdate.getUpdatedBy().getUser().getName(),
                projectUpdate.getTitle(),
                projectUpdate.getDescription(),
                projectUpdate.getStatus(),
                reviewedByMembershipId,
                reviewedByName
        );
        return projectUpdateResponse;
    }

    @PutMapping("/{updateId}")
    public ProjectUpdateResponse updateProjectUpdate(@PathVariable Long updateId, @RequestBody ProjectUpdateRequest projectUpdateRequest){
        ProjectUpdate projectUpdate= projectUpdateService.updateProjectUpdate(updateId,projectUpdateRequest);
        Long reviewedByMembershipId = null;
        String reviewedByName = null;
        if (projectUpdate.getReviewedBy() != null) {
            reviewedByMembershipId = projectUpdate.getReviewedBy().getId();
            reviewedByName = projectUpdate.getReviewedBy().getUser().getName();
        }
        ProjectUpdateResponse projectUpdateResponse=new ProjectUpdateResponse(
                projectUpdate.getId(),
                projectUpdate.getProject().getId(),
                projectUpdate.getProject().getName(),
                projectUpdate.getUpdatedBy().getId(),
                projectUpdate.getUpdatedBy().getUser().getName(),
                projectUpdate.getTitle(),
                projectUpdate.getDescription(),
                projectUpdate.getStatus(),
                reviewedByMembershipId,
                reviewedByName
        );
        return projectUpdateResponse;
    }

    @DeleteMapping("/{updateId}")
    public ProjectUpdateResponse archiveProjectUpdate(@PathVariable Long updateId){
        ProjectUpdate projectUpdate= projectUpdateService.archiveProjectUpdate(updateId);
        Long reviewedByMembershipId = null;
        String reviewedByName = null;
        if (projectUpdate.getReviewedBy() != null) {
            reviewedByMembershipId = projectUpdate.getReviewedBy().getId();
            reviewedByName = projectUpdate.getReviewedBy().getUser().getName();
        }
        ProjectUpdateResponse projectUpdateResponse=new ProjectUpdateResponse(
                projectUpdate.getId(),
                projectUpdate.getProject().getId(),
                projectUpdate.getProject().getName(),
                projectUpdate.getUpdatedBy().getId(),
                projectUpdate.getUpdatedBy().getUser().getName(),
                projectUpdate.getTitle(),
                projectUpdate.getDescription(),
                projectUpdate.getStatus(),
                reviewedByMembershipId,
                reviewedByName
        );
        return projectUpdateResponse;
    }

    @PatchMapping("/{updateId}/review")
    public ProjectUpdateResponse reviewProjectUpdate(@PathVariable Long updateId, @RequestBody ProjectUpdateReviewRequest projectUpdateReviewRequest){
        ProjectUpdate projectUpdate= projectUpdateService.reviewProjectUpdate(updateId,projectUpdateReviewRequest.getStatus());
        Long reviewedByMembershipId = null;
        String reviewedByName = null;
        if (projectUpdate.getReviewedBy() != null) {
            reviewedByMembershipId = projectUpdate.getReviewedBy().getId();
            reviewedByName = projectUpdate.getReviewedBy().getUser().getName();
        }
        ProjectUpdateResponse projectUpdateResponse=new ProjectUpdateResponse(
                projectUpdate.getId(),
                projectUpdate.getProject().getId(),
                projectUpdate.getProject().getName(),
                projectUpdate.getUpdatedBy().getId(),
                projectUpdate.getUpdatedBy().getUser().getName(),
                projectUpdate.getTitle(),
                projectUpdate.getDescription(),
                projectUpdate.getStatus(),
                reviewedByMembershipId,
                reviewedByName
        );
        return projectUpdateResponse;
    }
    }

