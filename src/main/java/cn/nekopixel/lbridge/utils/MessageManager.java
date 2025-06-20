package cn.nekopixel.lbridge.utils;

import cn.nekopixel.lbridge.entity.BanRecord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MessageManager {
    private final Map<String, Object> messages;
    private final SimpleDateFormat dateFormat;

    @SuppressWarnings("unchecked")
    public MessageManager(InputStream messageFile) {
        Yaml yaml = new Yaml();
        messages = yaml.load(messageFile);
        dateFormat = new SimpleDateFormat(messages.get("time_format").toString());
    }

    @SuppressWarnings("unchecked")
    private String formatDuration(long start, long end) {
        if (end == 0) {
            Object forever = messages.get("duration.forever");
            return forever != null ? forever.toString() : "永久";
        }

        if (end < System.currentTimeMillis()) {
            Object expired = messages.get("duration.expired");
            return expired != null ? expired.toString() : "已过期";
        }

        long duration = end - start;
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

        if (sb.length() == 0) {
            sb.append(String.format(format, 1, getMessageWithDefault("duration.minute", "分钟")));
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

        return parseColorCodes(message);
    }

    private String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    private static class FormatState {
        TextColor color = null;
        boolean obfuscated = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underlined = false;
        boolean italic = false;

        void reset() {
            obfuscated = false;
            bold = false;
            strikethrough = false;
            underlined = false;
            italic = false;
        }

        Component apply(Component component) {
            return component
                    .color(color)
                    .decoration(TextDecoration.OBFUSCATED, obfuscated)
                    .decoration(TextDecoration.BOLD, bold)
                    .decoration(TextDecoration.STRIKETHROUGH, strikethrough)
                    .decoration(TextDecoration.UNDERLINED, underlined)
                    .decoration(TextDecoration.ITALIC, italic);
        }
    }

    private Component parseColorCodes(String message) {
        net.kyori.adventure.text.TextComponent.Builder builder = Component.text();
        String[] parts = message.split("\n");
        
        for (String part : parts) {
            String[] sections = part.split("(?=&[0-9a-fk-or])");
            FormatState state = new FormatState();

            for (String section : sections) {
                if (section.isEmpty()) continue;

                if (section.startsWith("&")) {
                    String text = section.substring(2);
                    char code = section.charAt(1);

                    switch (code) {
                        // 颜色代码（重置所有格式）
                        case '0' -> {
                            state.color = NamedTextColor.BLACK;
                            state.reset();
                        }
                        case '1' -> {
                            state.color = NamedTextColor.DARK_BLUE;
                            state.reset();
                        }
                        case '2' -> {
                            state.color = NamedTextColor.DARK_GREEN;
                            state.reset();
                        }
                        case '3' -> {
                            state.color = NamedTextColor.DARK_AQUA;
                            state.reset();
                        }
                        case '4' -> {
                            state.color = NamedTextColor.DARK_RED;
                            state.reset();
                        }
                        case '5' -> {
                            state.color = NamedTextColor.DARK_PURPLE;
                            state.reset();
                        }
                        case '6' -> {
                            state.color = NamedTextColor.GOLD;
                            state.reset();
                        }
                        case '7' -> {
                            state.color = NamedTextColor.GRAY;
                            state.reset();
                        }
                        case '8' -> {
                            state.color = NamedTextColor.DARK_GRAY;
                            state.reset();
                        }
                        case '9' -> {
                            state.color = NamedTextColor.BLUE;
                            state.reset();
                        }
                        case 'a' -> {
                            state.color = NamedTextColor.GREEN;
                            state.reset();
                        }
                        case 'b' -> {
                            state.color = NamedTextColor.AQUA;
                            state.reset();
                        }
                        case 'c' -> {
                            state.color = NamedTextColor.RED;
                            state.reset();
                        }
                        case 'd' -> {
                            state.color = NamedTextColor.LIGHT_PURPLE;
                            state.reset();
                        }
                        case 'e' -> {
                            state.color = NamedTextColor.YELLOW;
                            state.reset();
                        }
                        case 'f' -> {
                            state.color = NamedTextColor.WHITE;
                            state.reset();
                        }
                        case 'k' -> state.obfuscated = true;
                        case 'l' -> state.bold = true;
                        case 'm' -> state.strikethrough = true;
                        case 'n' -> state.underlined = true;
                        case 'o' -> state.italic = true;
                        case 'r' -> {
                            state.color = NamedTextColor.WHITE;
                            state.reset();
                        }
                        default -> builder.append(Component.text(section));
                    }

                    if (!text.isEmpty()) {
                        builder.append(state.apply(Component.text(text)));
                    }
                } else {
                    builder.append(state.apply(Component.text(section)));
                }
            }
            builder.append(Component.newline());
        }

        return builder.build();
    }
} 