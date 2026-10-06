package com.ga.arqemio.controller;

import com.ga.arqemio.model.Reservation;
import com.ga.arqemio.model.User;
import com.ga.arqemio.model.request.ReservationRequest;
import com.ga.arqemio.model.response.ReservationResponse;
import com.ga.arqemio.security.MyUserDetails;
import com.ga.arqemio.service.ReservationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company/reservations")
@AllArgsConstructor
public class ReservationController {
    private ReservationService reservationService;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest reservationRequest){
        Reservation reservation= reservationService.createReservation(reservationRequest);
        ReservationResponse reservationResponse=new ReservationResponse(
                reservation.getId(),
                reservation.getEquipment().getId(),
                reservation.getEquipment().getName(),
                reservation.getProject().getId(),
                reservation.getProject().getName(),
                reservation.getMembership().getId(),
                reservation.getMembership().getUser().getName(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getDescription(),
                reservation.getStatus(),
                reservation.getQuantity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponse);
    }



}
