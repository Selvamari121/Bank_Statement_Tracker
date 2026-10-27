package com.example.demotrack.tracker;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
public class TransactionSummaryDto {

    private String member;

    private BigDecimal credit;

    private BigDecimal debit;

}
