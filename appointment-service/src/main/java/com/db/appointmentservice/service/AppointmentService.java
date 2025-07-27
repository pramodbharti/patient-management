package com.db.appointmentservice.service;

import com.db.appointmentservice.dto.AppointmentResponseDto;
import com.db.appointmentservice.entity.CachedPatient;
import com.db.appointmentservice.repository.AppointmentRepository;
import com.db.appointmentservice.repository.CachedPatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final CachedPatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, CachedPatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    public List<AppointmentResponseDto> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByStartTimeBetween(startDate, endDate).stream()
                .map(appointment -> {

                    String name = patientRepository.findById(appointment.getPatientId())
                            .map(CachedPatient::getFullName)
                            .orElse("Unknown");

                    AppointmentResponseDto appointmentResponseDto = new AppointmentResponseDto();

                    appointmentResponseDto.setId(appointment.getId());
                    appointmentResponseDto.setPatientId(appointment.getPatientId());
                    appointmentResponseDto.setPatientName(name);
                    appointmentResponseDto.setStartTime(appointment.getStartTime());
                    appointmentResponseDto.setEndTime(appointment.getEndTime());
                    appointmentResponseDto.setReason(appointment.getReason());
                    appointmentResponseDto.setVersion(appointment.getVersion());
                    return appointmentResponseDto;
                }).toList();
    }
}
