package com.nit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nit.bindings.ChildInputs;
import com.nit.bindings.CitizenAppRegistrationInputs;
import com.nit.bindings.DcSummaryReport;
import com.nit.bindings.EducationInputs;
import com.nit.bindings.IncomeInputs;
import com.nit.bindings.PlanSelectionInputs;
import com.nit.entity.CitizenAppRegistrationEntity;
import com.nit.entity.DcCaseEntity;
import com.nit.entity.DcChildrenEntity;
import com.nit.entity.DcEducationEntity;
import com.nit.entity.DcIncomeEntity;
import com.nit.entity.PlanEntity;
import com.nit.repository.IApplicationRegistrationRepository;
import com.nit.repository.IDcCaseRepository;
import com.nit.repository.IDcChildrenRepository;
import com.nit.repository.IDcEducationRepository;
import com.nit.repository.IDcIncomeRepository;
import com.nit.repository.IPlanRepository;

@Service
public class DcMgmtServiceImpl implements IDcMgmtService {
	
	@Autowired
	private IDcCaseRepository caseRepo;
	
	@Autowired
	private IApplicationRegistrationRepository citizenAppRepo;
	
	@Autowired
	private IPlanRepository planRepo;
	
	@Autowired
	private IDcIncomeRepository incomeRepo;
	
	@Autowired
	private IDcEducationRepository educationRepo;
	
	@Autowired
	private IDcChildrenRepository childrenRepo;

	@Override
	public Integer generateCaseNo(Integer appId) {
		//Load Citizen Data
		Optional<CitizenAppRegistrationEntity> appCitizen = citizenAppRepo.findById(appId);
		if(appCitizen.isPresent()) {
			DcCaseEntity caseEntity = new DcCaseEntity();
			caseEntity.setAppId(appId);
			return caseRepo.save(caseEntity).getCaseNo();
		}
		return 0;
	}

	@Override
	public List<String> showAllPlanNames() {
		List<PlanEntity> plansList = planRepo.findAll();
		// get only plan names using streaming api
		List<String> planNameList = plansList.stream().map(plan -> plan.getPlanName()).toList();
		return planNameList;
	}

	@Override
	public Integer savePlanSelection(PlanSelectionInputs plan) {
		//Load DCaseEntity object
		Optional<DcCaseEntity> opt = caseRepo.findById(plan.getCaseNo());
		if(opt.isPresent()) {
			DcCaseEntity caseEntity = opt.get();
			caseEntity.setPlanId(plan.getPlanId());
			//update the DcCaseEntity with plan id
			caseRepo.save(caseEntity); // update obj operation
			return caseEntity.getCaseNo();
		}
		return 0;
	}

	@Override
	public Integer saveIncomeDetails(IncomeInputs income) {
		// Convert binding obj data to Entity class obj data
		DcIncomeEntity incomeEntity = new DcIncomeEntity();
		BeanUtils.copyProperties(income, incomeEntity);
		// save the income details
		incomeRepo.save(incomeEntity);
		// return caseNo
		return income.getCaseNo();
	}

	@Override
	public Integer saveEducationDetails(EducationInputs education) {
		// Convert binding object to Entity object
		DcEducationEntity educationEntity = new DcEducationEntity();
		BeanUtils.copyProperties(education, educationEntity);
		// save the obj
		educationRepo.save(educationEntity);
		// return the caseNumber
		return education.getCaseNo();
	}

	@Override
	public Integer saveChildrenDetails(List<ChildInputs> children) {
		// Convert each Binding class obj to each Entity class obj
		children.forEach(child ->{
			DcChildrenEntity childEntity = new DcChildrenEntity();
			BeanUtils.copyProperties(child, childEntity);
			// save each entity obj
			childrenRepo.save(childEntity);
		});
		// return caseNo
		return children.get(0).getCaseNo();
	}

	@Override
	public DcSummaryReport showDcSummary(Integer caseNo) {
		// get multiple entity objs based on caseNo
		DcIncomeEntity incomeEntity = incomeRepo.findByCaseNo(caseNo);
		DcEducationEntity educationEntity = educationRepo.findByCaseNo(caseNo);
		List<DcChildrenEntity> childsEntityList = childrenRepo.findByCaseNo(caseNo);
		Optional<DcCaseEntity> optCaseEntity = caseRepo.findById(caseNo);
		// get PlanName
		String planName = null;
		Integer appId = null;
		if(optCaseEntity.isPresent()) {
			DcCaseEntity caseEntity = optCaseEntity.get();
			Integer planId = caseEntity.getPlanId();
			appId = caseEntity.getAppId();
			Optional<PlanEntity> optPlanEntity = planRepo.findById(planId);
			if(optPlanEntity.isPresent()) {
				planName = optPlanEntity.get().getPlanName();
			}
		}
		
		Optional<CitizenAppRegistrationEntity> optCitizenEntity = citizenAppRepo.findById(appId);
		CitizenAppRegistrationEntity citizenEntity = null;
		if(optCitizenEntity.isPresent()) {
			citizenEntity = optCitizenEntity.get();
		}
		
		// Convert Entity objs to Binding objs
		IncomeInputs income = new IncomeInputs();
		BeanUtils.copyProperties(incomeEntity, income);
		
		EducationInputs education = new EducationInputs();
		BeanUtils.copyProperties(educationEntity, education);
		
		List<ChildInputs> listChilds = new ArrayList<>();
		childsEntityList.forEach(childEntity -> {
			ChildInputs child = new ChildInputs();
			BeanUtils.copyProperties(childEntity, child);
			listChilds.add(child);
		});
		
		CitizenAppRegistrationInputs citizen = new CitizenAppRegistrationInputs();
		BeanUtils.copyProperties(citizenEntity, citizen);
		
		// prepare DcSummaryReport obj
		DcSummaryReport report = new DcSummaryReport();
		report.setPlanName(planName);
		report.setIncomeDetails(income);
		report.setEducationDetails(education);
		report.setCitizenDetails(citizen);
		report.setChildrenDetails(listChilds);
		
		return null;
	}

}
