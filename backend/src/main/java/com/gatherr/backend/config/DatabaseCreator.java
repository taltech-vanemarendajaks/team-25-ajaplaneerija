package com.gatherr.backend.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DatabaseCreator implements BeanPostProcessor {

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPass;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource && !(bean instanceof DelegatingDataSource)) {
            createDatabaseIfNotExists();
            return new DelegatingDataSource((DataSource) bean);
        }
        return bean;
    }

    private void createDatabaseIfNotExists() {
        String dbName = dbUrl.substring(dbUrl.lastIndexOf('/') + 1);
        String adminUrl = dbUrl.replace("/" + dbName, "/postgres");

        try (Connection conn = DriverManager.getConnection(adminUrl, dbUser, dbPass);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
            System.out.println("Database '" + dbName + "' created successfully.");

        } catch (SQLException e) {
            if (!"42P04".equals(e.getSQLState())) { // 42P04 = database exists
                throw new RuntimeException("Failed to create database", e);
            }
        }
    }
}
