package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String mobileNumber;
    private String profilePicture;
    private boolean isPlatformAdmin;
    private List<UserMembershipResponse> memberships;
}
