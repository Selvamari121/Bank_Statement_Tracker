package com.example.demotrack.tracker;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TransactionService {

    @Autowired 
    private TransactionRepository transactionRepository;

    @Autowired
    private PdfParserService pdfParserService;

    public void addTransaction(TransactionEntity transaction) {

        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        if (transaction.getMember() == null || transaction.getMember().trim().isEmpty()) {
            throw new IllegalArgumentException("Member name is required");
        }

        if (transaction.getType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        if (transaction.getAmount() == null) {
            throw new IllegalArgumentException("Transaction amount is required");
        }

        if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transaction amount cannot be negative");
        }

        transaction.setMember(transaction.getMember().trim());
        transaction.setType(transaction.getType());
        transaction.setAmount(transaction.getAmount());
        transactionRepository.save(transaction);
    }

    public List<TransactionEntity> getAllData() {
        return transactionRepository.findAllByOrderByIdDesc();
    }

    public void processPdf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is empty");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException( "PDF file name is missing");
        }

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        try {
            List<TransactionEntity> transactions = pdfParserService.parse(file);

            if (transactions.isEmpty()) {
                throw new IllegalArgumentException("No transactions found in PDF");
            }

            transactionRepository.saveAll(transactions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF", e);
        }
    }

    public List<TransactionSummaryDto> getSummary() {
        List<TransactionEntity> transactions = transactionRepository.findAll();

        Map<String, BigDecimal> creditMap = new LinkedHashMap<>();
        Map<String, BigDecimal> debitMap = new LinkedHashMap<>();
        Map<String, String> memberNameMap = new LinkedHashMap<>();

        for (TransactionEntity transaction : transactions) {

            if (transaction == null) {
                continue;
            }

            if (transaction.getMember() == null || transaction.getType() == null || transaction.getAmount() == null) {
                continue;
            }

            String displayName = transaction.getMember().trim();

            if (displayName.isEmpty()) {
                continue;
            }

            String memberKey = displayName.toLowerCase();

            memberNameMap.putIfAbsent(memberKey,displayName);

            if (transaction.getType() == TransactionType.CREDIT) {
                creditMap.merge( memberKey, transaction.getAmount(), BigDecimal::add);
            }else if (transaction.getType() == TransactionType.DEBIT) {
                debitMap.merge(memberKey, transaction.getAmount(), BigDecimal::add);
            }
        }

        Map<String, TransactionSummaryDto> result = new LinkedHashMap<>();
        for (String memberKey : memberNameMap.keySet()) {
            String member = memberNameMap.get(memberKey);

            BigDecimal credit = creditMap.getOrDefault(memberKey, BigDecimal.ZERO);

            BigDecimal debit = debitMap.getOrDefault(memberKey, BigDecimal.ZERO);

            result.put(memberKey, new TransactionSummaryDto(member, credit, debit)
            );
        }

        return result.values().stream().toList();
    }
}