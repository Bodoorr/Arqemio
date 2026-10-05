package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectMemberResponse {
    private Long projectId;
    private String companyName;
    private Long membershipId;
    private String name;
    private String role;
}
