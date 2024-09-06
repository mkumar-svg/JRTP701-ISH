package com.nit.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.nit.binding.ElgibilityDetails;
import com.nit.entity.ElgibilityDetailsEntity;

@Component
public class EDDetailsProcessor implements ItemProcessor<ElgibilityDetailsEntity, ElgibilityDetails> {

	@Override
	public ElgibilityDetails process(ElgibilityDetailsEntity item) throws Exception {
		if(item.getPlanStatus().equalsIgnoreCase("approved")) {
			ElgibilityDetails details = new ElgibilityDetails();
			BeanUtils.copyProperties(item, details);
			return details;
		} else
			return null;
	}

}
