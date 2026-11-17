package com.ga.auth_service.service;


import com.ga.auth_service.client.AuditLogClient;
import com.ga.auth_service.client.CompanyClient;
import com.ga.auth_service.model.EmailDetails;
import com.ga.auth_service.model.User;
import com.ga.auth_service.model.request.*;
import com.ga.auth_service.model.response.LoginResponse;
import com.ga.auth_service.model.response.UserMembershipResponse;
import com.ga.auth_service.model.response.UserProfileResponse;
import com.ga.auth_service.repository.UserRepository;
import com.ga.auth_service.security.JWTUtils;
import com.ga.auth_service.security.MyUserDetails;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final CompanyClient companyClient;
    private AuditLogClient auditLogClient;
    private static final Logger logger= Logger.getLogger(UserService.class.getName());

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, JWTUtils jwtUtils, @Lazy AuthenticationManager authenticationManager, EmailService emailService, CompanyClient companyClient, AuditLogClient auditLogClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.emailService= emailService;
        this.companyClient= companyClient;
        this.auditLogClient= auditLogClient;
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
        auditLogClient.createAuditLog(savedCustomer.getId(),null,"REGISTER_CUSTOMER","USER", savedCustomer.getId(), "Customer registered: "+savedCustomer.getName());
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

    public UserProfileResponse getProfile(String token) {
        User currentUser = getCurrentLoggedInUser();
        UserMembershipResponse[] memberships= companyClient.getMembershipByUserId(currentUser.getId(), token);
        List<UserMembershipResponse> membershipResponses= new ArrayList<>();

        if (memberships!=null){
            for (UserMembershipResponse membership : memberships){
                membershipResponses.add(membership);
            }
        }
        return toProfileResponse(currentUser, membershipResponses);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest updateProfileRequest, String token) {
        User currentUser = getCurrentLoggedInUser();
        currentUser.setMobileNumber(updateProfileRequest.getMobileNumber());

        userRepository.save(currentUser);
        auditLogClient.createAuditLog(currentUser.getId(), null, "UPDATE_PROFILE", "USER", currentUser.getId(), "User updated their profile");
        logger.log(Level.INFO,"User {0} updated their profile", currentUser.getId());
        return getProfile(token);
    }

    public UserProfileResponse updateEmployee(Long employeeId, String name, String mobileNumber, String email, String profilePictureUrl){
        User employee= getUserById(employeeId);
        employee.setName(name);
        employee.setMobileNumber(mobileNumber);

        if (email !=null && !email.isBlank()){
            if (!employee.getEmail().equals(email) && userRepository.existsByEmail(email)){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
            }
            employee.setEmail(email);
        }

        if (profilePictureUrl!=null){
            employee.setProfilePicture(profilePictureUrl);
        }
        userRepository.save(employee);
        logger.log(Level.INFO, "Employee {0} profile updated successfully", employeeId);

        return toProfileResponse(employee, new ArrayList<>());
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
    public User getUserById(Long id){
        return userRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found."));
    }
}

