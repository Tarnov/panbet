package com.panbet.ahservice.common.dao;


import java.util.Collection;
import java.util.HashSet;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.panbet.common.services.CommandProxy;
import com.panbet.logic.account.TransferTO.Operations;
import com.panbet.paymentsystems.PaymentGatewayAccessService;


public class PaymentGatewayAccessServiceImpl implements PaymentGatewayAccessService
{
    private Collection<Operations> paymentGatewayIncreaceOperations = new HashSet<>();
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayAccessServiceImpl.class);

    @Autowired
    private CommandProxy commandProxy;


    @Override
    public Collection<Operations> getIncreaseOperations()
    {
        return paymentGatewayIncreaceOperations;
    }


    @PostConstruct
    public void postConstruct()
    {
        logger.info("Try to get PaymentGateway increaceOperations from APP");
        this.paymentGatewayIncreaceOperations.addAll(commandProxy.getIncreaseOperationsFromPaymentGateway());
        logger.info("Success. increaceOperations count {}", paymentGatewayIncreaceOperations.size());
    }

}
