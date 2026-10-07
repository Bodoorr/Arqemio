package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMembershipResponse {
    private Long membershipId;
    private Long companyId;
    private String companyName;
    private String role;
    private String status;
}
