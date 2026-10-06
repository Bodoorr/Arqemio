package com.ga.arqemio.service;

import com.ga.arqemio.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
@Service
@AllArgsConstructor
public class FileAttachmentService {
    private final Dotenv dotenv = Dotenv.load();
    private final Cloudinary cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    private FileAttachmentRepository fileAttachmentRepository;
    private ProjectRepository projectRepository;
    private ProjectUpdateRepository projectUpdateRepository;
    private ExpenseRepository expenseRepository;
    private  CompanyMembershipRepository companyMembershipRepository;
    public Map uploadImage(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image to upload");
        }

        if (file.getContentType() == null ||
                !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "arqemio",
                        "resource_type", "image",
                        "use_filename", true,
                        "unique_filename", true,
                        "overwrite", false
                )
        );

        return uploadResult;
    }

}
