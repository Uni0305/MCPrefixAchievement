package com.github.silvertreekr.mcprefixachievement.database;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MysqlDatabaseConfig {
    // MySQL 서버 주소
    private final String address;

    // MySQL 사용자 이름
    private final String username;

    // MySQL 비밀번호
    private final String password;

    // MySQL 데이터베이스 이름
    private final String database;

    // JDBC에 전달할 추가 속성 (옵셔널)
    private final Map<String, Object> properties;

    // 생성자
    private MysqlDatabaseConfig(@NotNull String address, @NotNull String username, @NotNull String password, @NotNull String database, @NotNull Map<String, Object> properties) {
        this.address = address;
        this.username = username;
        this.password = password;
        this.database = database;
        this.properties = properties;
    }

    // DB Config 파일에서 읽어온 값으로 MysqlDatabaseConfig 객체를 생성힘
    public static @NotNull MysqlDatabaseConfig fromBukkitConfig(@NotNull ConfigurationSection config) throws IllegalArgumentException {

        // NULL 체크
        String address = config.getString("address");
        if (address == null) {
            throw new IllegalArgumentException("MyuSQL address is null");
        }
        String username = config.getString("username");
        if (username == null) {
            throw new IllegalArgumentException("MyuSQL usenname is null");
        }
        String password = config.getString("password");
        if (password == null) {
            throw new IllegalArgumentException("MyuSQL password is null");
        }
        String database = config.getString("database");
        if (database == null) {
            throw new IllegalArgumentException("MyuSQL database is null");
        }

        ConfigurationSection propertiesSection = config.getConfigurationSection("properties");
        Map<String, Object> properties = propertiesSection != null ? propertiesSection.getValues(false) : new HashMap<>();

        return new MysqlDatabaseConfig(address, username, password, database, properties);
    }

    // Getter
    public @NotNull String getAddress() {
        return address;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public @NotNull String getDatabase() {
        return database;
    }

    public @NotNull Map<String, Object> getProperties() {
        return properties;
    }
}
