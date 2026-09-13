package com.carex.dto.intelligence;

public class WaitTimeResponse {

    private Long appointmentId;
    private int predictedMinutes;
    private String method;
    private String disclaimer;

    public WaitTimeResponse() {}

    public WaitTimeResponse(Long appointmentId, int predictedMinutes, String method, String disclaimer) {
        this.appointmentId = appointmentId;
        this.predictedMinutes = predictedMinutes;
        this.method = method;
        this.disclaimer = disclaimer;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPredictedMinutes() {
        return predictedMinutes;
    }

    public void setPredictedMinutes(int predictedMinutes) {
        this.predictedMinutes = predictedMinutes;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
