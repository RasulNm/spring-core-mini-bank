package sorokin.java.course.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import sorokin.java.course.account.Account;
import sorokin.java.course.user.User;

import java.util.Scanner;

@Configuration
@PropertySource("classpath:application.properties")
public class HibernateConfiguration {

    private final DBProperties dbProperties;
    private final HibernateProperties hibernateProperties;

    public HibernateConfiguration(DBProperties dbProperties, HibernateProperties hibernateProperties) {
        this.dbProperties = dbProperties;
        this.hibernateProperties = hibernateProperties;
    }

    @Bean
    public Scanner scanner() {
        return new Scanner(System.in);
    }

    @Bean
    public SessionFactory sessionFactory() {
        org.hibernate.cfg.Configuration config = new org.hibernate.cfg.Configuration();

        config
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Account.class)
                .setProperty("hibernate.connection.driver_class", dbProperties.getDriver())
                .setProperty("hibernate.connection.url", dbProperties.getUrl())
                .setProperty("hibernate.connection.username", dbProperties.getUsername())
                .setProperty("hibernate.connection.password", dbProperties.getPassword())
                .setProperty("hibernate.dialect", dbProperties.getDialect())
                .setProperty("hibernate.show_sql", String.valueOf(hibernateProperties.isShowSql()))
                .setProperty("hibernate.format_sql", String.valueOf(hibernateProperties.isFormatSql()))
                .setProperty("hibernate.hbm2ddl.auto", hibernateProperties.getAuto())
                .setProperty("hibernate.current_session_context_class", hibernateProperties.getSessionContextClass());

        return config.buildSessionFactory();
    }
}