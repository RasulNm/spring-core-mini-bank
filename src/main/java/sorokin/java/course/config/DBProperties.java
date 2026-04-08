package sorokin.java.course.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DBProperties {
    private final String driver;
    private final String url;
    private final String username;
    private final String password;
    private final String dialect;

    public DBProperties(
            @Value("${db.driver}") String driver,
            @Value("${db.url}") String url,
            @Value("${db.username}") String username,
            @Value("${db.password}") String password,
            @Value("${db.dialect}") String dialect
    ) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.dialect = dialect;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDialect() {
        return dialect;
    }
}