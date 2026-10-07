package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProjectUpdateShareResponse {
    private String companyName;
    private String projectName;
    private String updateTitle;
    private String updateDescription;
    private LocalDateTime updateDate;
    private List<String> images;
    private LocalDateTime expiresAt;

}
