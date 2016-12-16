package com.edgar.sentiment;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Edgar on 03/12/2016.
 */
public class SaveTwitWithEmoji {

    private HTable table;

    public SaveTwitWithEmoji() throws IOException {

        Configuration conf = HBaseConfiguration.create();
        HBaseAdmin admin = new HBaseAdmin(conf);
        if (!admin.tableExists("Twit-emoji-eg")) {
            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf("Twit-emoji-eg"));
            tableDescriptor.addFamily(new HColumnDescriptor("twit-text"));
            tableDescriptor.addFamily(new HColumnDescriptor("twit-feeling"));
            admin.createTable(tableDescriptor);
        }

        // Instantiating HTable class
        table = new HTable(conf, "Twit-emoji-eg");
    }

    public void add(String idName, String emoji, String text) throws IOException {

        Date now = new Date();
        Put put = new Put(Bytes.toBytes(idName + now.toString()));
        put.add(Bytes.toBytes("twit-text"), Bytes.toBytes("text"), Bytes.toBytes(text));
        put.add(Bytes.toBytes("twit-feeling"), Bytes.toBytes("text"), Bytes.toBytes(emoji));
        table.put(put);

        table.close();
    }

}
