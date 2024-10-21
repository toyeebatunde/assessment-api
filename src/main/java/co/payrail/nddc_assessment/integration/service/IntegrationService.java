package co.payrail.nddc_assessment.integration.service;

public interface IntegrationService {


    void sendSms(String phoneNumber , String message) throws  NullPointerException;


}
