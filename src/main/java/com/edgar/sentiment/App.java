package com.edgar.sentiment;

import java.io.IOException;

public class App {


    public static void main( String[] args ) throws IOException {

        //train();
        //listenAll();
        try {
            Twit2Vector twit2Vector = new Twit2Vector();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Configuration config = HBaseConfiguration.create();
//        Job job = new Job(config,"ExampleSummary");
//        job.setJarByClass(App.class);     // class that contains mapper and reducer
//
//        Scan scan = new Scan();
//        scan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
//        scan.setCacheBlocks(false);  // don't set to true for MR jobs
//// set other scan attrs
//
//        TableMapReduceUtil.initTableMapperJob(
//                "Twit-in-eg",        // input table
//                scan,               // Scan instance to control CF and attribute selection
//                TwitModeratorMapper.class,     // mapper class
//                Text.class,         // mapper output key
//                IntWritable.class,  // mapper output value
//                job);
//        TableMapReduceUtil.initTableReducerJob(
//                "Twit-abusive-eg",        // output table
//                TwitModeratorReduce.class,    // reducer class
//                job);
//        job.setNumReduceTasks(1);   // at least one, adjust as required
//
//        boolean b = false;
//        try {
//            b = job.waitForCompletion(true);
//        } catch (InterruptedException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        if (!b) {
//            throw new IOException("error with job!");
//        }

    }


    /**
     * Save twit in Twit-emoji-eg to be train
     * @throws IOException
     */
    public static void train() throws IOException {
        try {
            TwitterStream printFilterStream = new TwitterStream();
            printFilterStream.train();
            try {
                Thread.sleep(60 *   // minutes to sleep
                                    60 *   // seconds to a minute
                                    1000); // milliseconds to a second
                printFilterStream.getTwitterStream().cleanUp();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save twit in Twit-emoji-eg to be train
     * @throws IOException
     */
    public static void listenAll() throws IOException {
        try {
            TwitterStream printFilterStream = new TwitterStream();
            printFilterStream.listenAll();
            try {
                Thread.sleep(10 *   // minutes to sleep
                                    60 *   // seconds to a minute
                                    1000); // milliseconds to a second
                printFilterStream.getTwitterStream().cleanUp();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
