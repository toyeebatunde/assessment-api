package co.payrail.nddc_assessment.integration.service;

import co.payrail.nddc_assessment.integration.termii.model.Email;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.context.Context;

import java.io.IOException;

public interface MailService {

    void sendGrid(Email emailInfo) throws IOException;

    void sendGridMail(Email email, Context context) throws IOException;

    @Async
    void sendMailgunMail(Email emailInfo, Context context) throws IOException;

    void sendAwsEmail(Email email, Context context) throws IOException;

    void sendGridwithSenderMail(Email emailInfo, Context context, String mailSender) throws IOException;

    void sendGridMailWithAttachment(Email emailInfo, Context context, String fileName, String fileLocation) throws IOException;




}

