package com.example.demo.service;

import com.example.demo.dto.request.DispatchOrderDto;
import com.lowagie.text.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public interface ReceiptService {

    ResponseEntity<Document> export(Long transactionId, HttpServletResponse response, DispatchOrderDto dispatchOrderDto) throws IOException;

}
