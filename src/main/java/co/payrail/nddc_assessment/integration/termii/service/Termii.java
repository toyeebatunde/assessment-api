package co.payrail.nddc_assessment.integration.termii.service;


import co.payrail.nddc_assessment.integration.termii.model.TermiiSmsRequest;
import co.payrail.nddc_assessment.integration.termii.model.TermiiSmsResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class Termii {


    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    @Value("${termii.apiKey}")
    private String apiKey;
    @Value("${termii.From}")
    private String from;


    public TermiiSmsResponse sendSms(TermiiSmsRequest request) throws IOException{

        String urlToCall = "https://api.ng.termii.com/api/sms/send" ;

        request.setFrom(from);
        request.setApi_key(apiKey);
        String json = gson.toJson(request);
        log.info("request body for termii  ...{}", json);
        RequestBody body = RequestBody.create( MediaType.get("application/json; charset=utf-8"), json);
        Request smsRequest = new Request.Builder()
                .url(urlToCall )
                .post(body)
                .build();

        log.debug("url ......{}",urlToCall);

        try (Response response = client.newCall(smsRequest).execute()) {
            String result =  response.body().string();
            log.info("result  ......{}", result);
            return gson.fromJson(result , TermiiSmsResponse.class);
        }

    }

    @Async
    public void sendSmsTo(String phoneNumber, String invite) throws IOException {
        TermiiSmsRequest request = new TermiiSmsRequest();
        request.setTo(phoneNumber);
        request.setSms(String.format("You have been invited to take an assessment. Click here to start: %s. It expires in 10 hrs. Please do not disclose it to anyone",invite));
        sendSms(request);
    }


}
