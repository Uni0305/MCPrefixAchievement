package com.github.silvertreekr.mcprefixachievement.database;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public class MysqlDatabase {
    // SLF4J 로거를 사용하여 로그 메시지를 기록
    private static final Logger logger = LoggerFactory.getLogger(MysqlDatabase.class);

    // ExecutorService를 사용하여 비동기적으로 데이터베이스 작업을 처리
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    // MysqlDatabaseConfig에서 Config 정보 가져오기
    private final MysqlDatabaseConfig config;

    // 생성자
    private MysqlDatabase(@NotNull MysqlDatabaseConfig config) {
        this.config = config;
    }

    // MysqlDatabaseConfig에서 Config 정보 가져오기
    public static @NotNull MysqlDatabase initialize(@NotNull JavaPlugin plugin) throws NullPointerException {
        ConfigurationSection bukkitConfig = plugin.getConfig().getConfigurationSection("database");
        if (bukkitConfig == null) {
            throw new NullPointerException("database config is null");
        }

        MysqlDatabaseConfig databaseConfig = MysqlDatabaseConfig.fromBukkitConfig(bukkitConfig);
        return new MysqlDatabase(databaseConfig);
    }

    // MySQL 데이터베이스에 연결
    public @NotNull Connection connect() throws SQLException {
        // Driver 클래스 못잡으면 RuntimeException 날려라
        try {
            Class.forName(Driver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // MySQL 연결하는 URL 만들기
        String url = String.format("jdbc:mysql://%s/%s", config.getAddress(), config.getDatabase());

        // MySQL 연결에 필요한 Properties 객체 만들기
        Properties properties = new Properties();
        properties.put("user", config.getUsername());
        properties.put("password", config.getPassword());
        properties.putAll(config.getProperties());

        return DriverManager.getConnection(url, properties);
    }

    // 비동기적으로 데이터베이스 작업을 수행하는 메서드(반환 없음)
    public @NotNull CompletableFuture<Void> runAsync(Consumer<Connection> consumer) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = connect()) {
                consumer.accept(connection);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }, executor);
    }

    // 비동기적으로 데이터베이스 작업을 수행하고 결과를 반환하는 메서드
    public <T> @NotNull CompletableFuture<T> supplyAsync(Function<Connection, T> function) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connect()) {
                return function.apply(connection);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    // ExecutorService 종료
    public void shutdown() {
        executor.shutdown();
    }
}
