package com.corporate.visitor.db;

import com.corporate.visitor.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private static final DatabaseConfig CONFIG = DatabaseConfig.load();

    private Database() {
    }

    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(CONFIG.url(), CONFIG.user(), CONFIG.password());
    }
}
