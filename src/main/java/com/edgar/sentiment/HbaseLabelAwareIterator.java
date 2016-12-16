package com.edgar.sentiment;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Edgar on 10/12/2016.
 */
public class HbaseLabelAwareIterator implements SentenceIterator, Iterable<String> {

    private List<String> tableNames;
    private List<HTable> hbTables;
    private int i_tableNames;


    protected HTable current_htable;
    protected ResultScanner current_scanner;
    private Result result_0, result_1;

    private ArrayList<String> d_sentiments;
    public ArrayList<String> getSentiments(){
        return d_sentiments;
    }
    public int totalCont;

    public HbaseLabelAwareIterator(String tableNameTrain, String tableNamePredict) throws IOException {

        tableNames = new ArrayList<>();
        tableNames.add(tableNameTrain);
        tableNames.add(tableNamePredict);
        hbTables = new ArrayList<>();

        totalCont = 0;
        i_tableNames = 0;

        createScanner();
        nextBaseIfResultNull();
        nextRow();
    }

    public void finalize() {
        finish();
    }

    private void createScanner() throws IOException  {
        Scan scan = new Scan();
        scan.setFilter(new FirstKeyOnlyFilter());
        Configuration conf = HBaseConfiguration.create();

        current_htable = new HTable(conf, tableNames.get(i_tableNames));
        hbTables.add(current_htable);
        current_scanner = current_htable.getScanner(scan);
        result_1 = current_scanner.next();
    }

    private void nextBaseIfResultNull() throws IOException  {
        if (result_1 == null) {
            if (i_tableNames < tableNames.size()) {
                ++i_tableNames;
                createScanner();
            }
        }
    }

    private void nextRow() throws IOException {
        ++totalCont;
        result_0 = result_1;
        result_1 = current_scanner.next();
        nextBaseIfResultNull();
    }

    public boolean hasNextDocument() {
        return result_1 != null;
    }

    @Override
    public void reset() {
        try {
            for (int i = 0; i < i_tableNames; ++i) {
                    hbTables.get(i).close();
            }

            i_tableNames = 0;
            d_sentiments.clear();

            createScanner();
            nextBaseIfResultNull();
            nextRow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        for (int i = 0; i < i_tableNames; ++i) {
            try {
                hbTables.get(i).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SentencePreProcessor getPreProcessor() {
        return null;
    }

    @Override
    public void setPreProcessor(SentencePreProcessor sentencePreProcessor) {

    }

    @Override
    public String nextSentence() {
        String twitText = "";
        try {
            if (result_0 == null) {
                throw new RuntimeException();
            }

            byte[] key = result_0.getRow();
            // Instantiating Get class
            Get g = new Get(key);
            // Reading the data
            Result result = current_htable.get(g);
            // Reading values from Result class object
            byte [] value = result.getValue(Bytes.toBytes("twit-text"),Bytes.toBytes("text"));
            byte [] value1 = result.getValue(Bytes.toBytes("twit-feeling"),Bytes.toBytes("text"));
            twitText = Bytes.toString(value);
            String twitEmo = Bytes.toString(value1);

            if (i_tableNames == 0)
                d_sentiments.add(twitEmo);

            nextRow();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return twitText;
    }

    @Override
    public boolean hasNext() {
        return hasNextDocument();
    }


    @Override
    public Iterator<String> iterator() {
        this.reset();
        Iterator ret = new Iterator() {
            public boolean hasNext() {
                return HbaseLabelAwareIterator.this.hasNext();
            }

            public String next() {
                return HbaseLabelAwareIterator.this.nextSentence();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return ret;
    }
}
