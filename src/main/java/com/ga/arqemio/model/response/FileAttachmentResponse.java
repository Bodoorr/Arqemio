package com.ga.arqemio.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileAttachmentResponse {
    private Long id;
    private Long projectId;
    private Long expenseId;
    private Long projectUpdateId;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long uploadedByMembershipId;
    private String uploadedByName;
}
