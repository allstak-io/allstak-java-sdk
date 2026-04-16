package dev.allstak.spring;

import dev.allstak.AllStakClient;
import dev.allstak.database.AllStakDataSource;
import dev.allstak.internal.SdkLogger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

/**
 * Automatically wraps all DataSource beans with AllStak DB instrumentation.
 * <p>
 * This BeanPostProcessor intercepts every DataSource bean created by Spring
 * and wraps it with {@link AllStakDataSource} for automatic query capture.
 * <p>
 * Activated when {@code allstak.capture-db-queries=true} (default: true).
 * <p>
 * Works transparently with:
 * <ul>
 *   <li>Spring Boot auto-configured DataSources (HikariCP)</li>
 *   <li>Custom @Bean DataSources</li>
 *   <li>Multiple DataSources (each wrapped independently)</li>
 *   <li>JPA/Hibernate EntityManagerFactory</li>
 *   <li>JdbcTemplate</li>
 *   <li>Flyway/Liquibase migrations</li>
 *   <li>Connection pools</li>
 * </ul>
 */
public class AllStakDataSourcePostProcessor implements BeanPostProcessor, Ordered {

    private final AllStakClient client;

    public AllStakDataSourcePostProcessor(AllStakClient client) {
        this.client = client;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource ds && !(bean instanceof AllStakDataSource)) {
            String dbType = detectDatabaseType(ds, beanName);
            String dbName = beanName;
            SdkLogger.debug("AllStak wrapping DataSource bean '%s' (type=%s) for DB query capture", beanName, dbType);
            return AllStakDataSource.wrap(ds, client, dbType, dbName);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        // Run after all other post-processors to wrap the final DataSource
        return Ordered.LOWEST_PRECEDENCE;
    }

    private String detectDatabaseType(DataSource ds, String beanName) {
        String className = ds.getClass().getName().toLowerCase();
        if (className.contains("postgres") || className.contains("pgpool")) return "postgresql";
        if (className.contains("mysql") || className.contains("mariadb")) return "mysql";
        if (className.contains("h2")) return "h2";
        if (className.contains("sqlite")) return "sqlite";
        if (className.contains("oracle")) return "oracle";
        if (className.contains("sqlserver") || className.contains("mssql")) return "sqlserver";

        // Check bean name as fallback
        String name = beanName.toLowerCase();
        if (name.contains("postgres") || name.contains("pg")) return "postgresql";
        if (name.contains("mysql")) return "mysql";
        if (name.contains("geo")) return "postgresql"; // PostGIS is PostgreSQL

        // For HikariCP, try the JDBC URL from toString()
        try {
            String str = ds.toString();
            if (str.contains("postgresql")) return "postgresql";
            if (str.contains("mysql")) return "mysql";
            if (str.contains("h2")) return "h2";
        } catch (Exception ignored) {}

        return "unknown";
    }
}
