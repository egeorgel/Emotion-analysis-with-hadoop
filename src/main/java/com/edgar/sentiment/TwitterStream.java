package com.edgar.sentiment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import twitter4j.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class TwitterStream {

    private twitter4j.TwitterStream twitterStream;
    private Tokenize tokenize;
    public TwitterStream () {
        try {
            tokenize = new Tokenize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenAll () throws Exception  {

        final SaveTwit saveTwit = new SaveTwit();
        twitterStream = new TwitterStreamFactory().getInstance();

        RawStreamListener listener = new RawStreamListener() {
            @Override
            public void onMessage(String rawJSON) {
                JsonObject jsonObject = new JsonParser().parse(rawJSON).getAsJsonObject();
                String text = jsonObject.get("text").getAsString();
                List<String> emojies = tokenize.findEmoji(text); // get emoji
                String feeling = "";

                if ( !emojies.isEmpty() ) {
                    text = tokenize.removeEmoji(text);
                    feeling = tokenize.emojisToFeelings(emojies);
                }

                try {
                    saveTwit.add(jsonObject.getAsJsonObject("user").get("screen_name").getAsString(),
                            text,
                            feeling);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(jsonObject.getAsJsonObject("user").get("screen_name").getAsString());
                System.out.println(jsonObject.get("text").getAsString());
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        twitterStream.addListener(listener);

        FilterQuery filtre = new FilterQuery();
        String[] keywordsArray = {"a", "the", "i", "you", "u", "my", "to", "and", "this", "is", "\'s", "in", "it", "of",
                "for", "on", "that", "at", "with", "do", "me", "be", "so", "not", "\'t", "are", "was", "but", "up", "out" };
        filtre.language("en");
        filtre.track(keywordsArray);
        twitterStream.filter(filtre);
    }



    public void train() throws IOException {
        final SaveTwitWithEmoji saveTwit = new SaveTwitWithEmoji();
        twitterStream = new TwitterStreamFactory().getInstance();

        RawStreamListener listener = new RawStreamListener() {
            @Override
            public void onMessage(String rawJSON) {
                JsonObject jsonObject = new JsonParser().parse(rawJSON).getAsJsonObject();
                String text = jsonObject.get("text").getAsString();
                List<String> emojies = tokenize.findEmoji(text); // get emoji

                if ( !emojies.isEmpty() ) { // check if we have emiji
                    String textWithoutEmo = tokenize.removeEmoji(text);

                    // Only take english twit Don't work because of com.google.guava
                    //if (tokenize.isEnglish(textWithoutEmo)) {
                        String feeling = tokenize.emojisToFeelings(emojies);
                        try {
                            saveTwit.add(jsonObject.getAsJsonObject("user").get("screen_name").getAsString(),
                                    feeling,
                                    textWithoutEmo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(jsonObject.getAsJsonObject("user").get("screen_name").getAsString());
                        System.out.println(jsonObject.get("text").getAsString());
                    //}

                }

            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        twitterStream.addListener(listener);
        //twitterStream.sample();
        // Filter
        FilterQuery filtre = new FilterQuery();
        String[] keywordsArray = {"a", "the", "i", "you", "u", "my", "to", "and", "this", "is", "\'s", "in", "it", "of",
                "for", "on", "that", "at", "with", "do", "me", "be", "so", "not", "\'t", "are", "was", "but", "up", "out" };
        filtre.language("en");
        filtre.track(keywordsArray);
        twitterStream.filter(filtre);
    }

    public twitter4j.TwitterStream getTwitterStream() {
        return twitterStream;
    }
}
