package cn.nekopixel.lbridge.utils;

import cn.nekopixel.lbridge.entity.BanRecord;
import cn.nekopixel.lbridge.mapper.BanMapper;
import com.moandjiezana.toml.Toml;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class Checker {
    private final SqlSessionFactory sqlSessionFactory;
    private BanRecord lastBanRecord;

    public Checker(Toml config) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL 驱动加载失败", e);
        }

        Toml dbConfig = config.getTable("database");
        Toml poolConfig = dbConfig.getTable("pool");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai",
                dbConfig.getString("host"),
                dbConfig.getLong("port").intValue(),
                dbConfig.getString("name")));
        hikariConfig.setUsername(dbConfig.getString("user"));
        hikariConfig.setPassword(dbConfig.getString("pass"));
        
        hikariConfig.setMinimumIdle(poolConfig.getLong("minimum-idle").intValue());
        hikariConfig.setMaximumPoolSize(poolConfig.getLong("maximum-pool-size").intValue());
        hikariConfig.setConnectionTimeout(poolConfig.getLong("connection-timeout"));

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);

        try {
            Environment environment = new Environment("development",
                    new JdbcTransactionFactory(),
                    dataSource);

            Configuration configuration = new Configuration(environment);
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.addMapper(BanMapper.class);

            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        } catch (Exception e) {
            if (dataSource != null) {
                dataSource.close();
            }
            throw new RuntimeException("初始化 MyBatis 失败", e);
        }
    }

    public boolean isBanned(String username) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            BanMapper mapper = session.getMapper(BanMapper.class);
            
            String uuid = mapper.findLatestUuidByName(username);
            if (uuid == null) {
                return false;
            }

            lastBanRecord = mapper.findActiveBan(uuid, System.currentTimeMillis());
            return lastBanRecord != null;
        }
    }

    public BanRecord getBanRecord() {
        return lastBanRecord;
    }
}
