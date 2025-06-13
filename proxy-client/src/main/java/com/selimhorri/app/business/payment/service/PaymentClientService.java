package com.selimhorri.app.business.payment.service;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.selimhorri.app.business.payment.model.PaymentDto;
import com.selimhorri.app.business.payment.model.response.PaymentPaymentServiceDtoCollectionResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(name = "PAYMENT-SERVICE", contextId = "paymentClientService", path = "/payment-service/api/payments")
public interface PaymentClientService {
	
	@GetMapping
	@CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackFindAll")
	@Bulkhead(name = "paymentService")
	@Retry(name = "paymentService")
	public ResponseEntity<PaymentPaymentServiceDtoCollectionResponse> findAll();
	
	@GetMapping("/{paymentId}")
	@CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackFindById")
	@Bulkhead(name = "paymentService")
	@Retry(name = "paymentService")
	public ResponseEntity<PaymentDto> findById(
			@PathVariable("paymentId") 
			@NotBlank(message = "Input must not be blank!") 
			@Valid final String paymentId);
	
	@PostMapping
	@CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackSave")
	@Bulkhead(name = "paymentService")
	@Retry(name = "paymentService")
	public ResponseEntity<PaymentDto> save(
			@RequestBody 
			@NotNull(message = "Input must not be NULL!") 
			@Valid final PaymentDto paymentDto);
	
	@PutMapping
	@CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackUpdate")
	@Bulkhead(name = "paymentService")
	@Retry(name = "paymentService")
	public ResponseEntity<PaymentDto> update(
			@RequestBody 
			@NotNull(message = "Input must not be NULL!") 
			@Valid final PaymentDto paymentDto);
	
	@DeleteMapping("/{paymentId}")
	@CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackDeleteById")
	@Bulkhead(name = "paymentService")
	@Retry(name = "paymentService")
	public ResponseEntity<Boolean> deleteById(@PathVariable("paymentId") final String paymentId);
	
}










