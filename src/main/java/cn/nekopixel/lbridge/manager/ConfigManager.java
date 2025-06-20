package cn.nekopixel.lbridge.manager;

import com.moandjiezana.toml.Toml;

import java.io.InputStream;

public class ConfigManager {
    private final Toml config;

    public ConfigManager(InputStream configFile) {
        this.config = new Toml().read(configFile);
    }

    public String getDatabaseHost() {
        return config.getString("database.host", "localhost");
    }

    public int getDatabasePort() {
        return Math.toIntExact(config.getLong("database.port", 3306L));
    }

    public String getDatabaseName() {
        return config.getString("database.name", "litebans");
    }

    public String getDatabaseUser() {
        return config.getString("database.user", "litebans");
    }

    public String getDatabasePassword() {
        return config.getString("database.pass", "");
    }

    public int getDatabaseMinimumIdle() {
        return Math.toIntExact(config.getLong("database.pool.minimum-idle", 5L));
    }

    public int getDatabaseMaximumPoolSize() {
        return Math.toIntExact(config.getLong("database.pool.maximum-pool-size", 10L));
    }

    public long getDatabaseConnectionTimeout() {
        return config.getLong("database.pool.connection-timeout", 30000L);
    }

    public long getRandomIdSeed() {
        return config.getLong("randomid.seed", 0L);
    }

    public int getRandomIdOffset() {
        return Math.toIntExact(config.getLong("randomid.offset", 12500L));
    }
} 