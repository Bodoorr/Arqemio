package com.ga.arqemio.service;

import com.ga.arqemio.model.*;
import com.ga.arqemio.model.request.ReservationRequest;
import com.ga.arqemio.repository.CompanyMembershipRepository;
import com.ga.arqemio.repository.EquipmentRepository;
import com.ga.arqemio.repository.ProjectRepository;
import com.ga.arqemio.repository.ReservationRepository;
import com.ga.arqemio.security.MyUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {
    private ReservationRepository reservationRepository;
    private EquipmentRepository equipmentRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Reservation createReservation(ReservationRequest reservationRequest){
        User currentUser= getCurrentLoggedInUser();
        Equipment equipment= equipmentRepository.findById(reservationRequest.getEquipmentId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found."));
        Project project= projectRepository.findById(reservationRequest.getProjectId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(), equipment.getCompany().getId(), "OWNER","ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), currentUser.getId(), "ACTIVE");

        if (!equipment.getCompany().getId().equals(project.getCompany().getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Equipment and project must belong to the same company.");
        }

        if (!isCompanyOwner && !isAssignedManager){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You're not allowed to reserve equipment for this project.");
        }

        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(), equipment.getCompany().getId(), "ACTIVE")
                .orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN, "Active company membership is not found."));

        if (!equipment.getStatus().equals("AVAILABLE")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment is not available for reservation.");
        }

        if (reservationRequest.getQuantity()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation quantity must be greater than 0.");
        }

        if (reservationRequest.getEndDateTime().isBefore(reservationRequest.getStartDateTime()) || reservationRequest.getEndDateTime().equals(reservationRequest.getStartDateTime())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date and time must be after start date and time.");
        }


        //Reservation Logic
        List<Reservation> existingReservations= reservationRepository.findByEquipmentId(equipment.getId());

        int reservedQuantity=0;

        for (Reservation existingReservation: existingReservations){
            if (existingReservation.getStatus().equals("RESERVED") || existingReservation.getStatus().equals("IN_USE")){
                boolean overlaps= reservationRequest.getStartDateTime().isBefore(existingReservation.getEndDateTime()) &&
                        reservationRequest.getEndDateTime().isAfter(existingReservation.getStartDateTime());
                if (overlaps){
                    reservedQuantity += existingReservation.getQuantity();
                }
            }
        }

        int availableQuantity= equipment.getQuantity()-reservedQuantity;
        if (reservationRequest.getQuantity()> availableQuantity){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough equipment available for the selected time.");
        }

        Reservation reservation= new Reservation();
        reservation.setEquipment(equipment);
        reservation.setProject(project);
        reservation.setMembership(membership);
        reservation.setStartDateTime(reservationRequest.getStartDateTime());
        reservation.setEndDateTime(reservationRequest.getEndDateTime());
        reservation.setDescription(reservationRequest.getDescription());
        reservation.setQuantity(reservationRequest.getQuantity());
        reservation.setStatus("RESERVED");

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservation(){
        User currentUser= getCurrentLoggedInUser();
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin().equals(true);
        if (isPlatformAdmin){
            return reservationRepository.findAll();
        }
        return reservationRepository.findByProjectCompanyMembershipsUserIdAndProjectCompanyMembershipsStatus(currentUser.getId(), "ACTIVE");
    }

    public Reservation getReservationById(Long reservationId){
        User currentUser=getCurrentLoggedInUser();
        Reservation reservation= reservationRepository.findById(reservationId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found."));
        boolean isPlatformAdmin= currentUser.getIsPlatformAdmin();
        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), reservation.getProject().getCompany().getId(), "ACTIVE");

        if (!isActiveMember && !isPlatformAdmin){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this reservation.");
        }

        return reservation;
    }

    public Reservation updateReservation(Long reservationId, ReservationRequest reservationRequest){
        User currentUser= getCurrentLoggedInUser();
        Reservation reservation= reservationRepository.findById(reservationId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found."));
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(),reservation.getProject().getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(reservation.getProject().getId(), currentUser.getId(), "ACTIVE");

        if (!isCompanyOwner && !isAssignedManager){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this reservation.");
        }

        if (reservationRequest.getQuantity()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation quantity must be greater than 0.");
        }

        if (reservationRequest.getEndDateTime().isBefore(reservationRequest.getStartDateTime()) || reservationRequest.getEndDateTime().equals(reservationRequest.getStartDateTime())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date and time must be after start date and time.");
        }

        List<Reservation> existingReservations= reservationRepository.findByEquipmentId(reservation.getEquipment().getId());
        int reservedQuantity=0;
        for (Reservation exiatingReservation : existingReservations){
            if (!exiatingReservation.getId().equals(reservation.getId())){
                if (exiatingReservation.getStatus().equals("RESERVED") || exiatingReservation.getStatus().equals("IN_USE")){
                    boolean overlaps= reservationRequest.getStartDateTime().isBefore(exiatingReservation.getEndDateTime()) &&
                            reservationRequest.getEndDateTime().isAfter(exiatingReservation.getStartDateTime());

                    if (overlaps){
                        reservedQuantity+=exiatingReservation.getQuantity();
                    }
                }
            }
        }
        int availableQuantity= reservation.getEquipment().getQuantity() - reservedQuantity;

        if (reservationRequest.getQuantity() > availableQuantity){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough equipment available for the selected time.");
        }

        reservation.setStartDateTime(reservationRequest.getStartDateTime());
        reservation.setEndDateTime(reservationRequest.getEndDateTime());
        reservation.setDescription(reservationRequest.getDescription());
        reservation.setQuantity(reservationRequest.getQuantity());

        return reservationRepository.save(reservation);
    }

    public Reservation cancelReservation(Long reservationId){
        User currentUser= getCurrentLoggedInUser();
        Reservation reservation= reservationRepository.findById(reservationId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found."));
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(currentUser.getId(),reservation.getProject().getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(reservation.getProject().getId(), currentUser.getId(), "ACTIVE");

        if (!isCompanyOwner && !isAssignedManager){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to cancel this reservation.");
        }

        if (!reservation.getStatus().equals("RESERVED")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only reserved reservations can be cancelled.");
        }

        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }
}
