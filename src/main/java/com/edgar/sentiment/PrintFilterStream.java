package main.java.com.edgar.sentiment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import twitter4j.*;

import java.io.IOException;

public final class PrintFilterStream {

    private TwitterStream twitterStream;

    public PrintFilterStream() throws TwitterException, IOException {

        SaveTwit saveTwit = new SaveTwit();
        twitterStream = new TwitterStreamFactory().getInstance();

        RawStreamListener listener = new RawStreamListener() {
            @Override
            public void onMessage(String rawJSON) {
                JsonObject jsonObject = new JsonParser().parse(rawJSON).getAsJsonObject();
                try {
                    saveTwit.add(jsonObject.getAsJsonObject("user").get("screen_name").getAsString(),
                                 jsonObject.get("text").getAsString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        twitterStream.addListener(listener);

        // Filter
        FilterQuery filtre = new FilterQuery();
        String[] keywordsArray = {"a", "the", "i", "you", "u" };
        filtre.language("en");
        filtre.track(keywordsArray);

        twitterStream.filter(filtre);
    }

    public TwitterStream getTwitterStream() {
        return twitterStream;
    }
}
