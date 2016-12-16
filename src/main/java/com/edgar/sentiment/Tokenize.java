package com.edgar.sentiment;

/**
 * Created by Edgar on 02/12/2016.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import com.vdurmont.emoji.EmojiParser;

public class Tokenize {

    private LanguageDetector languageDetector;
    private TextObjectFactory textObjectFactory;

    public Tokenize() throws IOException {
        //load all languages:
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

        //build language detector:
        languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();

        //create a text object factory
        textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    }

    public boolean isEnglish (String input) {
        //query:
        TextObject textObject = textObjectFactory.forText(input);
        Optional<LdLocale> lang = languageDetector.detect(textObject);

        return lang.get().getLanguage().equals("en");
    }

    public List<String> findEmoji(String input) {

        String parsStr = EmojiParser.parseToAliases(input);

        final Pattern pattern = Pattern.compile(":[|_1-9a-zA-Z]*:");
        final Matcher matcher = pattern.matcher(parsStr);
        List<String> emojis = new ArrayList<>();

        while (matcher.find()) {
            emojis.add(matcher.group(0).split(":")[1]);
        }

        return emojis;
    }

    public String removeEmoji(String input) {
        return EmojiParser.removeAllEmojis(input);
    }

    /**
     * Take a list of emojis (pars in word) and return a string of emotion
     * @param inputOfEmojis
     * @return
     */
    public String emojisToFeelings (List<String> inputOfEmojis) {

        HashMap<String, Integer> mapFeeling = new HashMap<>();
        mapFeeling.put("happy", 0);
        mapFeeling.put("funny", 0);
        mapFeeling.put("love", 0);

        mapFeeling.put("sad", 0);
        mapFeeling.put("angry", 0);

        mapFeeling.put("sport", 0);
        mapFeeling.put("holiday", 0);

        for (String strEmo : inputOfEmojis) {

            happy(mapFeeling, strEmo);
            funny(mapFeeling, strEmo);
            love(mapFeeling, strEmo);
            sad(mapFeeling, strEmo);
            angry(mapFeeling, strEmo);
            sport(mapFeeling, strEmo);
            holiday(mapFeeling, strEmo);
        }

        return compute(mapFeeling);
    }

    private void happy (HashMap<String, Integer> mapFeeling, String strEmo) {
        if ( strEmo.contains("kissing") || strEmo.contains("smile") || strEmo.contains("wine") || strEmo.contains("hug") ||
                strEmo.contains("yum") || strEmo.contains("chocolate") || strEmo.contains("flipped_face")) {
            mapFeeling.put("happy", mapFeeling.get("happy") + 1);
        }
    }

    private void funny (HashMap<String, Integer> mapFeeling, String strEmo) {
        if (strEmo.contains("grimacing") || strEmo.contains("tongue") || strEmo.contains("laughing")) {
            mapFeeling.put("funny", mapFeeling.get("funny") + 1);
        }
    }

    private void love (HashMap<String, Integer> mapFeeling, String strEmo) {
        if ( ( strEmo.contains("bride") || strEmo.contains("couple") || strEmo.contains("heart")  || strEmo.contains("lips") ||
                strEmo.contains("rose") ) && !strEmo.equals("broken_heart") ) {
            mapFeeling.put("love", mapFeeling.get("love") + 1);
        }
    }

    private void sad (HashMap<String, Integer> mapFeeling, String strEmo) {
        if ( strEmo.equals("broken_heart") ) {
            mapFeeling.put("love", mapFeeling.get("love") - 1);
            mapFeeling.put("sad", mapFeeling.get("sad") + 1);
        }

        if ( strEmo.contains("cloud") || strEmo.contains("sleepy") || strEmo.contains("-1") || strEmo.contains("confused") ||
                strEmo.contains("frowning") || strEmo.contains("sob") || strEmo.contains("tired_face") || strEmo.contains("tired_face") ||
                strEmo.contains("fearful") || strEmo.contains("disappointed") || strEmo.contains("sick") || strEmo.contains("injured") ||
                strEmo.contains("worried") || strEmo.contains("anguished")) {
            mapFeeling.put("sad", mapFeeling.get("sad") + 1);
        }
    }

    private void angry (HashMap<String, Integer> mapFeeling, String strEmo) {
        if ( strEmo.contains("skull_crossbones") || strEmo.contains("rage") || strEmo.contains("triumph") ||
                strEmo.contains("gun") || strEmo.contains("sweat") ) {
            mapFeeling.put("angry", mapFeeling.get("angry") + 1);
        }
    }

    private void sport (HashMap<String, Integer> mapFeeling, String strEmo) {
        if (strEmo.contains("ski") ||strEmo.contains("golf") || strEmo.contains("tennis") || strEmo.contains("hockey") ||
                strEmo.contains("volleyball") || strEmo.contains("surfer") || strEmo.contains("run")) {
            mapFeeling.put("sport", mapFeeling.get("sport") + 1);
        }
    }

    private void holiday (HashMap<String, Integer> mapFeeling, String strEmo) {
        if (strEmo.contains("train") || strEmo.contains("metro") || strEmo.contains("monorail") || strEmo.contains("tent") ||strEmo.contains("snow") ||
                strEmo.contains("planted_umbrella") || strEmo.contains("ski") || strEmo.contains("locomotive") || strEmo.length() == 2 ||
                strEmo.contains("air") || strEmo.contains("desert") || strEmo.contains("rail") || strEmo.contains("helicopter") ||
                strEmo.contains("crab") || strEmo.contains("statue")) { // strEmo.length() == 2 => flag
            mapFeeling.put("holiday", mapFeeling.get("holiday") + 1);
        }
    }

    private String compute (HashMap<String, Integer> mapFeeling) {
        int max = mapFeeling.get("happy");
        String feeling = "happy";
        if (max < mapFeeling.get("funny")) {
            max =  mapFeeling.get("funny");
            feeling = "funny";
        }
        if (max < mapFeeling.get("love")) {
            max =  mapFeeling.get("love");
            feeling = "love";
        }
        if (max < mapFeeling.get("sad")) {
            max =  mapFeeling.get("sad");
            feeling = "sad";
        }
        if (max < mapFeeling.get("angry")) {
            max =  mapFeeling.get("angry");
            feeling = "angry";
        }
        if (max < mapFeeling.get("sport")) {
            max =  mapFeeling.get("sport");
            feeling = "sport";
        }
        if (max < mapFeeling.get("holiday")) {
            max =  mapFeeling.get("holiday");
            feeling = "holiday";
        }

        return feeling;
    }

}
