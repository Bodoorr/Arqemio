package com.ga.arqemio.controller;

import com.ga.arqemio.model.ProjectUpdate;
import com.ga.arqemio.model.request.ProjectUpdateRequest;
import com.ga.arqemio.model.response.ProjectUpdateResponse;
import com.ga.arqemio.service.ProjectUpdateService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
