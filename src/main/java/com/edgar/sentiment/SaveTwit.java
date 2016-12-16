package com.edgar.sentiment;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by Edgar on 26/11/2016.
 */
public class SaveTwit {

    public SaveTwit() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        HBaseAdmin admin = new HBaseAdmin(conf);
        if (!admin.tableExists("Twit-in-eg")) {
            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf("Twit-in-eg"));
            tableDescriptor.addFamily(new HColumnDescriptor("twit-text"));
            tableDescriptor.addFamily(new HColumnDescriptor("twit-feeling"));
            admin.createTable(tableDescriptor);
        }
    }

    public void add(String idName, String text, String feeling) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        HTable table = new HTable(conf, "Twit-in-eg");

        Date now = new Date();
        Put put = new Put(Bytes.toBytes(idName + now.toString()));
        put.add(Bytes.toBytes("twit-text"), Bytes.toBytes("text"), Bytes.toBytes(text));
        put.add(Bytes.toBytes("twit-feeling"), Bytes.toBytes("text"), Bytes.toBytes(feeling));
        table.put(put);

        table.close();
    }
}
