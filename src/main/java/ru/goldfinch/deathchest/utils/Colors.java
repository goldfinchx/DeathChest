package ru.goldfinch.deathchest.utils;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Colors {

    public static final ChatColor RED = ChatColor.of(new Color(252, 17, 17));
    public static final ChatColor WHITE = ChatColor.of(new Color(255, 255, 255));
    public static final ChatColor GRAY =  ChatColor.of(new Color(200, 200, 200));

    public static java.util.List<String> parseColors(final java.util.List<String> list){
        return list.stream().map(Colors::parseColors).collect(Collectors.toList());
    }

    public static java.util.List<String> parseColors(final String[] strings){
        List<String> parseStrings = new ArrayList<>();
        for (String s : strings) {
            String parseString = parseColors(s);
            parseStrings.add(parseString);
        }

        return parseStrings;
    }

    public static String parseColors(final String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
