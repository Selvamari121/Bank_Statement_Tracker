package com.example.demotrack.tracker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired 
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Void> addTransaction(@RequestBody TransactionEntity transaction) {
        transactionService.addTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        transactionService.processPdf(file);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping
    public ResponseEntity<List<TransactionEntity>> getAllData() {
        List<TransactionEntity> data = transactionService.getAllData();
        return ResponseEntity.status(HttpStatus.OK).body(data);
    }

    @GetMapping("/summary")
    public ResponseEntity<List<TransactionSummaryDto>> getSummary() {
        List<TransactionSummaryDto> summary = transactionService.getSummary();
        return ResponseEntity.status(HttpStatus.OK).body(summary);
    }
}