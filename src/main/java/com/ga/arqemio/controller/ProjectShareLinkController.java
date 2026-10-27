package com.ga.arqemio.controller;

import com.ga.arqemio.model.request.CustomerRegistrationRequest;
import com.ga.arqemio.model.request.ProjectUpdateShareRequest;
import com.ga.arqemio.model.response.ProjectUpdateShareResponse;
import com.ga.arqemio.service.ProjectShareLinkService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/project-share")
public class ProjectShareLinkController {
    private ProjectShareLinkService projectShareLinkService;

    @PostMapping("/{updateId}")
    public ResponseEntity<String> shareProjectUpdate(@PathVariable Long updateId, @Valid @RequestBody ProjectUpdateShareRequest projectUpdateShareRequest){
        projectShareLinkService.shareProjectUpdate(updateId,projectUpdateShareRequest);
        return ResponseEntity.ok("Project update shared successfully");
    }

    @GetMapping("/customer/{token}")
    public ResponseEntity<ProjectUpdateShareResponse> getSharedProjectUpdate(@PathVariable String token){
        return ResponseEntity.ok(projectShareLinkService.getSharedProjectUpdate(token));
    }

    @GetMapping("/register/{token}")
    public ResponseEntity<String> getCustomerRegistrationEmail(@PathVariable String token){
        return ResponseEntity.ok(projectShareLinkService.getCustomerRegistrationEmail(token));
    }

    @PostMapping("/register/{token}")
    public ResponseEntity<String> registerCustomer(@PathVariable String token, @Valid @RequestBody CustomerRegistrationRequest customerRegistrationRequest){
        if (!customerRegistrationRequest.getPassword().equals(customerRegistrationRequest.getConfirmedPassword())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password do not match.");
        }
        projectShareLinkService.registerCustomer(token, customerRegistrationRequest.getName(), customerRegistrationRequest.getPassword());
        return ResponseEntity.ok("Customer registered successfully.");
    }
}
