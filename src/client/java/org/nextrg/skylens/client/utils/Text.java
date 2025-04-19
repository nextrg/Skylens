package org.nextrg.skylens.client.utils;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Text {
    public static String getColorCode(String input) {
        return switch (input.replaceAll("_", "")) {
            case "darkblue" -> "§1";
            case "darkgreen" -> "§2";
            case "darkaqua" -> "§3";
            case "darkred" -> "§4";
            case "darkpurple", "epic" -> "§5";
            case "gold", "legendary" -> "§6";
            case "gray" -> "§7";
            case "darkgray" -> "§8";
            case "blue", "rare" -> "§9";
            case "green", "uncommon" -> "§a";
            case "aqua", "divine" -> "§b";
            case "red", "special" -> "§c";
            case "lightpurple", "mythic" -> "§d";
            case "yellow" -> "§e";
            default -> "§f";
        };
    }
    public static String getRarity(String input) {
        return switch(input) {
            case "light_purple" -> "mythic";
            case "gold" -> "legendary";
            case "dark_purple" -> "epic";
            case "blue" -> "rare";
            case "green" -> "uncommon";
            default -> "common";
        };
    }
    public static String getFormat(String input) {
        return switch (input) {
            case "obfuscated" -> "§k";
            case "bold" -> "§l";
            case "strikethrough" -> "§m";
            case "underline" -> "§n";
            case "italic" -> "§o";
            case "reset" -> "§r";
            default -> "";
        };
    }
    public static String getLiteral(String input) {
        return input.replace("literal{", "").replace("}", "");
    }
    public static String capitalize(String text) {
        return Arrays.stream(text.split(" "))
                .map(word -> Character.toTitleCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
    public static int rgbToHexa(Color color) {
        return (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }
    public static int hexToHexa(int hex, int alpha) {
        return (int) Long.parseLong(Integer.toHexString(alpha) + Integer.toHexString(hex).substring(2), 16);
    }
    public static int hexaToHex(int hexa) {
        return (int) Long.parseLong("ff" + Integer.toHexString(hexa).substring(2),16);
    }
}
