package main.java.com.edgar.sentiment;

import twitter4j.*;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {


    public static void main( String[] args ) throws IOException {
        try {
            PrintFilterStream printFilterStream = new PrintFilterStream();
            try {
                Thread.sleep(10000);                 //10s.
                printFilterStream.getTwitterStream().cleanUp();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }



    }
}
