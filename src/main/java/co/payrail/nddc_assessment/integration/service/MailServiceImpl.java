package co.payrail.nddc_assessment.integration.service;

import co.payrail.nddc_assessment.integration.service.awsemail.EmailSenderService;
import co.payrail.nddc_assessment.integration.termii.model.Email;
import co.payrail.nddc_assessment.integration.termii.model.EmailAlertAttachment;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Personalization;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.io.File;

@Service
@Slf4j
public class MailServiceImpl implements MailService {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    EmailSenderService emailSenderService;

    @Value("${mail.from}")
    private String sender;

    @Value("${SENDGRID_API_KEY}")
    private String SENDGRID_API_KEY;

    @Value("${mail.from.name}")
    private String senderName;

    @Value("${MAILGUN_DOMAIN}")
    private String MAILGUN_DOMAIN;

    @Value("${MAILGUN_API_KEY}")
    private String MAILGUN_API_KEY;


    @Override
    public void sendGrid(Email emailInfo) throws IOException {
        logger.info("Recipients >> ", emailInfo.getReceiverEmails());
        com.sendgrid.helpers.mail.objects.Email from = new com.sendgrid.helpers.mail.objects.Email(sender, senderName);
        String subject = emailInfo.getMessageSubject();
        com.sendgrid.helpers.mail.objects.Email to = new com.sendgrid.helpers.mail.objects.Email(emailInfo.getReceiverEmail());
        Content content = new Content("text/plain", emailInfo.getMessageBody());
        Mail mail = new Mail(from, subject, to, content);

        if (emailInfo.getReceiverEmails().length > 0 ) {
            Personalization personalization = null;
            for (int i = 1, size = emailInfo.getReceiverEmails().length; i < size; i++) {
                personalization = new Personalization();
                personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(emailInfo.getReceiverEmails()[i]));
                mail.addPersonalization(personalization);
            }
        }

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("MAIL SENT");
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }

    @Override
    @Async
    public void sendGridMail(Email emailInfo, Context context) throws IOException {
        String messageBody = templateEngine.process(emailInfo.getTemplate(), context);

        com.sendgrid.helpers.mail.objects.Email from = new com.sendgrid.helpers.mail.objects.Email(sender, senderName);
        String subject = emailInfo.getMessageSubject();
        com.sendgrid.helpers.mail.objects.Email to = new com.sendgrid.helpers.mail.objects.Email(emailInfo.getReceiverEmail());
        Content content = new Content("text/html", messageBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
            log.info("Sent");
        } catch (IOException ex) {
            throw ex;
        }
    }

    @Override
    @Async
    public void sendMailgunMail(Email emailInfo, Context context) throws IOException {
        // Process the email template using Thymeleaf
        String messageBody = templateEngine.process(emailInfo.getTemplate(), context);

        // Build the Mailgun request
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN + "/messages")
                    .basicAuth("api", MAILGUN_API_KEY)  // Mailgun requires 'api' as the username
                    .field("from", String.format("%s <%s>", senderName, sender))
                    .field("to", emailInfo.getReceiverEmail() != null ? emailInfo.getReceiverEmail() : String.join(",", emailInfo.getReceiverEmails()))
                    .field("subject", emailInfo.getMessageSubject())
                    .field("html", messageBody)
                    .asJson();  // Call asJson() on the request to send and get a JSON response

            // Log the response details
            System.out.println("Mailgun Status: " + response.getStatus());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Headers: " + response.getHeaders());

            // Handle CC recipients if available
            if (emailInfo.getCcList() != null && emailInfo.getCcList().length > 0) {
                response = Unirest.post("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN + "/messages")
                        .basicAuth("api", MAILGUN_API_KEY)
                        .field("cc", String.join(",", emailInfo.getCcList()))
                        .asJson();
            }

            // Add attachments if any
            if (emailInfo.getEmailAttachments() != null && !emailInfo.getEmailAttachments().isEmpty()) {
                for (EmailAlertAttachment attachment : emailInfo.getEmailAttachments()) {
                    File file = new File(attachment.getFilePath());  // Ensure this path is valid
                    if (file.exists()) {
                        response = Unirest.post("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN + "/messages")
                                .basicAuth("api", MAILGUN_API_KEY)
                                .field("attachment", file)
                                .asJson();
                    } else {
                        System.out.println("Attachment not found: " + attachment.getFilePath());
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException("Error occurred while sending email via Mailgun", ex);
        }
    }

    @Override
    public void sendAwsEmail(Email emailInfo, Context context) throws IOException {
        String messageBody = templateEngine.process(emailInfo.getTemplate(), context);
        List<String> rlist = new ArrayList<>();
        if(!StringUtils.isEmpty(emailInfo.getReceiverEmail())){
            rlist.add(emailInfo.getReceiverEmail());
        }
        if(!Objects.isNull(emailInfo.getReceiverEmails()) && emailInfo.getReceiverEmails().length<1){
            Collections.addAll(rlist, emailInfo.getReceiverEmails());
        }
        List<String> cclist = new ArrayList<>();
        if(!Objects.isNull(emailInfo.getCcList()) && emailInfo.getCcList().length<1){
            Collections.addAll(cclist, emailInfo.getCcList());
        }
        List<String> bcclist = new ArrayList<>();
        bcclist.add("wsowunmi@plethub.com");
        bcclist.add("komirin@plethub.com");
        emailSenderService.sendAWSHTMLEmail("Welcome to PayRail", rlist, cclist, bcclist, messageBody);
    }

    @Override
    public void sendGridwithSenderMail(Email emailInfo, Context context, String mailSender) throws IOException {
        String messageBody = templateEngine.process(emailInfo.getTemplate(), context);

        com.sendgrid.helpers.mail.objects.Email from = new com.sendgrid.helpers.mail.objects.Email(mailSender);
        String subject = emailInfo.getMessageSubject();
        com.sendgrid.helpers.mail.objects.Email to = new com.sendgrid.helpers.mail.objects.Email(emailInfo.getReceiverEmail());
        Content content = new Content("text/html", messageBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }

    @Override
    public void sendGridMailWithAttachment(Email emailInfo, Context context,String fileName , String fileLocation) throws IOException {

        String messageBody = templateEngine.process(emailInfo.getTemplate(), context);

        com.sendgrid.helpers.mail.objects.Email from = new com.sendgrid.helpers.mail.objects.Email(sender,senderName);
        String subject = emailInfo.getMessageSubject();
        com.sendgrid.helpers.mail.objects.Email to = new com.sendgrid.helpers.mail.objects.Email(emailInfo.getReceiverEmail());
        Content content = new Content("text/html", messageBody);
        Mail mail = new Mail(from, subject, to, content);

        InputStream inputStream =  this.getClass().getClassLoader().getResourceAsStream(fileLocation);

        if(Objects.isNull(inputStream)){
            logger.info("location is : {}", fileLocation);
            logger.info("input is null!!!!!!!");
        }

        assert inputStream != null;
        Attachments attachments = new Attachments.Builder(fileName,inputStream).withType("application/pdf").build();


        mail.addAttachments(attachments);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {


            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            request.setBody(mail.build());
            Response response = sg.api(request);
            log.info("Response ==> {}", response);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }










}