package com.nit.binding;

import lombok.Data;

@Data
public class ElgibilityDetails {
	private Integer caseNo;
	private String holderName;
	private Long holderSSN;
	private String planName;
	private String planStatus;
	private Double benifitAmt;
	private String denialReason;
	private String bankName;
	private Long accountNumber;
}
