package cn.nekopixel.lbridge;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import cn.nekopixel.lbridge.utils.Checker;
import cn.nekopixel.lbridge.manager.ConfigManager;
import cn.nekopixel.lbridge.manager.MessageManager;
import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
    id = "litebans-bridge",
    name = "Litebans-Bridge",
    version = "1.0.0",
    description = "A bridge between Velocity and Litebans",
    authors = {"BLxcwg666"}
)
public class Main {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private Checker checker;
    private MessageManager messageManager;
    private ConfigManager configManager;

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                logger.error("无法创建配置目录", e);
                return;
            }
        }

        Path configPath = dataDirectory.resolve("config.toml");
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getResourceAsStream("/config.toml")) {
                Files.copy(in, configPath);
            } catch (IOException e) {
                logger.error("无法创建默认配置文件", e);
                return;
            }
        }

        Path messagePath = dataDirectory.resolve("messages.yml");
        if (!Files.exists(messagePath)) {
            try (InputStream in = getClass().getResourceAsStream("/messages.yml")) {
                Files.copy(in, messagePath);
            } catch (IOException e) {
                logger.error("无法创建消息配置文件", e);
                return;
            }
        }

        try {
            // 创建ConfigManager
            try (InputStream configStream = Files.newInputStream(configPath)) {
                this.configManager = new ConfigManager(configStream);
            }
            
            // 创建Checker（使用旧的Toml方式，保持兼容性）
            Toml toml = new Toml().read(configPath.toFile());
            this.checker = new Checker(toml);

            // 创建MessageManager（使用新的ConfigManager）
            try (InputStream messageStream = Files.newInputStream(messagePath)) {
                this.messageManager = new MessageManager(messageStream, configManager);
            }

            // 显示RandomID配置信息
            logger.info("RandomID配置: {}", messageManager.getRandomIdInfo());
            logger.info("加载完成！");
        } catch (Exception e) {
            logger.error("无法加载配置文件", e);
        }
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        if (checker == null || messageManager == null) {
            logger.error("插件未初始化，跳过检查");
            return;
        }

        String username = event.getUsername();
        try {
            if (checker.isBanned(username)) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                        messageManager.getBanMessage(checker.getBanRecord())
                ));
            }
        } catch (Exception e) {
            logger.error("检查玩家 {} 的封禁状态时发生错误", username, e);
            event.setResult(PreLoginEvent.PreLoginComponentResult.allowed());
        }
    }
}
