package com.nit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.nit.entity.CitizenAppRegistrationEntity;
import com.nit.entity.DcCaseEntity;
import com.nit.entity.PlanEntity;
import com.nit.repository.IApplicationRegistrationRepository;
import com.nit.repository.IDcCaseRepository;
import com.nit.repository.IDcChildrenRepository;
import com.nit.repository.IDcEducationRepository;
import com.nit.repository.IDcIncomeRepository;
import com.nit.repository.IPlanRepository;
import com.nit.service.DcMgmtServiceImpl;

//@WebMvcTest(value = DcMgmtServiceImpl.class)
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class DcMgmtServiceImplTest {
	@MockBean
	private IDcCaseRepository caseRepo;
	
	@MockBean
	private IApplicationRegistrationRepository citizenAppRepo;
	
	@MockBean
	private IPlanRepository planRepo;
	
	@MockBean
	private IDcIncomeRepository incomeRepo;
	
	@MockBean
	private IDcEducationRepository educationRepo;
	
	@MockBean
	private IDcChildrenRepository childrenRepo;
	
	@InjectMocks
	private DcMgmtServiceImpl dcService;
	
	@Test
	public void generateCaseNoTest1() {
		// provide behaviour for repository methods (findById())
		CitizenAppRegistrationEntity citizenEntity = new CitizenAppRegistrationEntity();
		citizenEntity.setAppId(1);
		Optional<CitizenAppRegistrationEntity> optCitizenEntity = Optional.of(citizenEntity);
		Mockito.when(citizenAppRepo.findById(1)).thenReturn(optCitizenEntity);
		// provide behaviour for repository methods (save())
		DcCaseEntity caseEntity = new DcCaseEntity();
		caseEntity.setAppId(1);
		
		DcCaseEntity caseEntity1 = new DcCaseEntity();
		caseEntity1.setAppId(1);
		caseEntity1.setCaseNo(1001);
		
		Mockito.when(caseRepo.save(caseEntity)).thenReturn(caseEntity1);
		
		// get actual result
		int actual = dcService.generateCaseNo(1);
		assertEquals(1001, actual);
	}
	
	@Test
	public void generateCaseNoTest2() {
		// provide behaviour for repository methods (findById())
		CitizenAppRegistrationEntity citizenEntity = new CitizenAppRegistrationEntity();
		citizenEntity.setAppId(10);
		Optional<CitizenAppRegistrationEntity> optCitizenEntity = Optional.empty();
		Mockito.when(citizenAppRepo.findById(1)).thenReturn(optCitizenEntity);
		// provide behaviour for repository methods (save())
		DcCaseEntity caseEntity = new DcCaseEntity();
		caseEntity.setAppId(10);
		
		DcCaseEntity caseEntity1 = new DcCaseEntity();
		caseEntity1.setAppId(10);
		caseEntity1.setCaseNo(0000);
		
		Mockito.when(caseRepo.save(caseEntity)).thenReturn(caseEntity1);
		
		// get actual result
		int actual = dcService.generateCaseNo(10);
		assertEquals(0, actual);
	}
	
	@Test
	public void showAllPlanNamesTest() {
		PlanEntity entity1 = new PlanEntity();
		entity1.setPlanName("SNAP");
		PlanEntity entity2 = new PlanEntity();
		entity2.setPlanName("MEDAID");
		PlanEntity entity3 = new PlanEntity();
		entity3.setPlanName("MEDCARE");
		PlanEntity entity4 = new PlanEntity();
		entity4.setPlanName("QHP");
		PlanEntity entity5 = new PlanEntity();
		entity5.setPlanName("CCAP");
		PlanEntity entity6 = new PlanEntity();
		entity6.setPlanName("CAJW");
		List<PlanEntity> list = List.of(entity1, entity2, entity3, entity4, entity5, entity6);
		
		// provide behaviour for repository methods (findAll())
		Mockito.when(planRepo.findAll()).thenReturn(list);
		// invoke actual method to actual result
		List<String> planList = dcService.showAllPlanNames();
		
		assertEquals(6, planList.size());
	}
}
