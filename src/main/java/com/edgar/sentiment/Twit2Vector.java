package com.edgar.sentiment;

import javafx.util.Pair;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Edgar on 10/12/2016.
 */
public class Twit2Vector {

    ParagraphVectors paragraphVectors;
    HbaseLabelAwareIterator iterator;
    TokenizerFactory tokenizerFactory;

    MultiLayerNetwork model;
    Evaluation eval;
    INDArray feelings;

    public Twit2Vector() throws Exception {

        twitInHBase2Vectors();
        trainNN();
        predict();
    }

    void twitInHBase2Vectors()  throws Exception {

        // build a iterator for our dataset
        iterator = new HbaseLabelAwareIterator("Twit-emoji-eg","Twit-in-eg");

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        LabelsSource source = new LabelsSource("DOC_");

        // ParagraphVectors training configuration
        paragraphVectors = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minLearningRate(0.001)
                .batchSize(300)
                .epochs(20)
                .labelsSource(source)
                .iterate(iterator)
                .trainWordVectors(true)
                .tokenizerFactory(tokenizerFactory)
                .build();

        // Start model training
        paragraphVectors.fit();

        //paragraphVectors.lookupTable()
        //LabelAwareSentenceIterator
    }

    public void trainNN() throws IOException {
        final int rngSeed = 123; // This random-number generator applies a seed to ensure that the same initial weights are used when training. We’ll explain why this matters later.
        final int iteration = 1;
        final int outputNum = 7;
        final int nEpochs = 50;
        final int nOut = 1000;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(iteration)
                .learningRate(0.006)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .regularization(true).l2(1e-4)
                .list()

                .layer(0, new DenseLayer.Builder()
                        .nIn(paragraphVectors.getLayerSize())
                        .nOut(nOut) // Number of output datapoints.
                        .activation("relu") // Activation function.
                        .weightInit(WeightInit.XAVIER) // Weight initialization.
                        .build())

                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(nOut)
                        .nOut(outputNum)
                        .activation("softmax")
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .pretrain(false).backprop(true)
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));  //Print score every 10 parameter updates

        INDArray indArrayTwit = paragraphVectors.getWordVectorMatrix("DOC_0");
        for (int i = 1; i < iterator.getSentiments().size(); ++i) {
            String doc_i = "DOC_" + i;
            INDArray doublesVect = paragraphVectors.getWordVectorMatrix(doc_i);
            indArrayTwit = Nd4j.vstack(indArrayTwit, doublesVect);
        }

        feelings = readFeelings();

        for ( int n = 0; n < nEpochs; n++) {
            model.fit(indArrayTwit, feelings );
        }

        System.out.println("Evaluate model....");
        eval = new Evaluation(outputNum);

        INDArray result = model.output(indArrayTwit);
        eval.eval(feelings, result); //check the prediction against the true class

        //Print the evaluation statistics
        System.out.println(eval.stats());
    }


    public INDArray readFeelings() {
        HashMap<String, double[]> str2Int = new HashMap<>();
        double[] funny = {1, 0, 0, 0, 0, 0, 0};
        double[] happy = {0, 1, 0, 0, 0, 0, 0};
        double[] love =  {0, 0, 1, 0, 0, 0, 0};
        double[] sad =   {0, 0, 0, 1, 0, 0, 0};
        double[] angry = {0, 0, 0, 0, 1, 0, 0};
        double[] sport =   {0, 0, 0, 0, 0, 1, 0};
        double[] holiday = {0, 0, 0, 0, 0, 0, 1};
        str2Int.put("funny", funny);
        str2Int.put("happy", happy);
        str2Int.put("love", love);
        str2Int.put("sad", sad);
        str2Int.put("angry", angry);
        str2Int.put("sport", sport);
        str2Int.put("holiday", holiday);

        INDArray feeling =  Nd4j.create(str2Int.get(iterator.getSentiments().get(0)),new int[]{1,funny.length});
        for (int i = 1; i < iterator.getSentiments().size(); ++i) {
            feeling = Nd4j.vstack(feeling, Nd4j.create(str2Int.get(iterator.getSentiments().get(i)),new int[]{1,funny.length}));
        }

        return feeling;
    }

    public void predict() {
        INDArray toPredict =  paragraphVectors.getWordVectorMatrix("DOC_"+ iterator.getSentiments().size());
        for (int i = iterator.getSentiments().size(); i < iterator.totalCont; ++i) {
            String doc_i = "DOC_" + i;
            INDArray doublesVect = paragraphVectors.getWordVectorMatrix(doc_i);
            toPredict = Nd4j.vstack(toPredict, doublesVect);
        }

        System.out.println("Evaluate model predict....");
        INDArray result = model.output(toPredict);

        //paragraphVectors.getVocab().
    }



}
