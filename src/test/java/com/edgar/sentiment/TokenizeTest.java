package com.edgar.sentiment;

import com.vdurmont.emoji.EmojiParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by Edgar on 03/12/2016.
 */
public class TokenizeTest {
    
    private Tokenize tokenize;
    
    @Before
    public void setUp() throws IOException {
        tokenize = new Tokenize();
    }

    @Test
    public void testIsEnglishFransh() {
        String str = "Salut moi c'est Paul j'aime les frites";
        Assert.assertFalse(tokenize.isEnglish(str));
    }

    @Test
    public void testIsEnglish_input_English() {
        String str = "RT @clarabeninmusic: Um all my fa\n" +
                "                        ves in one night?? If you have other plans tonight, cancel them \n" +
                "                        and come here instead.  https://t.co/lK\\xE2\\x80\\xA6";
        Assert.assertTrue(tokenize.isEnglish(str));
    }

    @Test
    public void testIsEnglish_input_English_2() {
        String str = "RT @onplanet612: Rong putting the\n" +
                "                         mic on eunji's head  #eunrong #\\xEC\\x9D\\x80\\xEC\\xB4\\x88 https:/\n" +
                "                        /t.co/AB1sITkyq6          ";
        Assert.assertTrue(tokenize.isEnglish(str));
    }

    @Test
    public void testFindEmoji() {
        String str = "An :grinning:awesome :smiley:string &#128516;with a few :wink:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji

        List<String> emoji = tokenize.findEmoji(strWithEmo);
        Assert.assertEquals(emoji.get(0), "grinning");
        Assert.assertEquals(emoji.get(1), "smiley");
        Assert.assertEquals(emoji.get(2), "smile");
        Assert.assertEquals(emoji.get(3), "wink");
    }

    @Test
    public void testFindEmojiB() {
        String str = "An :boy|type_1_2: :smiley:string; with a few :person_with_pouting_face|type_6:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji

        List<String> emoji = tokenize.findEmoji(strWithEmo);
        Assert.assertEquals(emoji.get(0), "boy|type_1_2");
        Assert.assertEquals(emoji.get(1), "smiley");
        Assert.assertEquals(emoji.get(2), "person_with_pouting_face|type_6");
    }

    @Test
    public void testRemoveEmo() {
        String str = "An :boy|type_1_2: :smiley:string; with a few :person_with_pouting_face|type_6:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji

        String strWithOutEmo = tokenize.removeEmoji(strWithEmo);
        Assert.assertEquals(strWithOutEmo ,"An  string; with a few emojis!");
    }

    @Test
    public void testEmojisToFeelings_happy() {
        String str = "An :grinning:awesome :smiley:string &#128516;with a few :wink:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji
        List<String> emoji = tokenize.findEmoji(strWithEmo);

        Assert.assertEquals(tokenize.emojisToFeelings(emoji), "happy");
    }

    @Test
    public void testEmojisToFeelings_happy_funny() {
        String str = "An :laughing:awesome :smiley:string with a few :laughing:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji
        List<String> emoji = tokenize.findEmoji(strWithEmo);

        Assert.assertEquals(tokenize.emojisToFeelings(emoji), "funny");
    }

    @Test
    public void testEmojisToFeelings_happy_love() {
        String str = "An :couple_with_heart:awesome :smiley:string with a few :couple_with_heart_man_man:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji
        List<String> emoji = tokenize.findEmoji(strWithEmo);

        Assert.assertEquals(tokenize.emojisToFeelings(emoji), "love");
    }

    @Test
    public void testEmojisToFeelings_happy_sad() {
        String str = "An :sick:awesome :worried:string with a few :couple_with_heart_man_man:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji
        List<String> emoji = tokenize.findEmoji(strWithEmo);

        Assert.assertEquals(tokenize.emojisToFeelings(emoji), "sad");
    }

    @Test
    public void testEmojisToFeelings_happy_angry() {
        String str = "An :triumph:awesome :worried:string with a few :triumph:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji
        List<String> emoji = tokenize.findEmoji(strWithEmo);

        Assert.assertEquals(tokenize.emojisToFeelings(emoji), "angry");
    }

    @Test
    public void testEmojisToFeelings_happy_sport() {
        String str = "An :ski:awesome :ski:string with a few :triumph:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji
        List<String> emoji = tokenize.findEmoji(strWithEmo);

        Assert.assertEquals(tokenize.emojisToFeelings(emoji), "sport");
    }

    @Test
    public void testEmojisToFeelings_happy_holiday() {
        String str = "An :small_airplane:awesome :airplane:string with a few :triumph:emojis!";
        String strWithEmo = EmojiParser.parseToUnicode(str); // creat real emoji
        List<String> emoji = tokenize.findEmoji(strWithEmo);

        Assert.assertEquals(tokenize.emojisToFeelings(emoji), "holiday");
    }


}
