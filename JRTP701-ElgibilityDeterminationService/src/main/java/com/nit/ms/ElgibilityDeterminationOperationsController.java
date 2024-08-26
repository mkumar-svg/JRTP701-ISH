package com.nit.ms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nit.bindings.ElgibilityDetailsOutput;
import com.nit.service.IElgibilityDeterminationMgmtService;

@RestController
@RequestMapping("/ed-api")
public class ElgibilityDeterminationOperationsController {
	
	@Autowired
	private IElgibilityDeterminationMgmtService edService;
	
	@GetMapping("/determine/{caseNo}")
	public ResponseEntity<ElgibilityDetailsOutput> checkPlanElgibility(@PathVariable Integer caseNo) {
		// use service
		ElgibilityDetailsOutput output = edService.determineElgibility(caseNo);
		return new ResponseEntity<ElgibilityDetailsOutput>(output, HttpStatus.CREATED);
	}

}
