package com.ga.arqemio.service;


import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.EmailDetails;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.*;
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
import org.springframework.security.core.AuthenticationException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CompanyMembershipRepository companyMembershipRepository;
    private final FileAttachmentService fileAttachmentService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private static final Logger logger= Logger.getLogger(UserService.class.getName());

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, JWTUtils jwtUtils, @Lazy AuthenticationManager authenticationManager, CompanyMembershipRepository companyMembershipRepository, FileAttachmentService fileAttachmentService, EmailService emailService, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.companyMembershipRepository = companyMembershipRepository;
        this.fileAttachmentService = fileAttachmentService;
        this.emailService= emailService;
        this.auditLogService= auditLogService;
    }

    public User createUser(User userObject) {
        if (!userRepository.existsByEmail(userObject.getEmail())) {
            userObject.setPassword(passwordEncoder.encode(userObject.getPassword()));
            User savedUser= userRepository.save(userObject);
            logger.log(Level.INFO,"User {0} registered successfully", savedUser.getId());
            return savedUser;
        } else {
            logger.log(Level.WARNING,"Registration rejected: email already exists");
            throw new RuntimeException("User with email address " + userObject.getEmail() + " already" + " exists.");
        }
    }

    public User registerCustomer(String name, String email, String password) {
        User customer = new User();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPassword(password);
        customer.setStatus(true);
        customer.setIsPlatformAdmin(false);
        customer.setIsCustomer(true);

        User savedCustomer= createUser(customer);
        auditLogService.createAuditLog(savedCustomer,null,"REGISTER_CUSTOMER","USER", savedCustomer.getId(), "Customer registered: "+savedCustomer.getName());
        return savedCustomer;
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
            final String JWT = jwtUtils.generateJwtToken(myUserDetails);
            logger.log(Level.INFO,"User {0} logged in successfully", myUserDetails.getUser().getId());
            return ResponseEntity.ok(new LoginResponse("Login successful", JWT));
        } catch (AuthenticationException e) {
            logger.log(Level.WARNING, "Login failed: invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Error: username or email is incorrect.", null));
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
            logger.log(Level.WARNING,"User {0} provided an incorrect current password", currentUser.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,("Current password is incorrect."));
        }

        currentUser.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(currentUser);
        logger.log(Level.INFO,"Password changed successfully for user {0}", currentUser.getId());

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
        logger.log(Level.INFO,"Password reset email sent for user {0}",user.getId());
        return "Password reset email sent successfully.";
    }

    public String resetPassword(ResetPasswordRequest resetPasswordRequest){
        User user= userRepository.findByResetPasswordToken(resetPasswordRequest.getToken()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset password token."));

        if (user.getResetPasswordTokenExpiresAt().isBefore(LocalDateTime.now())){
            logger.log(Level.WARNING,"Expired password reset token used for user {0}", user.getId());
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
        logger.log(Level.INFO,"Password reset successfully for user {0}",user.getId());

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
            membershipResponses.add(toMembershipResponse(membership));
        }

        return toProfileResponse(currentUser, membershipResponses);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest updateProfileRequest) {
        User currentUser = getCurrentLoggedInUser();
        currentUser.setMobileNumber(updateProfileRequest.getMobileNumber());

        userRepository.save(currentUser);
        logger.log(Level.INFO,"User {0} updated their profile", currentUser.getId());
        return getProfile();
    }

    public UserProfileResponse updateEmployeeProfile(Long companyId, Long employeeId, String name, String mobileNumber, String email, MultipartFile profilePicture) throws IOException {
        User currentUser = getCurrentLoggedInUser();
        boolean isOwner = companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), companyId, "OWNER", "ACTIVE");
        if (!isOwner) {
            logger.log(Level.WARNING, "User {0} attempted to update employee {1} in company {2} without permissions", new Object[]{currentUser.getId(), employeeId, companyId});
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
        logger.log(Level.INFO, "Employee {0} updated by user {1} in company {2}", new Object[]{employeeId, currentUser.getId(), companyId});

        List<UserMembershipResponse> membershipResponses = new ArrayList<>();
        membershipResponses.add(toMembershipResponse(employeeMembership));

        return toProfileResponse(employee, membershipResponses);
    }

    public List<UserProfileResponse> getEmployeeProfiles(Long companyId){
        User currentUser= getCurrentLoggedInUser();
        boolean isOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), companyId, "OWNER","ACTIVE");
        if (!isOwner){
            logger.log(Level.WARNING,"User {0} attempted to view employees in company {1} without permission", new Object[]{currentUser.getId(), companyId});
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view employees in this company.");
        }

        List<CompanyMembership> memberships= companyMembershipRepository.findByCompanyIdAndStatus(companyId,"ACTIVE");
        List<UserProfileResponse> employeeProfiles= new ArrayList<>();

        for (CompanyMembership membership: memberships){
            User employee= membership.getUser();
            List<UserMembershipResponse> membershipResponses= new ArrayList<>();
            membershipResponses.add(toMembershipResponse(membership));
            employeeProfiles.add(toProfileResponse(employee, membershipResponses));
        }
        return employeeProfiles;
    }

    private UserMembershipResponse toMembershipResponse(CompanyMembership membership){
        return new UserMembershipResponse(
                membership.getId(),
                membership.getCompany().getId(),
                membership.getCompany().getName(),
                membership.getRole(),
                membership.getStatus()
        );
    }

    private UserProfileResponse toProfileResponse(User user, List<UserMembershipResponse> membershipResponses){
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getProfilePicture(),
                membershipResponses,
                user.getIsCustomer()
        );
    }
}

