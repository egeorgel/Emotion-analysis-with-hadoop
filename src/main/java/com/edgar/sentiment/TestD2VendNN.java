package com.edgar.sentiment;

import edu.berkeley.compbio.jlibsvm.*;
import edu.berkeley.compbio.jlibsvm.binary.BinaryClassificationSVM;
import edu.berkeley.compbio.jlibsvm.binary.C_SVC;
import edu.berkeley.compbio.jlibsvm.kernel.KernelFunction;
import edu.berkeley.compbio.jlibsvm.kernel.LinearKernel;
import edu.berkeley.compbio.jlibsvm.labelinverter.StringLabelInverter;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassificationSVM;
import edu.berkeley.compbio.jlibsvm.multi.MutableMultiClassProblemImpl;
import edu.berkeley.compbio.jlibsvm.scaler.NoopScalingModel;
import edu.berkeley.compbio.jlibsvm.scaler.NoopScalingModelLearner;
import edu.berkeley.compbio.jlibsvm.scaler.ScalingModelLearner;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import edu.berkeley.compbio.ml.CrossValidationResults;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.*;
import java.util.*;

/**
 * Created by Edgar on 11/12/2016.
 */
public class TestD2VendNN {

    static ParagraphVectors doc2vec;
    //KernelFunction kernel;
    static SVM svm;
    private static MutableSvmProblem problem;

    public static void main(String[] args) throws Exception {

        int numberOfDoc = loadDoc2vect();
        trainNN();
    }

    public static void trainNN() throws IOException {
        final int rngSeed = 123;
        final int iteration = 1;
        final int outputNum = 5;
        final int nEpochs = 500;
        final int nOut = 50;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(iteration)
                .learningRate(0.006)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .regularization(true).l2(1e-4)
                .list()

                .layer(0, new DenseLayer.Builder()
                .nIn(doc2vec.getLayerSize())
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

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));  //Print score every 10 parameter updates

        INDArray indArrayTwit = doc2vec.getWordVectorMatrix("DOC_0");
        for (int i = 1; i < 12; ++i) {
            String doc_i = "DOC_" + i;
            INDArray doublesVect = doc2vec.getWordVectorMatrix(doc_i);
            indArrayTwit = Nd4j.vstack(indArrayTwit, doublesVect);
        }

        INDArray feelings = readFeelings();

        for ( int n = 0; n < nEpochs; n++) {
            model.fit(indArrayTwit, feelings );
        }

        System.out.println("Evaluate model....");
        Evaluation eval = new Evaluation(outputNum);

        INDArray result = model.output(indArrayTwit);
        eval.eval(feelings, result); //check the prediction against the true class

        //Print the evaluation statistics
        System.out.println(eval.stats());
    }


    public static INDArray readFeelings() throws IOException {
        HashMap<String, double[]> str2Int = new HashMap<>();
        double[] funny = {1, 0, 0, 0, 0};
        double[] happy = {0, 1, 0, 0, 0};
        double[] love =  {0, 0, 1, 0, 0};
        double[] holiday = {0, 0, 0, 1, 0};
        double[] sad =  {0, 0, 0, 0, 1};
        str2Int.put("funny", funny);
        str2Int.put("happy", happy);
        str2Int.put("love", love);
        str2Int.put("holiday", holiday);
        str2Int.put("sad", sad);

        File file = new File("/Users/Edgar/Desktop/emotion.txt");
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        INDArray feeling =  Nd4j.create(str2Int.get(dis.readLine()),new int[]{1,funny.length});
        for (int i = 1; i < 12; ++i) {
            feeling = Nd4j.vstack(feeling, Nd4j.create(str2Int.get(dis.readLine()),new int[]{1,funny.length}));
        }

        fis.close();
        bis.close();
        dis.close();

        return feeling;
    }

    public static SparseVector doubleArrayToSparsevec(int indexDoubleArray) {
        String doc_i = "DOC_" + indexDoubleArray;
        double[] doubleVect = doc2vec.getWordVector(doc_i);

        SparseVector sp = new SparseVector(doubleVect.length);
        for (int d_i = 0; d_i < doubleVect.length; ++d_i) {
            sp.values[d_i] = (float) doubleVect[d_i];
            sp.indexes[d_i] = d_i;
        }
        return sp;
    }

    public static void learnDoc2Vect() throws FileNotFoundException {

        File file = new File("/Users/Edgar/Desktop/twit.txt");
        SentenceIterator iter = new BasicLineIterator(file);

        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        List<String> stopWords = new ArrayList<>();
        stopWords.add("RT");

        LabelsSource source = new LabelsSource("DOC_");

        doc2vec = new ParagraphVectors.Builder()
                .minWordFrequency(1)
                .stopWords(stopWords)
                .iterations(5)
                .epochs(1)
                .layerSize(20)
                .learningRate(0.025)
                .labelsSource(source)
                .windowSize(5)
                .iterate(iter)
                .trainWordVectors(false)
                .tokenizerFactory(t)
                .sampling(0)
                .build();

        doc2vec.fit();
    }


    public static int loadDoc2vect() throws IOException {
//
        final String uri = "/Users/Edgar/Desktop/doc2vec.zip";
//        File file = new File(uri);
//
//        if ( file.exists() && !file.isDirectory() ) {
//            doc2vec = WordVectorSerializer.readParagraphVectors(uri);
//        }
//        else {
            learnDoc2Vect();
            WordVectorSerializer.writeParagraphVectors(doc2vec, uri);
 //       }

        final int numberOfDoc = 24;
        return numberOfDoc;
    }



    public static void svmCompute(int numberOfDoc) throws IOException {

        problem = new MutableMultiClassProblemImpl<String, SparseVector>(String.class, new StringLabelInverter(),
                numberOfDoc,
                new NoopScalingModel<SparseVector>());

        File file = new File("/Users/Edgar/Desktop/emotion.txt");
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        for (int i = 0; i < numberOfDoc; ++i) {
            problem.addExample(doubleArrayToSparsevec(i), dis.readLine() );
        }
        fis.close();
        bis.close();
        dis.close();

        ScalingModelLearner<Double[]> scalingModelLearner = new NoopScalingModelLearner<>();
        ImmutableSvmParameterGrid.Builder builder = ImmutableSvmParameterGrid.builder();

        builder.nu = 0.5f;
        builder.cache_size = 100;
        builder.eps = 1e-3f;
        builder.p = 0.1f;
        builder.shrinking = true;
        builder.probability = false;
        builder.redistributeUnbalancedC = true;

        builder.kernelSet = new HashSet<KernelFunction>();
        builder.kernelSet.add(new LinearKernel());
        builder.scalingModelLearner = scalingModelLearner;

        ImmutableSvmParameter param = builder.build();
        svm = new C_SVC();
        if (svm instanceof BinaryClassificationSVM && problem.getLabels().size() > 2) {
            svm = new MultiClassificationSVM((BinaryClassificationSVM) svm);
        }


        SolutionModel model = svm.train(problem, param);
        model.save("/Users/Edgar/Desktop/modelSvm.txt");

        CrossValidationResults cv = model.getCrossValidationResults();
        if (cv == null) {
            cv = svm.performCrossValidation(problem, param);
        }
        if (cv != null)
            System.out.println(cv.toString());
    }


}
