package com.nit.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nit.binding.COSummary;
import com.nit.entity.CitizenAppRegistrationEntity;
import com.nit.entity.CoTriggersEntity;
import com.nit.entity.DcCaseEntity;
import com.nit.entity.ElgibilityDetailsEntity;
import com.nit.repository.IApplicationRegistrationRepository;
import com.nit.repository.ICoTriggerRepository;
import com.nit.repository.IDcCaseRepository;
import com.nit.repository.IElgibilityDeterminationRepository;

@Service
public class CorrespondenceMgmtServiceImpl implements ICorrespondenceMgmtService {
	
	@Autowired
	private ICoTriggerRepository triggerRepo;
	
	@Autowired
	private IElgibilityDeterminationRepository elgiRepo;
	
	@Autowired
	private IDcCaseRepository caseRepo;
	
	@Autowired
	private IApplicationRegistrationRepository citizenRepo;

	@Override
	public COSummary processPendingTriggers() {
		
		CitizenAppRegistrationEntity citizenEntity = null;
		
		ElgibilityDetailsEntity eligiEntity = null;
		
		// get all pending triggers
		List<CoTriggersEntity> triggersList = triggerRepo.findByTriggerStatus("pending");
		
		// process each pending triggers
		for(CoTriggersEntity triggerEntity : triggersList) {
			// get Eligibility details based on caseno
			eligiEntity = elgiRepo.findByCaseNo(triggerEntity.getCaseNo());
			
			// get appId based on caseno
			Optional<DcCaseEntity> optCaseEntity = caseRepo.findById(triggerEntity.getCaseNo());
			if(optCaseEntity.isPresent()) {
				DcCaseEntity caseEntity = optCaseEntity.get();
				Integer appId = caseEntity.getAppId();
				
				// get the citizen details based on the appId
				Optional<CitizenAppRegistrationEntity> optCitizenEntity = citizenRepo.findById(appId);
				if(optCitizenEntity.isPresent()) {
					citizenEntity = optCitizenEntity.get();
				}
			}
			// generate pdf doc having eligibility details
			generatrPdf(eligiEntity, citizenEntity);
		}
			
			
			// send pdf doc to citizen as email message
			// store pdf doc in CO_TRIGGERS db table and also update Trigger status to "completed"
		return null;
	}
	
	// helper method to generate the pdf doc
	private void generatrPdf(ElgibilityDetailsEntity eligiEntity, CitizenAppRegistrationEntity citizenEntity) {
		
	}

}
