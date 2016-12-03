package com.edgar.sentiment;

/**
 * Created by Edgar on 02/12/2016.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vdurmont.emoji.EmojiParser;

public class Tokenize {

    public static List<String> findEmoji(String input) {

        String parsStr = EmojiParser.parseToAliases(input);

        final Pattern pattern = Pattern.compile(":[|_1-9a-zA-Z]*:");
        final Matcher matcher = pattern.matcher(parsStr);
        List<String> emojis = new ArrayList<>();

        while (matcher.find()) {
            emojis.add(matcher.group(0).split(":")[1]);
        }

        return emojis;
    }

    public static String removeEmoji(String input) {
        return EmojiParser.removeAllEmojis(input);
    }

}
