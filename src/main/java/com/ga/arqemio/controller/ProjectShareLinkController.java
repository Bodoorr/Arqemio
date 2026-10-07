package com.ga.arqemio.controller;

import com.ga.arqemio.model.request.ProjectUpdateShareRequest;
import com.ga.arqemio.model.response.ProjectUpdateShareResponse;
import com.ga.arqemio.service.ProjectShareLinkService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/project-share")
public class ProjectShareLinkController {
    private ProjectShareLinkService projectShareLinkService;

    @PostMapping("/{updateId}")
    public ResponseEntity<String> shareProjectUpdate(@PathVariable Long updateId, @RequestBody ProjectUpdateShareRequest projectUpdateShareRequest){
        projectShareLinkService.shareProjectUpdate(updateId,projectUpdateShareRequest);
        return ResponseEntity.ok("Project update shared successfully");
    }

    @GetMapping("/customer/{token}")
    public ResponseEntity<ProjectUpdateShareResponse> getSharedProjectUpdate(@PathVariable String token){
        return ResponseEntity.ok(projectShareLinkService.getSharedProjectUpdate(token));
    }
}
