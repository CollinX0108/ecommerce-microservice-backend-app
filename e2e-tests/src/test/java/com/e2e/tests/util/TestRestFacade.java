package com.e2e.tests.util;

import static org.springframework.http.HttpMethod.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestRestFacade {

    private final RestTemplate restTemplate;

    @Autowired
    public TestRestFacade(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public <T> ResponseEntity<T> post(String url, Object body, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            final var response = restTemplate.exchange(
                    url,
                    POST,
                    new HttpEntity<>(body, headers),
                    responseType
            );
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("HTTP Error in POST " + url + ": " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public <T> ResponseEntity<T> put(String url, Object body, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            final var response = restTemplate.exchange(
                    url,
                    PUT,
                    new HttpEntity<>(body, headers),
                    responseType
            );
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("HTTP Error in PUT " + url + ": " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public <T> ResponseEntity<T> get(String url, Class<T> responseType) {
        try {
            final var response = restTemplate.getForEntity(url, responseType);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("HTTP Error in GET " + url + ": " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public <T> ResponseEntity<T> delete(String url, Class<T> responseType) {
        try {
            final var response = restTemplate.exchange(
                    url,
                    DELETE,
                    null,
                    responseType
            );
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.err.println("HTTP Error in DELETE " + url + ": " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    // Método auxiliar para hacer llamadas sin validar el status code (útil para casos de error esperados)
    public <T> ResponseEntity<T> postWithoutValidation(String url, Object body, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            return restTemplate.exchange(
                    url,
                    POST,
                    new HttpEntity<>(body, headers),
                    responseType
            );
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("Expected error in POST " + url + ": " + e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }

    public <T> ResponseEntity<T> getWithoutValidation(String url, Class<T> responseType) {
        try {
            return restTemplate.getForEntity(url, responseType);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("Expected error in GET " + url + ": " + e.getStatusCode());
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }
} 