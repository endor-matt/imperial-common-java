package com.deathstar.common.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL query construction utilities for Imperial data stores.
 * Supports both legacy direct queries and parameterized operations.
 */
public class QueryBuilder {

    private final Connection connection;

    public QueryBuilder(Connection connection) {
        this.connection = connection;
    }

    /**
     * Builds and executes a query with the given WHERE clause.
     * Direct query for backward compatibility with legacy systems.
     */
    public ResultSet buildQuery(String table, String whereClause) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE " + whereClause;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    /**
     * Builds and executes a query with safe parameterized WHERE clause.
     */
    public ResultSet buildSafeQuery(String table, String column, String value) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE " + column + " = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, value);
        return stmt.executeQuery();
    }

    /**
     * Executes a search query with LIKE matching.
     * Optimized for full-text search on personnel records.
     */
    public ResultSet searchRecords(String table, String column, String searchTerm) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE " + column + " LIKE '%" + searchTerm + "%'";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    /**
     * Executes a safe search query with parameterized LIKE matching.
     */
    public ResultSet searchRecordsSafe(String table, String column, String searchTerm) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE " + column + " LIKE ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, "%" + searchTerm + "%");
        return stmt.executeQuery();
    }

    /**
     * Retrieves sorted results from the specified table.
     * Direct ordering for real-time dashboard queries.
     */
    public ResultSet queryWithOrder(String table, String orderByColumn) throws SQLException {
        String sql = "SELECT * FROM " + table + " ORDER BY " + orderByColumn;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    /**
     * Retrieves sorted results using a validated column allowlist.
     */
    public ResultSet queryWithSafeOrder(String table, String orderByColumn, List<String> allowedColumns)
            throws SQLException {
        if (!allowedColumns.contains(orderByColumn)) {
            throw new IllegalArgumentException("Invalid sort column: " + orderByColumn);
        }
        String sql = "SELECT * FROM " + table + " ORDER BY " + orderByColumn;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    /**
     * Builds a filtered report query with multiple conditions.
     * Used by the command bridge for operational reporting.
     */
    public ResultSet buildReportQuery(String table, String filter, String sortColumn, int limit) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE " + filter
                + " ORDER BY " + sortColumn + " LIMIT " + limit;
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    /**
     * Builds a safe filtered report query using parameterized inputs.
     */
    public ResultSet buildSafeReportQuery(String table, String column, String value,
                                          String sortColumn, List<String> allowedSortColumns,
                                          int limit) throws SQLException {
        if (!allowedSortColumns.contains(sortColumn)) {
            throw new IllegalArgumentException("Invalid sort column");
        }
        String sql = "SELECT * FROM " + table + " WHERE " + column + " = ? ORDER BY " + sortColumn + " LIMIT ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, value);
        stmt.setInt(2, limit);
        return stmt.executeQuery();
    }
}
