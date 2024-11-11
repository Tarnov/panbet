package com.panbet.ahservice.common.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;

import com.panbet.dao.oracle.PunterStatisticDAOOracle;

public class PunterStatisticLocalDAOOracle extends PunterStatisticDAOOracle
{
    private JdbcTemplate jdbcTemplate;

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
}
