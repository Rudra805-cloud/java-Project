package com.corporate.visitor.repository;

import com.corporate.visitor.db.Database;
import com.corporate.visitor.model.Visit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class VisitRepository {
    public long checkIn(String visitorName, String company, String hostName, String phone, int allowedMinutes)
            throws SQLException {
        String sql = """
                INSERT INTO visits (visitor_name, company, host_name, phone, checked_in_at, allowed_minutes)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, visitorName);
            statement.setString(2, company);
            statement.setString(3, hostName);
            statement.setString(4, phone);
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            statement.setInt(6, allowedMinutes);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("The database did not return the new visit ID.");
                }
                return keys.getLong(1);
            }
        }
    }

    public List<Visit> findActive() throws SQLException {
        return find("""
                SELECT id, visitor_name, company, host_name, phone, checked_in_at,
                       checked_out_at, allowed_minutes, reminder_sent
                FROM visits
                WHERE checked_out_at IS NULL
                ORDER BY checked_in_at
                """);
    }

    public void checkOut(long visitId) throws SQLException {
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE visits SET checked_out_at = ? WHERE id = ? AND checked_out_at IS NULL")) {
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(2, visitId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("The visit is already checked out or does not exist.");
            }
        }
    }

    public void markReminderSent(long visitId) throws SQLException {
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE visits SET reminder_sent = TRUE WHERE id = ? AND checked_out_at IS NULL")) {
            statement.setLong(1, visitId);
            statement.executeUpdate();
        }
    }

    private List<Visit> find(String sql) throws SQLException {
        List<Visit> visits = new ArrayList<>();
        try (Connection connection = Database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                visits.add(new Visit(
                        results.getLong("id"),
                        results.getString("visitor_name"),
                        results.getString("company"),
                        results.getString("host_name"),
                        results.getString("phone"),
                        results.getTimestamp("checked_in_at").toLocalDateTime(),
                        results.getTimestamp("checked_out_at") == null
                                ? null : results.getTimestamp("checked_out_at").toLocalDateTime(),
                        results.getInt("allowed_minutes"),
                        results.getBoolean("reminder_sent")));
            }
        }
        return visits;
    }
}
