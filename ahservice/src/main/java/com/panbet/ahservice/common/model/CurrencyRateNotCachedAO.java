package com.panbet.ahservice.common.model;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;

import com.panbet.ao.interfaces.CurrencyFindAO;
import com.panbet.ao.interfaces.CurrencyRateInfoAO;
import com.panbet.currency.CurrencyRateRowConverter;
import com.panbet.logic.currency.CurrencyRateDAO;
import com.panbet.logic.currency.CurrencyRateTO;
import com.panbet.logic.currency.CurrencyRateTO.Type;
import com.panbet.logic.dictionary.CurrencyTO;
import com.panbet.otherutils.model.CurrencyRateHelper;

public class CurrencyRateNotCachedAO implements CurrencyRateInfoAO {
	
	@Autowired
	private CurrencyRateDAO currencyRateDAO;

    @Autowired
    private CurrencyRateRowConverter rowConverter;
	
	@Override
	public CurrencyRateTO getRate(CurrencyTO first, CurrencyTO second, Type type, Timestamp timestamp) {
		return CurrencyRateHelper.getFromDB(first, second, type, timestamp, currencyRateDAO, rowConverter);
	}

}
