package com.panbet.ahservice.common.dao;


import java.util.Collection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.panbet.ao.interfaces.AccountBaseAO;
import com.panbet.dao.oracle.AccountDAOOracle;
import com.panbet.logic.account.TransferTO.Operations;
import com.panbet.paymentsystems.PaymentGatewayAccessService;


@ManagedResource("com.panbet:type=dao,name=AccountLocalDAOOracle")
public class AccountLocalDAOOracle extends AccountDAOOracle implements AccountBaseAO
{

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaymentGatewayAccessService paymentGatewayAccessService;

    @Override
    protected JdbcTemplate jdbc()
    {
        return jdbcTemplate;
    }


    @Required
    public void setDataSource(DataSource ds)
    {
        this.jdbcTemplate = new JdbcTemplate(ds, true);
    }
    
    @Override
    public Collection<Operations> getIncreaseOperationsList() 
    {
        return paymentGatewayAccessService.getIncreaseOperations();
    }
}
