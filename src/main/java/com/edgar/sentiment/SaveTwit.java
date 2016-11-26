package main.java.com.edgar.sentiment;

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
            admin.createTable(tableDescriptor);
        }
    }

    public void add(String idName, String text) throws IOException {
        // define people
        Configuration conf = HBaseConfiguration.create();
        HTable table = new HTable(conf, "peopleTable-EG");

        Put put = new Put(Bytes.toBytes(idName));
        put.add(Bytes.toBytes("twit-text"), Bytes.toBytes("text"), Bytes.toBytes(text));
        table.put(put);

        System.out.println("data inserted");

        table.close();
    }
}
