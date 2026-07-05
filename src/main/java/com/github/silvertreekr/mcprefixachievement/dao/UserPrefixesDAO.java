package com.github.silvertreekr.mcprefixachievement.dao;

import com.github.silvertreekr.mcprefixachievement.database.MysqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserPrefixesDAO {
    private final MysqlDatabase database;

    public UserPrefixesDAO(MysqlDatabase database) {
        this.database = database;
    }

    public CompletableFuture<Void> initialize() {
        return database.runAsync(connection -> {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS user_prefixes (
                        uuid VARCHAR(36) NOT NULL,
                        prefix_id INT NOT NULL,
                        PRIMARY KEY (uuid, prefix_id),
                        INDEX idx_prefix (prefix_id)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                        """);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Set<Integer>> getPrefixIDs(UUID uuid) {
        return database.supplyAsync(connection -> {
            try {
                Set<Integer> prefixIDs = new HashSet<>();
                String sql = "SELECT * FROM user_prefixes WHERE uuid = ?;";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, uuid.toString());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                     int prefixID = resultSet.getInt("prefix_id");
                     prefixIDs.add(prefixID);
                }
                return prefixIDs;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> addPrefixes(UUID uuid, Set<Integer> prefixIDs) {
        return database.runAsync(connection -> {
            String sql = "INSERT IGNORE INTO user_prefixes(uuid, prefix_id) VALUES (?, ?);";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int id : prefixIDs) {
                    statement.setString(1, uuid.toString());
                    statement.setInt(2, id);
                    statement.addBatch();
                }
                statement.executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> deletePrefix(UUID uuid, int id) {
        return database.runAsync(connection -> {
           String sql = "DELETE FROM user_prefixes WHERE uuid = ? AND prefix_id = ?;";
           try (PreparedStatement statement = connection.prepareStatement(sql)) {
               statement.setString(1, uuid.toString());
               statement.setInt(2, id);
               statement.executeUpdate();
           } catch (SQLException e) {
               throw new RuntimeException(e);
           }
        });
    }
}
