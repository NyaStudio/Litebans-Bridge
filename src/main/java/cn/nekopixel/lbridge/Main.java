package cn.nekopixel.lbridge;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
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
    version = "1.1.0",
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
                logger.error("Failed to create config dir", e);
                return;
            }
        }

        Path configPath = dataDirectory.resolve("config.toml");
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getResourceAsStream("/config.toml")) {
                Files.copy(in, configPath);
            } catch (IOException e) {
                logger.error("Failed to create config.toml", e);
                return;
            }
        }

        Path messagePath = dataDirectory.resolve("messages.yml");
        if (!Files.exists(messagePath)) {
            try (InputStream in = getClass().getResourceAsStream("/messages.yml")) {
                Files.copy(in, messagePath);
            } catch (IOException e) {
                logger.error("Failed to create message.yml", e);
                return;
            }
        }

        try {
            try (InputStream configStream = Files.newInputStream(configPath)) {
                this.configManager = new ConfigManager(configStream);
            }
            
            Toml toml = new Toml().read(configPath.toFile());
            this.checker = new Checker(toml);

            try (InputStream messageStream = Files.newInputStream(messagePath)) {
                this.messageManager = new MessageManager(messageStream, configManager);
            }

            logger.info("Enabled!");
        } catch (Exception e) {
            logger.error("Failed to load config", e);
        }
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (checker == null || messageManager == null) {
            logger.error("Plugin not initialized!");
            return;
        }

        Player player = event.getPlayer();
        String username = player.getUsername();
        try {
            if (checker.isBanned(username)) {
                event.setResult(LoginEvent.ComponentResult.denied(
                        messageManager.getBanMessage(checker.getBanRecord())
                ));
            }
        } catch (Exception e) {
            logger.error("Error while check ban stats for {}", username, e);
            event.setResult(LoginEvent.ComponentResult.allowed());
        }
    }
}
