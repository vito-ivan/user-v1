package com.nisum.user.infrastructure.config;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Configuration class for setting up an in-memory H2 database for development purposes.
 * This configuration is activated when the 'dev' profile is active.
 */
@Configuration
public class H2DevConfig {

    @Bean
    public DataSource dataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:usersdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean
    public ApplicationRunner warmUp(DataSource ds) {
        return args -> {
            try (var c = ds.getConnection(); var st = c.createStatement()) {
                st.execute("SELECT 1");
            }
        };
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2WebServer() throws SQLException {
        return Server.createWebServer("-web", "-webPort", "8082", "-webDaemon");
    }
}
