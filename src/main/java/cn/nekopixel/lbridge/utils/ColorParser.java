package cn.nekopixel.lbridge.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ColorParser {
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

    public static Component parse(String message) {
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