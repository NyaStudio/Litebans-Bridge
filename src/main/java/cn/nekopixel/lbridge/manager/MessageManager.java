package cn.nekopixel.lbridge.manager;

import cn.nekopixel.lbridge.entity.BanRecord;
import cn.nekopixel.lbridge.utils.ColorParser;
import cn.nekopixel.lbridge.utils.randomID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MessageManager {
    private final Map<String, Object> messages;
    private final SimpleDateFormat dateFormat;
    private final ConfigManager configManager;
    private randomID randomId;

    @SuppressWarnings("unchecked")
    public MessageManager(InputStream messageFile, ConfigManager configManager) {
        Yaml yaml = new Yaml();
        messages = yaml.load(messageFile);
        dateFormat = new SimpleDateFormat(messages.get("time_format").toString());
        this.configManager = configManager;
    }

    private synchronized randomID getRandomId() {
        if (randomId == null) {
            long seed = configManager.getRandomIdSeed();
            int offset = configManager.getRandomIdOffset();
            randomId = new randomID(seed, offset);
        }
        return randomId;
    }

    public Component getBanMessage(BanRecord ban) {
        if (ban == null) {
            return Component.text("无效的封禁记录")
                    .color(NamedTextColor.RED);
        }

        String baseMessage = getMessageWithDefault("banned_message_base", 
            "&c你已被服务器封禁！\n\n封禁时间: $dateStart\n封禁者: $executor\n原因: $reason");
        String appealMessage = getMessageWithDefault("banned_message_appeal_message", "");
        String banMessage;

        if (ban.getUntil() == 0) {
            banMessage = getMessageWithDefault("banned_message_permanent", 
                "$base\n你已被永久封禁！\n$appealMessage");
        } else {
            banMessage = getMessageWithDefault("banned_message", 
                "$base\n到期时间: $duration\n$appealMessage");
        }

        String message = banMessage
                .replace("$base", baseMessage)
                .replace("$appealMessage", appealMessage)
                .replace("$dateStart", formatDate(ban.getTime()))
                .replace("$executor", ban.getBannedByName())
                .replace("$reason", ban.getReason())
                .replace("$duration", formatDuration(ban.getTime(), ban.getUntil()));

        if (message.contains("$idRandom")) {
            message = message.replace("$idRandom", getRandomId().convert(ban.getId()));
        }
        
        message = message.replace("$id", String.valueOf(ban.getId()));

        return ColorParser.parse(message);
    }

    private String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    private String formatDuration(long start, long end) {
        if (end == 0) {
            Object forever = messages.get("duration.forever");
            return forever != null ? forever.toString() : "永久";
        }

        if (end < System.currentTimeMillis()) {
            Object expired = messages.get("duration.expired");
            return expired != null ? expired.toString() : "已过期";
        }

        long duration = end - System.currentTimeMillis();
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        StringBuilder sb = new StringBuilder();
        String separator = getMessageWithDefault("duration.separator", ", ");
        String format = getMessageWithDefault("duration.format", "%d %s");

        if (days > 0) {
            sb.append(String.format(format, days, 
                days == 1 ? getMessageWithDefault("duration.day", "天") 
                         : getMessageWithDefault("duration.days", "天")));
        }
        if (hours % 24 > 0) {
            if (sb.length() > 0) sb.append(separator);
            sb.append(String.format(format, hours % 24, 
                hours % 24 == 1 ? getMessageWithDefault("duration.hour", "小时") 
                               : getMessageWithDefault("duration.hours", "小时")));
        }
        if (minutes % 60 > 0) {
            if (sb.length() > 0) sb.append(separator);
            sb.append(String.format(format, minutes % 60, 
                minutes % 60 == 1 ? getMessageWithDefault("duration.minute", "分钟") 
                                : getMessageWithDefault("duration.minutes", "分钟")));
        }
        if (seconds % 60 > 0) {
            if (sb.length() > 0) sb.append(separator);
            sb.append(String.format(format, seconds % 60, 
                seconds % 60 == 1 ? getMessageWithDefault("duration.second", "秒") 
                                : getMessageWithDefault("duration.seconds", "秒")));
        }

        if (sb.length() == 0) {
            sb.append(String.format(format, 1, getMessageWithDefault("duration.second", "秒")));
        }

        return sb.toString();
    }

    private String getMessageWithDefault(String path, String defaultValue) {
        Object value = messages;
        for (String key : path.split("\\.")) {
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get(key);
                if (value == null) {
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        }
        return value.toString();
    }
    
    public String getRandomIdInfo() {
        return getRandomId().getInfo();
    }
} 