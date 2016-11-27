package com.edgar.sentiment;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created by Edgar on 09/10/2016.
 */
public class TwitModeratorReduce extends TableReducer<Text, IntWritable, ImmutableBytesWritable> {

    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

        for (IntWritable val : values) {

            if (val.get() == 1) {
                Put put = new Put(Bytes.toBytes(key.toString()));
                put.add(Bytes.toBytes("twit-text"), Bytes.toBytes("text"), Bytes.toBytes(key.toString()));

                context.write(null, put);
            }

        }


    }
}
