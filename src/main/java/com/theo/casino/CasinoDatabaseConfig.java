package com.theo.casino;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class CasinoDatabaseConfig {

  @Bean
  @ConditionalOnMissingBean(DataSource.class)
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:file:./data/casino");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }
}
