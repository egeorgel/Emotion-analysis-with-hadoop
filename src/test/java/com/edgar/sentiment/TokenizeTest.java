package com.edgar.sentiment;

import com.vdurmont.emoji.EmojiParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Edgar on 03/12/2016.
 */
public class TokenizeTest {

    @Test
    public void testFindEmoji() {
        String str = "An :grinning:awesome :smiley:string &#128516;with a few :wink:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str);

        List<String> emoji = Tokenize.findEmoji(strWithEmo);
        Assert.assertEquals(emoji.get(0), "grinning");
        Assert.assertEquals(emoji.get(1), "smiley");
        Assert.assertEquals(emoji.get(2), "smile");
        Assert.assertEquals(emoji.get(3), "wink");
    }

    @Test
    public void testFindEmojiB() {
        String str = "An :boy|type_1_2: :smiley:string; with a few :person_with_pouting_face|type_6:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str);

        List<String> emoji = Tokenize.findEmoji(strWithEmo);
        Assert.assertEquals(emoji.get(0), "boy|type_1_2");
        Assert.assertEquals(emoji.get(1), "smiley");
        Assert.assertEquals(emoji.get(2), "person_with_pouting_face|type_6");
    }

    @Test
    public void testRemoveEmo() {
        String str = "An :boy|type_1_2: :smiley:string; with a few :person_with_pouting_face|type_6:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str);

        String strWithOutEmo = Tokenize.removeEmoji(strWithEmo);
        Assert.assertEquals(strWithOutEmo ,"An  string; with a few emojis!");
    }
}
