package com.ronanos.lexiconlair.admin.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Component
public class AdminSettingsSchemaInitializer implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public AdminSettingsSchemaInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection, "admin_settings")) {
                return;
            }
            addColumnIfMissing(connection, "game_option_count", 4);
            addColumnIfMissing(connection, "game_question_count", 10);
        }
    }

    private void addColumnIfMissing(Connection connection, String columnName, int defaultValue) throws Exception {
        if (!columnExists(connection, "admin_settings", columnName)) {
            jdbcTemplate.execute("ALTER TABLE admin_settings ADD COLUMN " + columnName + " INTEGER NOT NULL DEFAULT " + defaultValue);
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet tables = metadata.getTables(null, null, tableName, new String[] { "TABLE" })) {
            if (tables.next()) {
                return true;
            }
        }
        try (ResultSet tables = metadata.getTables(null, null, tableName.toUpperCase(), new String[] { "TABLE" })) {
            return tables.next();
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                return true;
            }
        }
        try (ResultSet columns = metadata.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return columns.next();
        }
    }
}
