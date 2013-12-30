package com.gmind7.bakery.config;

import java.util.Properties;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.query.QueryDslJdbcTemplate;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories("com.gmind7")
@ImportResource("classpath:spring/auditing-context.xml")
public class JpaConfig {
	
	@Inject
	private Environment environment;
	
	@Inject
	private DataSourceConfig dataConfig;
	
	public Properties jpaProperties(){
		
		Properties properties = new Properties();
		
		properties.setProperty("hibernate.enable_lazy_load_no_trans", environment.getRequiredProperty("hibernate.enable_lazy_load_no_trans"));
		properties.setProperty("hibernate.auto_close_session", environment.getRequiredProperty("hibernate.auto_close_session"));
		properties.setProperty("hibernate.cache.use_second_level_cache", environment.getRequiredProperty("hibernate.cache.use_second_level_cache"));
		properties.setProperty("hibernate.cache.use_query_cache", environment.getRequiredProperty("hibernate.cache.use_query_cache"));
		properties.setProperty("hibernate.generate_statistics", environment.getRequiredProperty("hibernate.generate_statistics"));
		properties.setProperty("net.sf.ehcache.configurationResourceName", environment.getRequiredProperty("hibernate.net.sf.ehcache.configurationResourceName"));
		properties.setProperty("hibernate.cache.region.factory_class", environment.getRequiredProperty("hibernate.cache.region.factory_class"));
		
		return properties;
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
		HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
		
		jpaVendorAdapter.setGenerateDdl(environment.getRequiredProperty("hibernate.generateDdl", boolean.class));
		jpaVendorAdapter.setShowSql(environment.getRequiredProperty("hibernate.showSql", boolean.class));
		jpaVendorAdapter.setDatabase(getDatabaseType());
		jpaVendorAdapter.setDatabasePlatform(environment.getRequiredProperty("hibernate.dialectType"));
		
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(jpaVendorAdapter);
		factory.setPackagesToScan(environment.getRequiredProperty("hibernate.dialectType"));
		factory.setMappingResources(environment.getRequiredProperty("META-INF/orm.xml"));
		factory.setJpaProperties(jpaProperties());
		factory.setDataSource(dataConfig.dataSource());
		
		factory.afterPropertiesSet();
		return factory;
	}
	
	@Primary
	@Bean	
	public PlatformTransactionManager transactionManager(){
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return txManager;
	}	
	
	@Bean
	public QueryDslJdbcTemplate queryDslJdbcTemplate(){
		return new QueryDslJdbcTemplate(dataConfig.dataSource());
	}
	
	private Database getDatabaseType(){
		String database = environment.getRequiredProperty("hibernate.databaseType");
		for(Database dbType : Database.values())	{
			if(dbType.name().equals(database)) return dbType;
		}
		return Database.DEFAULT;
	}
	
}