package com.example.b07project.model;

public class Provider extends User{
    private String clinicName;
    private String specialty;

    public Provider(String id, String displayName, String clinicName) {
        super(id, displayName, UserRole.PROVIDER);
        this.clinicName = clinicName;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
