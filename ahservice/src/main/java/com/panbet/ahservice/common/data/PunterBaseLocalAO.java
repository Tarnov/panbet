package com.panbet.ahservice.common.data;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.panbet.ao.interfaces.PunterBaseAO;
import com.panbet.logic.punter.PunterDAO;
import com.panbet.logic.punter.PunterSummaryForBetFlow;


public class PunterBaseLocalAO implements PunterBaseAO
{
    @Autowired
    private PunterDAO punterDAO;


    @Override
    public Map<Integer, Integer> getCountryDefPunterTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PunterSummaryForBetFlow getPunterSummaryBetFlow(Integer punterId)
    {
        return punterDAO.getPunterSummaryBetFlow(punterId);
    }
}
