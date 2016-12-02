package com.edgar.sentiment;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {


    public static void main( String[] args ) throws IOException {
        try {
            PrintFilterStream printFilterStream = new PrintFilterStream();
            try {
                Thread.sleep(100000000);                 //10s.
                printFilterStream.getTwitterStream().cleanUp();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
