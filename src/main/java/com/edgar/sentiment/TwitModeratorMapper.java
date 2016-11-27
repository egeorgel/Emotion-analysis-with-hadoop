package com.edgar.sentiment;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * Created by Edgar on 08/10/2016.
 */
public class TwitModeratorMapper extends TableMapper<Text, IntWritable> {

    private final IntWritable ONE = new IntWritable(1);
    private final IntWritable O = new IntWritable(0);

    private Text text = new Text();

    public void map(ImmutableBytesWritable row, Result value, Context context) throws IOException, InterruptedException {
        String val = new String(value.getValue(Bytes.toBytes("twit-text"), Bytes.toBytes("text")));
        text.set(val);     // we can only emit Writables...
        if (val.equals("RT @quoteIibrary: Virginia Woolf, A Passionate Apprentice https://t.co/R9vEEZaovs")) {
            context.write(text, ONE);
        }

        context.write(text, O);
    }
}

