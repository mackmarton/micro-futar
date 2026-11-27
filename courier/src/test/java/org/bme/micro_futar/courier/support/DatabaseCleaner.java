package org.bme.micro_futar.courier.support;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/** Rolling back is not enough: the Kafka listeners write on their own threads and commit. */
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;
    private final Seed seed;

    DatabaseCleaner(JdbcTemplate jdbcTemplate, Seed seed) {
        this.jdbcTemplate = jdbcTemplate;
        this.seed = seed;
    }

    public void clean() throws Exception {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT quote_ident(tablename) FROM pg_tables WHERE schemaname = 'public'", String.class);
        if (!tables.isEmpty()) {
            jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tables) + " CASCADE");
        }
        seed.run();
    }

    @FunctionalInterface
    interface Seed {
        void run() throws Exception;
    }
}
