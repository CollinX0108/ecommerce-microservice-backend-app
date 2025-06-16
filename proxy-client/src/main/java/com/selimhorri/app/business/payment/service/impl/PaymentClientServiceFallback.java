package com.selimhorri.app.business.payment.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.selimhorri.app.business.payment.model.PaymentDto;
import com.selimhorri.app.business.payment.model.response.PaymentPaymentServiceDtoCollectionResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentClientServiceFallback {
	
	public ResponseEntity<PaymentPaymentServiceDtoCollectionResponse> fallbackFindAll(Exception ex) {
		log.error("Payment service is unavailable. Using fallback for findAll. Error: {}", ex.getMessage());
		PaymentPaymentServiceDtoCollectionResponse fallbackResponse = new PaymentPaymentServiceDtoCollectionResponse();
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
	}
	
	public ResponseEntity<PaymentDto> fallbackFindById(String paymentId, Exception ex) {
		log.error("Payment service is unavailable. Using fallback for findById with ID: {}. Error: {}", paymentId, ex.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
	}
	
	public ResponseEntity<PaymentDto> fallbackSave(PaymentDto paymentDto, Exception ex) {
		log.error("Payment service is unavailable. Using fallback for save. Error: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
	}
	
	public ResponseEntity<PaymentDto> fallbackUpdate(PaymentDto paymentDto, Exception ex) {
		log.error("Payment service is unavailable. Using fallback for update. Error: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
	}
	
	public ResponseEntity<Boolean> fallbackDeleteById(String paymentId, Exception ex) {
		log.error("Payment service is unavailable. Using fallback for deleteById with ID: {}. Error: {}", paymentId, ex.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(false);
	}
} 