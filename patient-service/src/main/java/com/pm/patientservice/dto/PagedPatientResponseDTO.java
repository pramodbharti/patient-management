package com.pm.patientservice.dto;

import java.util.List;

public class PagedPatientResponseDTO {
    private List<PatientResponseDTO> patients;
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;

    public PagedPatientResponseDTO(List<PatientResponseDTO> patients, int page, int size, int totalElements, int totalPages) {
        this.patients = patients;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public PagedPatientResponseDTO() {
    }

    public List<PatientResponseDTO> getPatients() {
        return patients;
    }

    public void setPatients(List<PatientResponseDTO> patients) {
        this.patients = patients;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
