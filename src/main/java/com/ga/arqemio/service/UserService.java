package com.ga.arqemio.service;


import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.LoginRequest;
import com.ga.arqemio.model.request.UpdateProfileRequest;
import com.ga.arqemio.model.response.LoginResponse;
import com.ga.arqemio.model.response.UserMembershipResponse;
import com.ga.arqemio.model.response.UserProfileResponse;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.UserRepository;
import com.ga.arqemio.security.JWTUtils;
import com.ga.arqemio.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private CompanyMembershipRepository companyMembershipRepository;
    private FileAttachmentService fileAttachmentService;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, JWTUtils jwtUtils, @Lazy AuthenticationManager authenticationManager, CompanyMembershipRepository companyMembershipRepository, FileAttachmentService fileAttachmentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.companyMembershipRepository = companyMembershipRepository;
        this.fileAttachmentService = fileAttachmentService;
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

    public UserProfileResponse getProfile() {
        User currentUser = getCurrentLoggedInUser();
        List<UserMembershipResponse> membershipResponses = new ArrayList<>();

        for (CompanyMembership membership : currentUser.getMemberships()) {
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
        if (!employee.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }
        employee.setEmail(email);
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

