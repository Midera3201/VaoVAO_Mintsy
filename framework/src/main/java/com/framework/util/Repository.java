package com.framework.util;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Repository<T> {

    protected abstract String getTableName();
    protected abstract Class<T> getEntityClass();

    protected Connection getConnection() throws Exception {
        return DatabaseConfig.getConnection();
    }

    public List<T> findAll() throws Exception {
        String sql = "SELECT * FROM " + getTableName();
        List<T> results = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    public T findById(int id) throws Exception {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public void save(T entity) throws Exception {
        Field[] fields = getEntityClass().getDeclaredFields();
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            if (name.equals("id")) continue;
            if (cols.length() > 0) cols.append(", ");
            if (vals.length() > 0) vals.append(", ");
            cols.append(name);
            vals.append("?");
            params.add(field.get(entity));
        }

        String sql = "INSERT INTO " + getTableName() + " (" + cols + ") VALUES (" + vals + ")";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws Exception {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private T mapRow(ResultSet rs) throws Exception {
        T instance = getEntityClass().getDeclaredConstructor().newInstance();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String colName = meta.getColumnLabel(i);
            try {
                Field field = getEntityClass().getDeclaredField(colName);
                field.setAccessible(true);
                Object value = rs.getObject(colName);
                if (value != null) {
                    field.set(instance, value);
                }
            } catch (NoSuchFieldException e) {
                // ignore columns without matching field
            }
        }
        return instance;
    }
}
