package com.nit.service;

import com.nit.bindings.ElgibilityDetailsOutput;

public interface IElgibilityDeterminationMgmtService {
	public ElgibilityDetailsOutput determineElgibility(Integer caseNo);

}
