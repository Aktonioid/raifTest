package com.raifTest.configs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan
//@EnableTransactionManagement
public class DatabaseConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource(){

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(env.getProperty("spring.datasource.url"));
        config.setUsername(env.getProperty("spring.datasource.username"));
        config.setPassword(env.getProperty("spring.datasource.password"));
        config.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        config.setTransactionIsolation(env.getProperty("spring.datasource.isolationLevel"));

        config.addDataSourceProperty("cachePrepStmts", env.getProperty("hikari.cachePrepStmts"));
        config.addDataSourceProperty("prepStmtCacheSize", env.getProperty("hikari.prepStmtCacheSize"));
        config.addDataSourceProperty("prepStmtCacheSqlLimit", env.getProperty("hikari.prepStmtCacheSqlLimit"));

        return new HikariDataSource(config);
    }

    @Bean
    public Properties hibernateProperties(){

        Properties properties = new Properties();

        properties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        properties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        properties.put("hibernate.format_sql", env.getProperty("hibernate.format_sql"));
        properties.put("hibernate.highlight_sql", env.getProperty("hibernate.highlight_sql"));
        properties.put("logging.level.org.hibernate.SQL", env.getProperty("logging.level.org.hibernate.SQL"));
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));

        return properties;
    }

    @Bean()
    public LocalSessionFactoryBean entityManagerFactory(){
        LocalSessionFactoryBean localSessionFactory = new LocalSessionFactoryBean();

        localSessionFactory.setDataSource(dataSource());
        localSessionFactory.setPackagesToScan(new String[] {
                "com.raifTest.core.models"
        });
        localSessionFactory.setHibernateProperties(hibernateProperties());

        return  localSessionFactory;
    }

    @Bean
    public HibernateTransactionManager transactionManager(){
        HibernateTransactionManager manager = new HibernateTransactionManager();

        manager.setSessionFactory(entityManagerFactory().getObject());

        return manager;
    }
}
