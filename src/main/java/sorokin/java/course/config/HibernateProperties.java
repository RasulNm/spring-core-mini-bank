package sorokin.java.course.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HibernateProperties {
    private final String auto;
    private final boolean showSql;
    private final boolean formatSql;
    private final String sessionContextClass;

    public HibernateProperties(
            @Value("${hibernate.hbm2ddl.auto}") String auto,
            @Value("${hibernate.show_sql}") boolean showSql,
            @Value("${hibernate.format_sql}") boolean formatSql,
            @Value("${hibernate.current_session_context_class}") String sessionContextClass
    ) {
        this.auto = auto;
        this.showSql = showSql;
        this.formatSql = formatSql;
        this.sessionContextClass = sessionContextClass;
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

    public String getSessionContextClass() {
        return sessionContextClass;
    }
}