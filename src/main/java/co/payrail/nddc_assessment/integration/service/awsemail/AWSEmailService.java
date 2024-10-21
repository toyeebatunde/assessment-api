/*
 * *
 *  * Created by Kolawole Omirin
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 20/09/2021, 12:42 PM
 *
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.payrail.nddc_assessment.integration.service.awsemail;


import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Properties;


@Service
public class AWSEmailService implements Runnable {

    private String subject;
    private List<String> receiver;
    private List<String> cc;
    private List<String> bcc;
    private String html_message;

    private static final Logger logger = LogManager.getLogger(AWSEmailService.class);

    private String FROM;
    private String FROMNAME;
    private String SMTP_SERVER;
    private String SMTP_SERVER_PORT;
    private String USERNAME;
    private String PASSWORD;
    private String PROVIDER;

    public AWSEmailService() {
    }

    public AWSEmailService(String subject, List<String> receiver, String html_message,
                           String FROM, String FROMNAME, String SMTP_SERVER, String SMTP_SERVER_PORT, String USERNAME, String PASSWORD, String PROVIDER) {
        super();
        this.subject = subject;
        this.html_message = html_message;
        this.receiver = receiver;
        this.FROM = FROM;
        this.FROMNAME = FROMNAME;
        this.SMTP_SERVER = SMTP_SERVER;
        this.SMTP_SERVER_PORT = SMTP_SERVER_PORT;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.PROVIDER = PROVIDER;
    }

    AWSEmailService(String subject, List<String> receiver, List<String> cc, String html_message,
                    String FROM, String FROMNAME, String SMTP_SERVER, String SMTP_SERVER_PORT, String USERNAME, String PASSWORD, String PROVIDER){
        super();
        this.subject= subject;
        this.receiver = receiver;
        this.cc = cc;
        this.html_message = html_message;
        this.FROM = FROM;
        this.FROMNAME = FROMNAME;
        this.SMTP_SERVER = SMTP_SERVER;
        this.SMTP_SERVER_PORT = SMTP_SERVER_PORT;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.PROVIDER = PROVIDER;
    }

    AWSEmailService(String subject, List<String> receiver, List<String> cc, List<String> bc, String html_message,
                    String FROM, String FROMNAME, String SMTP_SERVER, String SMTP_SERVER_PORT, String USERNAME, String PASSWORD, String PROVIDER){
        super();
        this.subject= subject;
        this.receiver = receiver;
        this.cc = cc;
        this.bcc = bc;
        this.html_message = html_message;
        this.FROM = FROM;
        this.FROMNAME = FROMNAME;
        this.SMTP_SERVER = SMTP_SERVER;
        this.SMTP_SERVER_PORT = SMTP_SERVER_PORT;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.PROVIDER = PROVIDER;
    }

    public void sendSESEmail() {
    	Properties props = System.getProperties();
    	props.put("mail.transport.protocol", "smtp");
    	props.put("mail.smtp.port", this.SMTP_SERVER_PORT);
    	props.put("mail.smtp.starttls.enable", "true");
    	props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
    	Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(this.FROM, this.FROMNAME));

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(StringUtils.join(receiver, ","), false));

            if(!Objects.isNull(cc)){
                // cc
                if(cc.size() > 0){
                   msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(StringUtils.join(cc, ","), false));
                }
            }

            if(!Objects.isNull(bcc)){
                // bcc
                if(bcc.size() > 0){
                   msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(StringUtils.join(bcc, ","), false));
                }
            }

            msg.setSubject(this.subject);

            msg.setContent(this.html_message, "text/html");

            // Create a transport.
            Transport transport = session.getTransport();

            // Send the message.
            try
            {
                transport.connect(this.SMTP_SERVER, this.USERNAME, this.PASSWORD);

                // Send the email.
                transport.sendMessage(msg, msg.getAllRecipients());
                logger.log(Level.INFO, "Email sent via "+this.PROVIDER+" successfully");
            }
            catch (Exception ex) {
                logger.log(Level.INFO, this.PROVIDER+" Error message: " + ex.getMessage());
            }
            finally
            {
                // Close and terminate the connection.
                transport.close();
            }

        } catch (Exception ex) {
            logger.log(Level.INFO, "Email while sending email via "+this.PROVIDER+". Error message: " + ex.getMessage());
        }


    }

    @Override
    public void run() {
        this.sendSESEmail();
    }

}
