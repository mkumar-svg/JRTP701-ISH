package com.nit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="JR701_CO_TRIGGERS")
@Data
public class CoTriggersEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer triggerId;
	private Integer caseNo;
	@Lob
	private byte[] coNoticePdf;
	@Column(length = 30)
	private String triggerStatus="pending";
}
