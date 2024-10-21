/*
 * *
 *  * Created by Kolawole Omirin
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/09/2021, 12:39 PM
 *
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.payrail.nddc_assessment.integration.service.awsemail;


import co.payrail.nddc_assessment.integration.termii.model.Email;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
public class EmailSenderService {

    ExecutorService mes = Executors.newFixedThreadPool(1);

    AWSEmailService awsEmailService;

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    MessageSource messageSource;

    Locale locale = LocaleContextHolder.getLocale();

    @Value("${aws.sender}")
    private String FROM;
    @Value("${aws.sender.name}")
    private String FROMNAME;
    @Value("${aws.server}")
    private String SMTP_SERVER;
    @Value("${aws.port}")
    private String SMTP_SERVER_PORT;
    @Value("${aws.username}")
    private String USERNAME;
    @Value("${aws.password}")
    private String PASSWORD;
    @Value("${email.provider}")
    private String PROVIDER;


    private static final Logger logger = LogManager.getLogger(EmailSenderService.class);

    
    public  void sendAWSHTMLEmail(String subject, List<String> rlist, String html_msg){
        awsEmailService = new AWSEmailService(subject, rlist, html_msg,FROM, FROMNAME,SMTP_SERVER,SMTP_SERVER_PORT,USERNAME,PASSWORD,PROVIDER);
        mes.execute(awsEmailService);
    }
    
    public void sendAWSHTMLEmails(String subject, List<String> rlist, List<String> cclist, String html_msg){
        awsEmailService = new AWSEmailService(subject, rlist, cclist, html_msg,FROM, FROMNAME,SMTP_SERVER,SMTP_SERVER_PORT,USERNAME,PASSWORD,PROVIDER);
        mes.execute(awsEmailService);
    }
    
    public void sendAWSHTMLEmail(String subject, List<String> rlist, List<String> cclist, List<String> bcclist, String html_msg){
        awsEmailService = new AWSEmailService(subject, rlist, cclist, bcclist, html_msg,FROM, FROMNAME,SMTP_SERVER,SMTP_SERVER_PORT,USERNAME,PASSWORD,PROVIDER);
        mes.execute(awsEmailService);
    }

    public String sendTestEmail(){
        List<String> email = new ArrayList<>();
        Context context = new Context();
        context.setVariable("fullName", "Kolawole Omirin");
        context.setVariable("username", "kelux007");
        context.setVariable("password", "0000");
        context.setVariable("adminUrl", "payrail.co");

        Email emails = new Email.Builder()
                .setRecipient("kelux007@gmail.com")
                .setSubject(messageSource.getMessage("user.creation.email.subject", null, locale))
                .setTemplate("mail/usercreation")
                .build();
        String messageBody = templateEngine.process(emails.getTemplate(), context);
        email.add("kelux007@gmail.com");
        email.add("wsowunmi@plethub.com");
        sendAWSHTMLEmail("Testing Email", email, messageBody);
        return "done";
    }


}
