package com.nit.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nit.bindings.ElgibilityDetailsOutput;
import com.nit.entity.CitizenAppRegistrationEntity;
import com.nit.entity.CoTriggersEntity;
import com.nit.entity.DcCaseEntity;
import com.nit.entity.DcChildrenEntity;
import com.nit.entity.DcEducationEntity;
import com.nit.entity.DcIncomeEntity;
import com.nit.entity.ElgibilityDetailsEntity;
import com.nit.entity.PlanEntity;
import com.nit.repository.IApplicationRegistrationRepository;
import com.nit.repository.ICoTriggerRepository;
import com.nit.repository.IDcCaseRepository;
import com.nit.repository.IDcChildrenRepository;
import com.nit.repository.IDcEducationRepository;
import com.nit.repository.IDcIncomeRepository;
import com.nit.repository.IElgibilityDeterminationRepository;
import com.nit.repository.IPlanRepository;

@Service
public class ElgibilityDeterminationMgmtServiceImpl implements IElgibilityDeterminationMgmtService {

	@Autowired
	private IDcCaseRepository caseRepo;
	
	@Autowired
	private IPlanRepository planRepo;
	
	@Autowired
	private IDcIncomeRepository incomeRepo;
	
	@Autowired
	private IDcChildrenRepository childrenRepo;
	
	@Autowired
	private IApplicationRegistrationRepository citizenRepo;
	
	@Autowired
	private IDcEducationRepository educationRepo;
	
	@Autowired
	private IElgibilityDeterminationRepository elgiRepo;
	
	@Autowired
	private ICoTriggerRepository triggerRepo;
	
	@Override
	public ElgibilityDetailsOutput determineElgibility(Integer caseNo) {
		Integer appId = null;
		Integer planId = null;
		// get planId and appId based on caseNo
		Optional<DcCaseEntity> optCaseEntity = caseRepo.findById(caseNo);
		if(optCaseEntity.isPresent()) {
			DcCaseEntity caseEntity = optCaseEntity.get();
			planId = caseEntity.getPlanId();
			appId = caseEntity.getAppId();
		}
		
		// get plan name
		String planName = null;
		Optional<PlanEntity> optPlanEntity = planRepo.findById(planId);
		if(optPlanEntity.isPresent()) {
			PlanEntity planEntity = optPlanEntity.get();
			planName = planEntity.getPlanName();
		}
		
		// calculate citizen age by getting citizen DOB through appId
		Optional<CitizenAppRegistrationEntity> optCitizenEntity = citizenRepo.findById(appId);
		
		int citizenAge = 0;
		String citizenName = null;
		long citizenSSN = 0;
		if(optCitizenEntity.isPresent()) {
			CitizenAppRegistrationEntity citizenEntity = optCitizenEntity.get();
			LocalDate citizenDOB = citizenEntity.getDob();
			citizenName = citizenEntity.getFullName();
			LocalDate sysDate = LocalDate.now();
			citizenAge = Period.between(citizenDOB, sysDate).getYears();
			citizenSSN = citizenEntity.getSsn();
		}
		
		// call helper method to plan conditions
		ElgibilityDetailsOutput elgiOutput = applyPlanConditions(caseNo, planName, citizenAge);
		// set Citizen name
		elgiOutput.setHolderName(citizenName);
		
		// save Elgibility entity object
		ElgibilityDetailsEntity elgiEntity = new ElgibilityDetailsEntity();
		BeanUtils.copyProperties(elgiOutput, elgiEntity);
		elgiEntity.setCaseNo(caseNo);
		elgiEntity.setHolderSSN(citizenSSN);
		elgiRepo.save(elgiEntity);
		
		// save CoTriggers object
		CoTriggersEntity triggersEntity = new CoTriggersEntity();
		triggersEntity.setCaseNo(caseNo);
		triggersEntity.setTriggerStatus("Pending");
		triggerRepo.save(triggersEntity);
		
		return elgiOutput;
	}
	
	private ElgibilityDetailsOutput applyPlanConditions(Integer caseNo, String planName, int citizenAge) {
		ElgibilityDetailsOutput elgiOutput = new ElgibilityDetailsOutput();
		elgiOutput.setPlanName(planName);
		
		// get income details of the citizen
		DcIncomeEntity incomeEntity = incomeRepo.findByCaseNo(caseNo);
		double empIncome = incomeEntity.getEmpIncome();
		double propertyIncome = incomeEntity.getPropertyIncome();
		
		// for SNAP
		if(planName.equalsIgnoreCase("SNAP")) {
			if(empIncome<=300) {
				elgiOutput.setPlanStatus("Approved");
				elgiOutput.setBenifitAmt(200.0);
			} else {
				elgiOutput.setPlanStatus("Denied");
				elgiOutput.setDenialReason("High Income");
			}
		} else if(planName.equalsIgnoreCase("CCAP")) {
			boolean kidsCountCondition = false;
			boolean kidAgeCondition = true;
			
			List<DcChildrenEntity> listChilds = childrenRepo.findByCaseNo(caseNo);
			if(!listChilds.isEmpty()) {
				kidsCountCondition = true;
				
				for(DcChildrenEntity child : listChilds) {
					int kidAge = Period.between(child.getChildDOB(), LocalDate.now()).getYears();
					if(kidAge > 16) {
						kidAgeCondition = false;
						break;
					}
				}
			}
			
			if(empIncome <= 300 && kidsCountCondition && kidAgeCondition) {
				elgiOutput.setPlanStatus("APPROVED");
				elgiOutput.setBenifitAmt(300.0);
			} else {
				elgiOutput.setPlanStatus("Denied");
				elgiOutput.setDenialReason("CCAP rules are not satisfied.");
			}
		} else if(planName.equalsIgnoreCase("MEDCARE")) {
			if(citizenAge >= 65) {
				elgiOutput.setPlanStatus("APPROVED");
				elgiOutput.setBenifitAmt(350.0);
			} else {
				elgiOutput.setPlanStatus("Denied");
				elgiOutput.setDenialReason("MEDCARE rules are not satisfied.");
			}
		} else if(planName.equalsIgnoreCase("MEDAID")) {
			if(empIncome <= 300 && propertyIncome == 0) {
				elgiOutput.setPlanStatus("APPROVED");
				elgiOutput.setBenifitAmt(200.0);
			} else {
				elgiOutput.setPlanStatus("Denied");
				elgiOutput.setDenialReason("MEDAID rules are not satisfied.");
			}
		} else if(planName.equalsIgnoreCase("CAJW")) {
			DcEducationEntity educationEntity = educationRepo.findByCaseNo(caseNo);
			int passOutYear = educationEntity.getPassOutYear();
			if(empIncome == 0 && passOutYear < LocalDate.now().getYear()) {
				elgiOutput.setPlanStatus("APPROVED");
				elgiOutput.setBenifitAmt(300.0);
			} else {
				elgiOutput.setPlanStatus("Denied");
				elgiOutput.setDenialReason("CAJW rules are not satisfied.");
			}
		} else if(planName.equalsIgnoreCase("QHP")) {
			if(citizenAge >= 1) {
				elgiOutput.setPlanStatus("APPROVED");
			} else {
				elgiOutput.setPlanStatus("Denied");
				elgiOutput.setDenialReason("QHP rules are not satisfied.");
			}
		}
			
		// set the common properties for elgiOutput obj only if the plan is approved
		if(elgiOutput.getPlanStatus().equalsIgnoreCase("Approved")) {
			elgiOutput.setPlanStartDate(LocalDate.now());
			elgiOutput.setPlanEndDate(LocalDate.now().plusYears(2));
		}
		
		return elgiOutput;
	}

}
