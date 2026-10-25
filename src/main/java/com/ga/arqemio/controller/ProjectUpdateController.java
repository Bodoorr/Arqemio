package com.ga.arqemio.controller;

import com.ga.arqemio.model.ProjectUpdate;
import com.ga.arqemio.model.request.ProjectUpdateRequest;
import com.ga.arqemio.model.request.ProjectUpdateReviewRequest;
import com.ga.arqemio.model.response.ProjectUpdateResponse;
import com.ga.arqemio.service.ProjectUpdateService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ProjectUpdateResponse> createProjectUpdate(@Valid @RequestBody ProjectUpdateRequest projectUpdateRequest){
        ProjectUpdate projectUpdate=projectUpdateService.createProjectUpdate(projectUpdateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(projectUpdate));
    }

    @GetMapping("/project/{projectId}")
    public List<ProjectUpdateResponse> getAllProjectUpdates(@PathVariable Long projectId){
        List<ProjectUpdate> projectUpdates= projectUpdateService.getProjectUpdates(projectId);
        List<ProjectUpdateResponse> projectUpdateResponses=new ArrayList<>();

        for (ProjectUpdate projectUpdate:projectUpdates){
            projectUpdateResponses.add(toResponse(projectUpdate));
        }
        return projectUpdateResponses;
    }

    @GetMapping("/{updateId}")
    public ProjectUpdateResponse getProjectUpdateById(@PathVariable Long updateId){
        ProjectUpdate projectUpdate= projectUpdateService.getProjectUpdateById(updateId);
        return toResponse(projectUpdate);
    }

    @PutMapping("/{updateId}")
    public ProjectUpdateResponse updateProjectUpdate(@PathVariable Long updateId, @Valid @RequestBody ProjectUpdateRequest projectUpdateRequest){
        ProjectUpdate projectUpdate= projectUpdateService.updateProjectUpdate(updateId,projectUpdateRequest);
        return toResponse(projectUpdate);
    }

    @DeleteMapping("/{updateId}")
    public ProjectUpdateResponse archiveProjectUpdate(@PathVariable Long updateId){
        ProjectUpdate projectUpdate= projectUpdateService.archiveProjectUpdate(updateId);
        return toResponse(projectUpdate);
    }

    @PatchMapping("/{updateId}/review")
    public ProjectUpdateResponse reviewProjectUpdate(@PathVariable Long updateId, @Valid @RequestBody ProjectUpdateReviewRequest projectUpdateReviewRequest){
        ProjectUpdate projectUpdate= projectUpdateService.reviewProjectUpdate(updateId,projectUpdateReviewRequest.getStatus(), projectUpdateReviewRequest.getReviewNote());
        return toResponse(projectUpdate);
    }

    private ProjectUpdateResponse toResponse(ProjectUpdate projectUpdate){
        Long reviewedByMembershipId = null;
        String reviewedByName = null;

        if (projectUpdate.getReviewedBy() != null) {
            reviewedByMembershipId = projectUpdate.getReviewedBy().getId();
            reviewedByName = projectUpdate.getReviewedBy().getUser().getName();
            }

        return new ProjectUpdateResponse(
                projectUpdate.getId(),
                projectUpdate.getProject().getId(),
                projectUpdate.getProject().getName(),
                projectUpdate.getUpdatedBy().getId(),
                projectUpdate.getUpdatedBy().getUser().getName(),
                projectUpdate.getTitle(),
                projectUpdate.getDescription(),
                projectUpdate.getStatus(),
                projectUpdate.getReviewNote(),
                reviewedByMembershipId,
                reviewedByName
        );
    }

    }

