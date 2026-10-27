package com.example.demotrack.tracker;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfParserService {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}-[A-Za-z]{3}-\\d{2}$");

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(-|[\\d,]+\\.\\d{2})\\s+" + "(-|[\\d,]+\\.\\d{2})\\s+"
                    + "([\\d,]+\\.\\d{2})$");

    private static final Pattern UPI_NAME_PATTERN = Pattern.compile("UPI/[^/]+/(?:CR|DR)\\s*/?\\s*" + "([^/]+?)\\s*/",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern INTEREST_PATTERN = Pattern.compile("^Int\\.Pd:", Pattern.CASE_INSENSITIVE);

    public List<TransactionEntity> parse(MultipartFile file) throws IOException {

        List<TransactionEntity> transactions = new ArrayList<>();

        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            String text = stripper.getText(document);

            transactions = parseTransactions(text);
        }

        return transactions;
    }

    private List<TransactionEntity> parseTransactions(String text) {

        List<TransactionEntity> transactions = new ArrayList<>();

        String[] lines = text.split("\\r?\\n");

        String currentDate = null;

        StringBuilder transactionText = new StringBuilder();

        for (String rawLine : lines) {

            String line = rawLine.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("Effective available balance")) {
                break;
            }

            if (line.startsWith("**This is a computer generated")) {
                break;
            }

            if (DATE_PATTERN.matcher(line).matches()) {

                if (currentDate != null && transactionText.length() > 0) {

                    TransactionEntity transaction = createTransaction(transactionText.toString());

                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                }

                currentDate = line;
                transactionText.setLength(0);
                continue;
            }

            if (line.matches("^\\(\\d{2}-[A-Za-z]{3}-\\d{2}\\)$")) {
                continue;
            }

            if (line.startsWith("Date")
                    || line.startsWith("Particulars")
                    || line.startsWith("Ref No")
                    || line.startsWith("Cheque No")
                    || line.startsWith("Transaction")
                    || line.startsWith("Type")
                    || line.startsWith("Debit")
                    || line.startsWith("Credit")
                    || line.startsWith("Balance")) {
                continue;
            }

            if (currentDate != null) {
                if (transactionText.length() > 0) {
                    transactionText.append(" ");
                }
                transactionText.append(line);
            }
        }

        if (currentDate != null && transactionText.length() > 0) {

            TransactionEntity transaction = createTransaction(transactionText.toString());
            if (transaction != null) {
                transactions.add(transaction);
            }
        }

        return transactions;
    }

    private TransactionEntity createTransaction(String transactionText) {

        Matcher amountMatcher = AMOUNT_PATTERN.matcher(transactionText);

        if (!amountMatcher.find()) {
            return null;
        }

        String debitText = amountMatcher.group(1);

        String creditText = amountMatcher.group(2);

        String description = transactionText.substring(0, amountMatcher.start()).trim();

        BigDecimal amount;

        TransactionType type;

        if (!"-".equals(debitText)) {
            amount = parseAmount(debitText);
            type =TransactionType.DEBIT;
        }else if (!"-".equals(creditText)) {
            amount = parseAmount(creditText);
            type = TransactionType.CREDIT;
        }else {
            return null;
        }

        String member = extractMember(description);
        if (member == null || member.isBlank()) {
            return null;
        }

        TransactionEntity transaction = new TransactionEntity();
        transaction.setMember( member.trim());
        transaction.setType(type);
        transaction.setAmount(amount);
        return transaction;
    }

    private String extractMember(String description) {

        if (description == null || description.isBlank()) {
            return "";
        }

        String value = description.trim();

        if (INTEREST_PATTERN.matcher(value).find()) {
            return "Interest";
        }

        Matcher upiMatcher = UPI_NAME_PATTERN.matcher(value);

        if (upiMatcher.find()) {
            String name = upiMatcher.group(1).trim();
            return cleanMemberName(name);
        }

        Pattern transferPattern = Pattern.compile("^(.*?)(?:\\s+S\\d{8,})" + "\\s+Transfer$",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher transferMatcher = transferPattern.matcher(value);

        if (transferMatcher.find()) {
            return cleanMemberName( transferMatcher.group(1));
        }

        Pattern referencePattern = Pattern.compile("^(.*?)(?:\\s+S\\d{8,})" + "\\s+.*$",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher referenceMatcher = referencePattern.matcher(value);

        if (referenceMatcher.find()) {
            return cleanMemberName(referenceMatcher.group(1));
        }

        return cleanMemberName(value);
    }

    private String cleanMemberName(String name) {

        if (name == null) {
            return "";
        }

        String value = name.trim();

        value = value.replaceFirst( "(?i)^(mr|mrs|ms|miss)\\.?\\s+", "");

        value = value.replaceAll("\\s+"," ").trim();

        return value;
    }

    private BigDecimal parseAmount(String amount) {
        return new BigDecimal( amount.replace(",", ""));
    }
}