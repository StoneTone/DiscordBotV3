package com.bot.discordbotv3.service;

import com.bot.discordbotv3.vo.LofiTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class LofiService {

    private static Logger logger = LoggerFactory.getLogger(LofiService.class);
    private static RestTemplate restTemplate;

    @Autowired
    public LofiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static List<LofiTrack> getLofiTracks(){
        String endpoint = "http://lofiscrap:8080/v1/lofi";

        try{
            ResponseEntity<List<LofiTrack>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<LofiTrack>>() {});

            if(response.getStatusCode() == HttpStatus.OK && response.getBody() != null){
                logger.info("Successfully connected to Lofi Service");
                return response.getBody();
            }
        }catch(RestClientException e){
            logger.error("Error connecting to Lofi Service. Either the service is down or is not running. Please check the service.");
        }
        return new ArrayList<>();
    }
}
