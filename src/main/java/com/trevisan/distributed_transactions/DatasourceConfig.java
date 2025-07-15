package com.trevisan.distributed_transactions;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.mysql.cj.jdbc.MysqlXADataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;


import javax.sql.DataSource;
import javax.sql.XADataSource;

@Configuration
public class DatasourceConfig {
    @Primary
    @Bean
    @ConfigurationProperties(prefix = "data01.datasource")
    public DataSource data01(@Qualifier("dsPropsDb1") DataSourceProperties dsPropsDb1){
        XADataSource xaDataSource = this.createXaDataSource(dsPropsDb1);
        AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
        bean.setXaDataSource(xaDataSource);
        bean.setMaxPoolSize(20);
        bean.setMinPoolSize(10);
        bean.setUniqueResourceName("xaDb1");
        return bean;
    }

    @Bean
    @ConfigurationProperties(prefix = "data02.datasource")
    public DataSource data02(@Qualifier("dsPropsDb2") DataSourceProperties dsPropsDb2){
        XADataSource xaDataSource = this.createXaDataSource(dsPropsDb2);
        AtomikosDataSourceBean bean = new AtomikosDataSourceBean();
        bean.setXaDataSource(xaDataSource);
        bean.setMaxPoolSize(20);
        bean.setMinPoolSize(10);
        bean.setUniqueResourceName("xaDb2");
        return bean;
    }

    @Bean
    @ConfigurationProperties(prefix = "data01.datasource")
    public DataSourceProperties dsPropsDb1(){
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "data02.datasource")
    public DataSourceProperties dsPropsDb2(){
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManagerJta(){
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setUserTransaction(new UserTransactionImp());
        jtaTransactionManager.setTransactionManager(new UserTransactionManager());
        jtaTransactionManager.setAllowCustomIsolationLevels(true);
        return jtaTransactionManager;
    }

    private XADataSource createXaDataSource(DataSourceProperties dataProps){
        MysqlXADataSource mysqlXADataSource = new MysqlXADataSource();
        mysqlXADataSource.setUrl(dataProps.getUrl());
        mysqlXADataSource.setUser(dataProps.getUsername());
        mysqlXADataSource.setPassword(dataProps.getPassword());
        return mysqlXADataSource;
    }
}
