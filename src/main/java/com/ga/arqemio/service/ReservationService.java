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

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {
    private ReservationRepository reservationRepository;
    private EquipmentRepository equipmentRepository;
    private ProjectRepository projectRepository;
    private CompanyMembershipRepository companyMembershipRepository;
    private AuditLogService auditLogService;

    public static User getCurrentLoggedInUser(){
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    public Reservation createReservation(ReservationRequest reservationRequest){
        User currentUser= getCurrentLoggedInUser();
        Equipment equipment= equipmentRepository.findById(reservationRequest.getEquipmentId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found."));
        Project project= projectRepository.findById(reservationRequest.getProjectId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));

        if (!equipment.getCompany().getId().equals(project.getCompany().getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Equipment and project must belong to the same company.");
        }

        if (!canManageReservation(currentUser, project)){
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

        validateReservationDates(reservationRequest.getStartDateTime(),reservationRequest.getEndDateTime());

        int availableQuantity= calculateAvailableQuantity(equipment, reservationRequest.getStartDateTime(), reservationRequest.getEndDateTime());

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

        Reservation savedReservation = reservationRepository.save(reservation);
        auditLogService.createAuditLog(currentUser, project.getCompany(),
                "CREATE", "RESERVATION", savedReservation.getId(),
                "Created equipment reservation for " + equipment.getName()
        );

        return savedReservation;
    }

    public List<Reservation> getAllReservation(){
        User currentUser= getCurrentLoggedInUser();
        return reservationRepository.findByProjectCompanyMembershipsUserIdAndProjectCompanyMembershipsStatus(currentUser.getId(), "ACTIVE");
    }

    public Reservation getReservationById(Long reservationId){
        User currentUser=getCurrentLoggedInUser();
        Reservation reservation= findReservationById(reservationId);

        boolean isActiveMember= companyMembershipRepository.existsByUserIdAndCompanyIdAndStatus(currentUser.getId(), reservation.getProject().getCompany().getId(), "ACTIVE");
        if (!isActiveMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to view this reservation.");
        }

        return reservation;
    }

    public Reservation updateReservation(Long reservationId, ReservationRequest reservationRequest){
        User currentUser= getCurrentLoggedInUser();
        Reservation reservation= findReservationById(reservationId);

        if (!canManageReservation(currentUser, reservation.getProject())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this reservation.");
        }

        if (reservationRequest.getQuantity()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation quantity must be greater than 0.");
        }

        validateReservationDates(reservationRequest.getStartDateTime(), reservationRequest.getEndDateTime());

        List<Reservation> existingReservations= reservationRepository.findByEquipmentId(reservation.getEquipment().getId());
        int reservedQuantity=0;
        for (Reservation existingReservation : existingReservations){
            if (!existingReservation.getId().equals(reservation.getId())){
                if (existingReservation.getStatus().equals("RESERVED")){
                    boolean overlaps= reservationRequest.getStartDateTime().isBefore(existingReservation.getEndDateTime()) &&
                            reservationRequest.getEndDateTime().isAfter(existingReservation.getStartDateTime());

                    if (overlaps){
                        reservedQuantity+=existingReservation.getQuantity();
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
        Reservation reservation= findReservationById(reservationId);

        if (!canManageReservation(currentUser, reservation.getProject())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to cancel this reservation.");
        }

        if (!reservation.getStatus().equals("RESERVED")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only reserved reservations can be cancelled.");
        }

        reservation.setStatus("CANCELLED");

        Reservation savedReservation = reservationRepository.save(reservation);
        auditLogService.createAuditLog(currentUser, reservation.getProject().getCompany(),
                "CANCEL", "RESERVATION", savedReservation.getId(),
                "Cancelled equipment reservation for " + reservation.getEquipment().getName()
        );

        return savedReservation;
    }

    public Reservation updateReservationStatus(Long reservationId, String status){
        User currentUser= getCurrentLoggedInUser();
        Reservation reservation= findReservationById(reservationId);
        CompanyMembership membership= companyMembershipRepository.findByUserIdAndCompanyIdAndStatus(currentUser.getId(), reservation.getProject().getCompany().getId(), "ACTIVE").orElseThrow(()-> new ResponseStatusException(HttpStatus.FORBIDDEN, "Active company membership not found."));
        boolean isAssignedWorker= projectRepository.existsByIdAndWorkersId(reservation.getProject().getId(), membership.getId());

        if (!isAssignedWorker || !membership.getRole().equals("WORKER")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You're not allowed to update this reservation status.");
        }

        if (!reservation.getStatus().equals("RESERVED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reservation can no longer be updated.");
        }

        if (!status.equals("COMPLETED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker can only mark a reservation as COMPLETED.");
        }

        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }

    public int getAvailableQuantity(Long equipmentId, LocalDateTime startDateTime, LocalDateTime endDateTime){
        Equipment equipment= equipmentRepository.findById(equipmentId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found."));
        validateReservationDates(startDateTime,endDateTime);

        return calculateAvailableQuantity(equipment,startDateTime,endDateTime);
    }

    private boolean canManageReservation(User user, Project project){
        boolean isCompanyOwner= companyMembershipRepository.existsByUserIdAndCompanyIdAndRoleAndStatus(user.getId(),project.getCompany().getId(), "OWNER", "ACTIVE");
        boolean isAssignedManager= projectRepository.existsByIdAndManagersUserIdAndManagersStatus(project.getId(), user.getId(), "ACTIVE");

        return isCompanyOwner || isAssignedManager;
    }

    private Reservation findReservationById(Long reservationId){
        return reservationRepository.findById(reservationId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found."));
    }

    private void validateReservationDates(LocalDateTime startDateTime, LocalDateTime endDateTime){
        if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date and time must be after start date and time.");
        }
    }

    private int calculateAvailableQuantity(Equipment equipment, LocalDateTime startDateTime, LocalDateTime endDateTime){
        //Reservation Logic
        List<Reservation> existingReservations= reservationRepository.findByEquipmentId(equipment.getId());
        int reservedQuantity=0;

        for (Reservation existingReservation: existingReservations){
            if (existingReservation.getStatus().equals("RESERVED")){
                boolean overlaps= startDateTime.isBefore(existingReservation.getEndDateTime()) &&
                        endDateTime.isAfter(existingReservation.getStartDateTime());
                if (overlaps){
                    reservedQuantity += existingReservation.getQuantity();
                }
            }
        }
    return equipment.getQuantity() - reservedQuantity;
    }

}
