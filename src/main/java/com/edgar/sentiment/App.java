package com.edgar.sentiment;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {


    public static void main( String[] args ) throws IOException {
//        try {
//            PrintFilterStream printFilterStream = new PrintFilterStream();
//            try {
//                Thread.sleep(10000);                 //10s.
//                printFilterStream.getTwitterStream().cleanUp();
//            } catch(InterruptedException ex) {
//                Thread.currentThread().interrupt();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Configuration config = HBaseConfiguration.create();
        Job job = new Job(config,"ExampleSummary");
        job.setJarByClass(App.class);     // class that contains mapper and reducer

        Scan scan = new Scan();
        scan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
        scan.setCacheBlocks(false);  // don't set to true for MR jobs
// set other scan attrs

        TableMapReduceUtil.initTableMapperJob(
                "Twit-in-eg",        // input table
                scan,               // Scan instance to control CF and attribute selection
                TwitModeratorMapper.class,     // mapper class
                Text.class,         // mapper output key
                IntWritable.class,  // mapper output value
                job);
        TableMapReduceUtil.initTableReducerJob(
                "Twit-abusive-eg",        // output table
                TwitModeratorReduce.class,    // reducer class
                job);
        job.setNumReduceTasks(1);   // at least one, adjust as required

        boolean b = false;
        try {
            b = job.waitForCompletion(true);
        } catch (InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!b) {
            throw new IOException("error with job!");
        }

    }
}
