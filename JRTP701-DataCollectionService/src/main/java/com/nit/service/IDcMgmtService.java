package com.nit.service;

import java.util.List;

import com.nit.bindings.ChildInputs;
import com.nit.bindings.DcSummaryReport;
import com.nit.bindings.EducationInputs;
import com.nit.bindings.IncomeInputs;
import com.nit.bindings.PlanSelectionInputs;

public interface IDcMgmtService {
	public Integer generateCaseNo(Integer appId);
	public List<String> showAllPlanNames();
	public Integer savePlanSelection(PlanSelectionInputs plan);
	public Integer saveIncomeDetails(IncomeInputs income);
	public Integer saveEducationDetails(EducationInputs education);
	public Integer saveChildrenDetails(List<ChildInputs> children);
	public DcSummaryReport showDcSummary(Integer caseNo);
}
