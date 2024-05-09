package com.HiWord9.RPRenames.util.config;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.rename.Rename;
import net.minecraft.item.Item;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

public class PropertiesHelper {

    public static String getFirstName(String nbtDisplayName) {
        return getFirstName(nbtDisplayName, null);
    }

    public static String getFirstName(String nbtDisplayName, @Nullable ArrayList<Item> items) {
        String name = parseEscapes(nbtDisplayName);
        if (name.startsWith("pattern:") || name.startsWith("ipattern:")) {
            if (name.startsWith("i")) {
                name = name.substring(1);
            }
            name = name.replaceFirst("pattern:", "");
            name = name.replace("*", "");
            name = name.replace("?", "_");
        } else if (name.startsWith("regex:") || name.startsWith("iregex")) {
            if (name.startsWith("i")) {
                name = name.substring(1);
            }
            name = name.replaceFirst("regex:", "");
            name = name.replace(".*", "");
            name = name.replace(".+", "_");

            String originalRegex = name;
            name = solveRegex(name);
            try {
                if (!name.matches(originalRegex)) {
                    RPRenames.LOGGER.error("Couldn't get valid string from regex" + (items != null ? " for " + items : ""));
                    RPRenames.LOGGER.error("regex:" + originalRegex);
                    RPRenames.LOGGER.error("received string:" + name);
                }
            } catch (PatternSyntaxException e) {
                RPRenames.LOGGER.error("INVALID REGEX");
            }
        }
        return name;
    }

    public static String parseEscapes(String string) {
        char[] chars = string.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\\') {
                if (chars[i + 1] == 'u') {
                    char unicode;
                    String unicodeNumbers = string.substring(i + 2, i + 6);
                    try {
                        unicode = (char) Integer.parseInt(unicodeNumbers, 16);
                    } catch (Exception e) {
                        stringBuilder.append(chars[i]);
                        RPRenames.LOGGER.warn("Invalid unicode \"" + unicodeNumbers + "\" for String: " + string);
                        continue;
                    }
                    i += 5;
                    stringBuilder.append(unicode);
                } else if (chars[i + 1] == 'n') {
                    stringBuilder.append("\n");
                } else if (chars[i + 1] == 'r') {
                    stringBuilder.append("\r");
                } else if (chars[i + 1] == 'f') {
                    stringBuilder.append("\f");
                } else if (chars[i + 1] == 't') {
                    stringBuilder.append("\t");
                } else {
                    stringBuilder.append(chars[i]);
                    i++;
                }
            } else {
                stringBuilder.append(chars[i]);
            }
        }
        return stringBuilder.toString();
    }

    public static String solveRegex(String string) {
        if (!string.startsWith("(")) {
            string = "(" + string + ")";
        }
        return initialSolveRegex(string);
    }

    public static String initialSolveRegex(String string) {
        try {
            StringBuilder builder = new StringBuilder();
            char[] chars = string.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                StringBuilder builder2 = new StringBuilder();
                if (chars[i] == '[') {
                    if (chars[i + 1] != '^') {
                        builder2.append(chars[i + 1]);
                        i += 2;
                        while (chars[i] != ']') {
                            i++;
                        }
                    } else {
                        int start = i;
                        i += 3;
                        while (chars[i] != ']') {
                            i++;
                        }
                        int ch = chars[start + 2];
                        int count = 0;
                        while (count != 65536) {
                            if (string.substring(start, i + 1).matches(String.valueOf((char) ch))) {
                                break;
                            }
                            ch++;
                            count++;
                            if (ch == 65536) {
                                ch = 0;
                            }
                            if (count == 65536) {
                                ch = 65535;
                            }
                        }
                        builder2.append((char) ch);
                    }
                } else if (chars[i] == '(') {
                    StringBuilder builder3 = new StringBuilder();
                    int brackets = 0;
                    ArrayList<Character> bracketsOrder = new ArrayList<>();
                    while (i + 1 < chars.length) {
                        i++;
                        if (chars[i] == '(') {
                            bracketsOrder.add('(');
                            brackets++;
                        } else if (chars[i] == ')' && (bracketsOrder.isEmpty() || bracketsOrder.get(bracketsOrder.size() - 1) != '[')) {
                            if (brackets == 0) {
                                break;
                            }
                            bracketsOrder.add(')');
                            brackets--;
                        } else if (chars[i] == '[') {
                            bracketsOrder.add('[');
                            brackets++;
                        } else if (chars[i] == ']' && chars[i - 1] != '[') {
                            bracketsOrder.add(']');
                            brackets--;
                        }
                        builder3.append(chars[i]);
                    }
                    builder3 = new StringBuilder(initialSolveRegex(builder3.toString()));
                    if (!builder3.toString().startsWith("|") && !builder3.toString().endsWith("|") && !builder3.toString().contains("||") && !builder3.isEmpty()) {
                        for (int i1 = 0; i1 != builder3.length() && builder3.charAt(i1) != '|'; i1++) {
                            builder2.append(builder3.charAt(i1));
                        }
                    }
                } else if (chars[i] != '^' && chars[i] != '$') {
                    if (chars[i] == '\\') {
                        i++;
                    }
                    builder2.append(chars[i]);
                }

                if (i + 1 < chars.length && chars[i + 1] == '{') {
                    StringBuilder builder3 = new StringBuilder();
                    i += 2;
                    while (chars[i] != '}') {
                        builder3.append(chars[i]);
                        i++;
                    }
                    int s = builder3.indexOf(",") == -1 ? builder3.length() : builder3.indexOf(",");
                    if (s > 0 || s == builder3.length() - 1) {
                        builder.append(String.valueOf(builder2).repeat(Math.max(0, Integer.parseInt(builder3.substring(0, s)))));
                        continue;
                    } else if (s == 0) {
                        builder.append(String.valueOf(builder2).repeat(Math.max(0, Integer.parseInt(builder3.substring(s + 1)))));
                        continue;
                    }
                } else if (i + 1 < chars.length) {
                    if (chars[i + 1] == '*' || chars[i + 1] == '+' || chars[i + 1] == '?') {
                        i++;
                        if (chars[i] == '*' || chars[i] == '?') {
                            continue;
                        }
                    }
                }
                builder.append(builder2);
            }
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static ArrayList<Text> parseCustomDescription(String description) {
        ArrayList<Text> lines = new ArrayList<>();
        String[] split = description
                .replaceAll("\\\\&", String.valueOf(Formatting.FORMATTING_CODE_PREFIX))
                .split("\n");

        for (String s : split) {
            MutableText line = Text.empty();
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                MutableText text = Text.empty();

                if (chars[i] == '\\'
                        && i != chars.length - 1
                        && chars[i + 1] == '#'
                        && i + 7 < chars.length) {
                    StringBuilder color = new StringBuilder();
                    for (int j = 2; j < 8; j++) {
                        color.append(chars[i + j]);
                    }
                    if (color.toString().matches("[0-9a-fA-F]*")) {
                        text.fillStyle(Style.EMPTY.withColor(Integer.parseInt(color.toString(), 16)));
                    } else {
                        text.append("\\#" + color);
                    }
                    i += 8;
                }

                StringBuilder stringBuilder = new StringBuilder();
                while (i < chars.length) {
                    if (chars[i] == '\\' && chars[i + 1] == '#') {
                        i--;
                        break;
                    }
                    stringBuilder.append(chars[i]);
                    i++;
                }

                text.append(Text.translatable(stringBuilder.toString()));
                line.append(text);
            }
            lines.add(line);
        }
        return lines;
    }

    public static boolean matchesRange(int num, String list) {
        return matchesRange(num, list, null);
    }

    public static boolean matchesRange(int num, String list, @Nullable Item damagedItem) {
        if (list == null) return true;
        if (!list.contains(" ") && !list.contains("-") && !list.contains("%")) {
            try {
                return num == Integer.parseInt(getFirstValueInList(list));
            } catch (Exception e) {
                RPRenames.LOGGER.error("Could not get valid Damage value from list: {}", list);
                e.printStackTrace();
                return false;
            }
        }

        for (String s : splitList(list)) {
            if (s.contains("-")) {
                if (s.indexOf("-") == s.length() - 1) {
                    String min = s.substring(0, s.length() - 1);
                    if (min.endsWith("%")) {
                        min = min.substring(0, min.length() - 1);

                        Rename.Damage minDamage = new Rename.Damage(Integer.parseInt(min), true);

                        if (num >= minDamage.getParsedDamage(damagedItem)) return true;
                    } else {
                        if (num >= Integer.parseInt(min)) return true;
                    }
                } else {
                    int i = s.indexOf('-');
                    String min = s.substring(0, i);
                    String max = s.substring(i + 1);
                    if (max.endsWith("%")) {
                        max = max.replace("%", "");

                        Rename.Damage minDamage = new Rename.Damage(Integer.parseInt(min), true);
                        Rename.Damage maxDamage = new Rename.Damage(Integer.parseInt(max), true);

                        if (num >= minDamage.getParsedDamage(damagedItem)
                                && num <= maxDamage.getParsedDamage(damagedItem)) {
                            return true;
                        }
                    } else {
                        if (num >= Integer.parseInt(min) && num <= Integer.parseInt(max)) return true;
                    }
                }
            } else if (num == Integer.parseInt(s)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> splitList(String list) {
        ArrayList<String> split = new ArrayList<>();
        if (list.contains(" ")) {
            int i = 0;
            int i1 = 0;
            while (i <= list.length()) {
                if (i == list.length() || list.charAt(i) == ' ') {
                    split.add(list.substring(i1, i));
                    i1 = i + 1;
                }
                i++;
            }
        } else {
            split.add(list);
        }
        return split;
    }

    public static int parseDamagePercent(int percent, Item item) {
        int maxDamage = item.getMaxDamage();
        return maxDamage * percent / 100;
    }

    public static String getFirstValueInList(String list) {
        StringBuilder builder = new StringBuilder();
        int n = 0;
        while (n < list.length()) {
            if (list.charAt(n) != '-' && list.charAt(n) != ' ') {
                builder.append(list.charAt(n));
                n++;
            } else {
                break;
            }
        }
        if (list.contains("%")) {
            builder.append("%");
        }
        return builder.toString();
    }
}
