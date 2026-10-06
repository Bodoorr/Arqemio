package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectUpdateResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long updatedByMembershipId;
    private String updatedByName;
    private String title;
    private String description;
    private String status;
    private Long reviewedByMembershipId;
    private String reviewedByName;

}
