package com.ga.arqemio.service;


import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.EmailDetails;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.ForgetPasswordRequest;
import com.ga.arqemio.model.request.LoginRequest;
import com.ga.arqemio.model.request.ResetPasswordRequest;
import com.ga.arqemio.model.request.UpdateProfileRequest;
import com.ga.arqemio.model.response.ChangePasswordRequest;
import com.ga.arqemio.model.response.LoginResponse;
import com.ga.arqemio.model.response.UserMembershipResponse;
import com.ga.arqemio.model.response.UserProfileResponse;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.UserRepository;
import com.ga.arqemio.security.JWTUtils;
import com.ga.arqemio.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CompanyMembershipRepository companyMembershipRepository;
    private final FileAttachmentService fileAttachmentService;
    private EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, JWTUtils jwtUtils, @Lazy AuthenticationManager authenticationManager, CompanyMembershipRepository companyMembershipRepository, FileAttachmentService fileAttachmentService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.companyMembershipRepository = companyMembershipRepository;
        this.fileAttachmentService = fileAttachmentService;
        this.emailService= emailService;
    }

    public User createUser(User userObject) {
        System.out.println("Service Calling createUser ==>");
        if (!userRepository.existsByEmail(userObject.getEmail())) {
            userObject.setPassword(passwordEncoder.encode(userObject.getPassword()));
            return userRepository.save(userObject);
        } else {
            throw new RuntimeException("User with email address " + userObject.getEmail() + " already" + " exists.");
        }
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found."));
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
            final String JWT = jwtUtils.generateJwtToken(myUserDetails);
            return ResponseEntity.ok(new LoginResponse("Login successful", JWT));
        } catch (Exception e) {
            return ResponseEntity.ok(new LoginResponse("Error: username or email is incorrect.", null));
        }
    }

    public User getCurrentLoggedInUser() {
        MyUserDetails myUserDetails =
                (MyUserDetails) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        return myUserDetails.getUser();
    }

    public String changePassword(ChangePasswordRequest changePasswordRequest){
        User currentUser= getCurrentLoggedInUser();

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,("Current password is incorrect."));
        }

        if (changePasswordRequest.getNewPassword() == null || changePasswordRequest.getNewPassword().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"New password cannot be empty.");
        }

        currentUser.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(currentUser);

        EmailDetails emailDetails = new EmailDetails(
                currentUser.getEmail(),
                "Hello " + currentUser.getName() +
                        ",\n\nYour Arqemio password has been changed successfully." +
                        "\n\nIf you did not make this change, please reset your password immediately.",
                "Arqemio - Password Changed",
                null
        );

        emailService.sendSimpleMail(emailDetails);

        return "Password changed successfully.";
    }

    public String getPasswordResetEmail(String resetLink) {

        try {
            ClassPathResource resource =
                    new ClassPathResource("templates/password-reset-email.html");

            String html = new String(
                    resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            html = html.replace("{{resetLink}}", resetLink);
            return html;

        } catch (IOException e) {
            throw new RuntimeException("Could not load password reset email.");
        }
    }

    public String forgetPassword(ForgetPasswordRequest forgetPasswordRequest){
        User user= userRepository.findUserByEmail(forgetPasswordRequest.getEmail()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        String token= UUID.randomUUID().toString();

        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        String resetLink = "http://localhost:5173/reset-password/" + token;

        String emailBody= getPasswordResetEmail(resetLink);

        EmailDetails emailDetails = new EmailDetails(
                user.getEmail(),
                emailBody,
                "Arqemio - Reset Your Password",
                null
        );

        emailService.sendHtmlMail(emailDetails);
        return "Password reset email sent successfully.";
    }

    public String resetPassword(ResetPasswordRequest resetPasswordRequest){
        User user= userRepository.findByResetPasswordToken(resetPasswordRequest.getToken()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset password token."));

        if (user.getResetPasswordTokenExpiresAt().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Reset password link has expired.");
        }
        if (resetPasswordRequest.getNewPassword() == null || resetPasswordRequest.getNewPassword().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"New password cannot be empty.");
        }
        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiresAt(null);

        userRepository.save(user);

        EmailDetails emailDetails = new EmailDetails(
                user.getEmail(),
                "Hello " + user.getName() +
                        ",\n\nYour Arqemio password has been reset successfully." +
                        "\n\nIf you did not make this change, please contact support.",
                "Arqemio - Password Reset Successful",
                null
        );

        emailService.sendSimpleMail(emailDetails);
        return "Password reset successfully.";
    }

    public UserProfileResponse getProfile() {
        User currentUser = getCurrentLoggedInUser();
        List<UserMembershipResponse> membershipResponses = new ArrayList<>();

        List<CompanyMembership> memberships = companyMembershipRepository.findByUserIdAndStatus(currentUser.getId(), "ACTIVE");

        for (CompanyMembership membership : memberships) {
            UserMembershipResponse membershipResponse = new UserMembershipResponse(
                    membership.getId(),
                    membership.getCompany().getId(),
                    membership.getCompany().getName(),
                    membership.getRole(),
                    membership.getStatus()
            );

            membershipResponses.add(membershipResponse);
        }

        UserProfileResponse userProfileResponse = new UserProfileResponse(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getEmail(),
                currentUser.getMobileNumber(),
                currentUser.getProfilePicture(),
                membershipResponses
        );

        return userProfileResponse;
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest updateProfileRequest) {
        User currentUser = getCurrentLoggedInUser();
        currentUser.setMobileNumber(updateProfileRequest.getMobileNumber());

        userRepository.save(currentUser);
        return getProfile();
    }

    public UserProfileResponse updateEmployeeProfile(Long companyId, Long employeeId, String name, String mobileNumber, String email, MultipartFile profilePicture) throws IOException {
        User currentUser = getCurrentLoggedInUser();
        boolean isOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), companyId, "OWNER", "ACTIVE");
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update employees in this company.");
        }

        CompanyMembership employeeMembership = companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(employeeId, companyId, "ACTIVE").
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found in this company."));

        User employee = employeeMembership.getUser();
        employee.setName(name);
        employee.setMobileNumber(mobileNumber);
        if (email!=null && !email.isBlank()){
        if (!employee.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }
            employee.setEmail(email);
        }
        if (profilePicture != null && !profilePicture.isEmpty()) {
            Map uploadResult = fileAttachmentService.uploadImage(profilePicture);
            employee.setProfilePicture(uploadResult.get("secure_url").toString());
        }
        userRepository.save(employee);

        List<UserMembershipResponse> membershipResponses = new ArrayList<>();
        UserMembershipResponse membershipResponse = new UserMembershipResponse(
                employeeMembership.getId(),
                employeeMembership.getCompany().getId(),
                employeeMembership.getCompany().getName(),
                employeeMembership.getRole(),
                employeeMembership.getStatus()
        );

        membershipResponses.add(membershipResponse);

        UserProfileResponse employeeProfile = new UserProfileResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getMobileNumber(),
                employee.getProfilePicture(),
                membershipResponses
        );
        return employeeProfile;
    }

    public List<UserProfileResponse> getEmployeeProfiles(Long companyId){
        User currentUser= getCurrentLoggedInUser();
        boolean isOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), companyId, "OWNER","ACTIVE");
        if (!isOwner){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view employees in this company.");
        }

        List<CompanyMembership> memberships= companyMembershipRepository.findByCompanyIdAndStatus(companyId,"ACTIVE");
        List<UserProfileResponse> employeeProfiles= new ArrayList<>();

        for (CompanyMembership membership: memberships){
            User employee= membership.getUser();
            List<UserMembershipResponse> membershipResponses= new ArrayList<>();
                    UserMembershipResponse membershipResponse=new UserMembershipResponse(
                    membership.getId(),
                    membership.getCompany().getId(),
                    membership.getCompany().getName(),
                    membership.getRole(),
                    membership.getStatus()
            );

            membershipResponses.add(membershipResponse);

            UserProfileResponse employeeProfile= new UserProfileResponse(
                    employee.getId(),
                    employee.getName(),
                    employee.getEmail(),
                    employee.getMobileNumber(),
                    employee.getProfilePicture(),
                    membershipResponses
            );
            employeeProfiles.add(employeeProfile);
        }
        return employeeProfiles;
    }

}

