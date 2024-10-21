package co.payrail.nddc_assessment.integration.service.implementation;


import co.payrail.nddc_assessment.integration.service.IntegrationService;
import co.payrail.nddc_assessment.integration.termii.model.TermiiSmsRequest;
import co.payrail.nddc_assessment.integration.termii.model.TermiiSmsResponse;
import co.payrail.nddc_assessment.integration.termii.service.Termii;
import co.payrail.nddc_assessment.integration.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
public class IntegrationServiceImpl implements IntegrationService {

    private static final int PHONE_NO_LENGTH = 14;
    @Autowired
    private Termii termii;


    @Autowired
    private MessageSource messageSource ;

    private final Locale locale = LocaleContextHolder.getLocale();
    @Autowired
    @Value("banking.nip")
    private  String nipBankName;

    @Autowired
    @Value("banking.vas")
    private  String vasBankName;

    @Override
    public void sendSms(String phoneNumber, String message) throws NullPointerException  {
        String formattedPhone = validateNumber(phoneNumber);
        TermiiSmsRequest request = new TermiiSmsRequest();
        request.setTo(formattedPhone);
        request.setSms(message);

        try {
            TermiiSmsResponse response = termii.sendSms(request);
            log.info("+++ SMS response ..{}",response);
        }   catch (Exception e){
            e.printStackTrace();
        }

    }




    /**
     * This validates a phone number against some specified rules
     * returns the correct number or null if the number failed a rule
     */
    public String validateNumber(String phone) throws NullPointerException {
        String returned = null;
        String phoneLocal = phone.replaceAll("[()]", "").trim();
        //check if it is in international format
        if (phoneLocal.contains("+")) {
            //Only send to mobile numbers
            //check if this is a nigerian number
            if (phoneLocal.contains("+234")) {
                if (phoneLocal.length() == PHONE_NO_LENGTH) {
                    returned = phoneLocal;
                } else {
                    //this a landline or invalid number hence no alert will be sent
                    log.error("This is an invalid number or a landline hence no alert will be sent: %s",
                            phoneLocal.replaceAll("[()]", "").trim());
                    log.info("Accepted formats; +234********** and 0**********");
                    throw new NullPointerException();
                }
            } else {
                //a non nigerian number
                returned = phoneLocal;
            }

            //check for more than one + sign
            if (phoneLocal.lastIndexOf('+') > 0) {
                //the phone number contains more than one + therefore it is invalid
                log.error(String.format("The phone number %s has more than one plus", phoneLocal));
                log.info("Accepted formats; +234********** and 0**********");
                throw new NullPointerException("Invalid phone number");
            }
        }else if (phoneLocal.contains("234")){
            //check if it is a landline
            if (phoneLocal.length() < 13) {
                log.error("This is a landline hence no alert will be sent");
                throw new NullPointerException("");
            } else {
                //It is a mobile number
                //convert it to international format
                returned = "+" + phoneLocal.trim();
            }
        } else {
            //check if it is a landline
            if (phoneLocal.length() < 10) {
                log.error("This is a landline hence no alert will be sent");
                throw new NullPointerException("");
            } else if (StringUtils.remove(phoneLocal,'+').length() > 11) {
                log.error("Invalid number: " + phoneLocal);
                log.info("Accepted formats; +234********** and 0**********");
                throw new NullPointerException();
            } else {
                //It is a mobile number
                //convert it to international format
                returned = "+234" + phoneLocal.substring(1).replaceAll("[()]", "").trim();
            }
        }
        //check if the number is invalid
        if (returned.length() < PHONE_NO_LENGTH) {
            log.error(String.format("Phone Number %s: is invalid", phoneLocal));
            log.info("Accepted formats; +234********** and 0**********");
            throw new NullPointerException("");
        } else if (returned.equals("+2348000000000")) {
            log.error("Invalid number: 08000000000");
            throw new NullPointerException("");
        }

        return returned.substring(1);
    }

}
