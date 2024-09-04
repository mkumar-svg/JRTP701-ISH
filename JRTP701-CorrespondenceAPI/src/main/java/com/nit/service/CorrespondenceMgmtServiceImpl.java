package com.nit.service;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nit.binding.COSummary;
import com.nit.entity.CitizenAppRegistrationEntity;
import com.nit.entity.CoTriggersEntity;
import com.nit.entity.DcCaseEntity;
import com.nit.entity.ElgibilityDetailsEntity;
import com.nit.repository.IApplicationRegistrationRepository;
import com.nit.repository.ICoTriggerRepository;
import com.nit.repository.IDcCaseRepository;
import com.nit.repository.IElgibilityDeterminationRepository;
import com.nit.utils.EmailUtils;

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
	
	@Autowired
	private EmailUtils mailUtil;

	@Override
	public COSummary processPendingTriggers() {
		
		CitizenAppRegistrationEntity citizenEntity = null;
		
		ElgibilityDetailsEntity eligiEntity = null;
		
		int pendingTriggers = 0;
		
		int successTriggers = 0;
		
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
			// generate pdf doc having eligibility details and send that pdf doc as email
			
			try
			{
				generatrPdfAndSendMail(eligiEntity, citizenEntity);
				successTriggers++;
			} catch(Exception e) {
				pendingTriggers++;
				e.printStackTrace();
			}
			
			
			// store pdf doc in CO_TRIGGERS db table and also update Trigger status to "completed"
		}
		
		// prepare COSummary Report
		COSummary summary = new COSummary();
		summary.setTotalTriggers(triggersList.size());
		summary.setPendingTriggers(pendingTriggers);
		summary.setSuccessTriggers(successTriggers);
			
			
		return summary;
	}
	
	// helper method to generate the pdf doc
	private void generatrPdfAndSendMail(ElgibilityDetailsEntity eligiEntity, CitizenAppRegistrationEntity citizenEntity) throws Exception {

//		create Document obj (openPdf)
		Document document = new Document(PageSize.A4);
		
//		create pdf file to write the content to it
		File file = new File(eligiEntity.getCaseNo()+".pdf");
		FileOutputStream fos = new FileOutputStream(file);
		
//		get PdfWriter to write to the document and response obj
		PdfWriter.getInstance(document, fos);
		
//		open the document
		document.open();
		
//		Define Font for the paragraph
		Font font = FontFactory.getFont(FontFactory.TIMES_BOLD);
		font.setSize(30);
		font.setColor(Color.CYAN);
		
//		create the paragraph having content and above font style
		Paragraph para = new Paragraph("Plan Approval/Denial Communication", font);
		para.setAlignment(Paragraph.ALIGN_CENTER);
		
//		add paragraph to document
		document.add(para);
		
//		Display search results as pdf table
		PdfPTable table = new PdfPTable(10);
		table.setWidthPercentage(70);
		table.setWidths(new float[] {3.0f,3.0f,3.0f,3.0f,3.0f,3.0f,3.0f,3.0f,3.0f,3.0f});
		table.setSpacingBefore(2.0f);
		
//		prepare heading row cells in the pdf table
		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(Color.GRAY);
		cell.setPadding(5);
		Font cellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		cellFont.setColor(Color.BLACK);
		
		cell.setPhrase(new Phrase("traceId", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("caseNo", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("holderName", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("holderSSN", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("planName", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("planStatus", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("planStartDate", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("planEndDate", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("BenifitAmt", cellFont));
		table.addCell(cell);
		cell.setPhrase(new Phrase("DenialReason", cellFont));
		table.addCell(cell);
		
//		add data cells to pdftable
			table.addCell(String.valueOf(eligiEntity.getEdTraceId()));
			table.addCell(String.valueOf(eligiEntity.getCaseNo()));			
			table.addCell(eligiEntity.getHolderName());
			table.addCell(String.valueOf(eligiEntity.getHolderSSN()));
			table.addCell(eligiEntity.getPlanName());
			table.addCell(eligiEntity.getPlanStatus());			
			table.addCell(String.valueOf(eligiEntity.getPlanStartDate()));
			table.addCell(String.valueOf(eligiEntity.getPlanEndDate()));
			table.addCell(String.valueOf(eligiEntity.getBenifitAmt()));
			table.addCell(String.valueOf(eligiEntity.getDenialReason()));
		
//		add table to document
		document.add(table);
		
//		close the document
		document.close();
		
		// send the generated pdf doc as the email message
		String subject = "Plan approval/denial mail";
		String body = "hello Mr/Miss/Mrs." + citizenEntity.getFullName() + ", This mail contains complete details plan approval or denial ";
		mailUtil.sendMail(citizenEntity.getEmail(), subject, body, file);
		
		// update Co_Trigger table
		updateCoTrigger(eligiEntity.getCaseNo(), file);
	}
	
	private void updateCoTrigger(Integer caseNo, File file) throws Exception {
		// check Trigger availblity based on the caseNo
		CoTriggersEntity triggerEntity = triggerRepo.findByCaseNo(caseNo);
		
		// get byte[] representing
		byte[] pdfContent = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(pdfContent);
		if(triggerEntity != null) {
			triggerEntity.setCoNoticePdf(pdfContent);
			triggerEntity.setTriggerStatus("completed");
			triggerRepo.save(triggerEntity);
		}
		fis.close();
	}

}
