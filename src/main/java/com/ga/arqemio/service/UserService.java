package com.ga.arqemio.service;


import com.ga.arqemio.model.CompanyMembership;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.LoginRequest;
import com.ga.arqemio.model.response.LoginResponse;
import com.ga.arqemio.model.response.UserMembershipResponse;
import com.ga.arqemio.model.response.UserProfileResponse;
import com.ga.arqemio.repository.UserRepository;
import com.ga.arqemio.security.JWTUtils;
import com.ga.arqemio.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, JWTUtils jwtUtils, @Lazy AuthenticationManager authenticationManager){
        this.userRepository= userRepository;
        this.passwordEncoder= passwordEncoder;
        this.jwtUtils= jwtUtils;
        this.authenticationManager= authenticationManager;
    }

    public User createUser(User userObject){
        System.out.println("Service Calling createUser ==>");
        if (!userRepository.existsByEmail(userObject.getEmail())){
            userObject.setPassword(passwordEncoder.encode(userObject.getPassword()));
            return userRepository.save(userObject);
        } else{
            throw new RuntimeException("User with email address "+userObject.getEmail()+" already"+" exists.");
        }
    }

    public User findUserByEmail(String email){
        return userRepository.findUserByEmail(email).orElseThrow(()->new RuntimeException("User not found."));
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest){
        try {
            Authentication authentication= authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            MyUserDetails myUserDetails=(MyUserDetails) authentication.getPrincipal();
            final String JWT= jwtUtils.generateJwtToken(myUserDetails);
            return ResponseEntity.ok(new LoginResponse("Login successful",JWT));
        }
        catch (Exception e){
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

        UserProfileResponse userProfileResponse=new UserProfileResponse(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getEmail(),
                currentUser.getMobileNumber(),
                currentUser.getProfilePicture(),
                membershipResponses
        );

        return userProfileResponse;
    }

}

