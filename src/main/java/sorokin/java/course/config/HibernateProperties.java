package sorokin.java.course.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HibernateProperties {
    private final String auto;
    private final boolean showSql;
    private final boolean formatSql;

    public HibernateProperties(
            @Value("${hibernate.hbm2ddl.auto}") String auto,
            @Value("${hibernate.show_sql}") boolean showSql,
            @Value("${hibernate.format_sql}") boolean formatSql
    ) {
        this.auto = auto;
        this.showSql = showSql;
        this.formatSql = formatSql;
    }

    public String getAuto() {
        return auto;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public boolean isFormatSql() {
        return formatSql;
    }
}