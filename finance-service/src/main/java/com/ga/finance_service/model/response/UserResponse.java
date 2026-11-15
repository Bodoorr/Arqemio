package com.ga.finance_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserResponse {
    private Long id;
    private String email;
    private Boolean isPlatformAdmin;
    private Boolean isCustomer;
}
