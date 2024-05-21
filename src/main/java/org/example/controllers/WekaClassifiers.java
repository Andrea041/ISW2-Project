package org.example.controllers;

import org.example.entities.ClassifierResults;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;

public class WekaClassifiers {
    private final String projName;
    private final int walkPass;

    private static final String RESOURCES = "src/main/resources/";

    private static final String NAIVE_BAYES = "naive bayes";
    private static final String RANDOM_FOREST = "random forest";
    private static final String IBK = "IBK";

    private static final String[] CLASSIFIER_NAME = {NAIVE_BAYES, RANDOM_FOREST, IBK};

    public WekaClassifiers(String projName, int walkPass) {
        this.projName = projName;
        this.walkPass = walkPass;
    }

    public List<ClassifierResults> fetchWekaAnalysis() throws Exception {
        List<ClassifierResults> classifierResults = new ArrayList<>();

        /* Walk forward analysis */
        for (int i = 2; i <= walkPass; i++) {
            String arffTrainingPath = RESOURCES + projName.toLowerCase() + "/training/ARFF/" + this.projName + "_trainingSet" + i + ".arff";
            DataSource trainingSource = new DataSource(arffTrainingPath);
            Instances trainDataset = trainingSource.getDataSet();

            String arffTestingPath = RESOURCES + projName.toLowerCase() + "/testing/ARFF/" + this.projName + "_testingSet" + i + ".arff";
            DataSource testingSource = new DataSource(arffTestingPath);
            Instances testDataset = testingSource.getDataSet();

            /* Setting class to predict to last column (buggyness) */
            trainDataset.setClassIndex(trainDataset.numAttributes() - 1);
            testDataset.setClassIndex(testDataset.numAttributes() - 1);

            /* Create List of classifiers to use */
            List<Classifier> classifiers = new ArrayList<>(List.of(new NaiveBayes(),
                                                                new RandomForest(),
                                                                new IBk()));

            Evaluation eval = new Evaluation(testDataset);

            /* Default classifier: no cost sensitive, no sampling, no selection */
            int index = 0;
            for (Classifier classifier : classifiers) {
                classifier.buildClassifier(trainDataset);
                eval.evaluateModel(classifier, testDataset);

                ClassifierResults res = new ClassifierResults(this.projName, i, CLASSIFIER_NAME[index], false, false, false, trainDataset.numInstances(), testDataset.numInstances());

                res.setRec(eval.recall(0));
                res.setPreci(eval.precision(0));
                res.setKappa(eval.kappa());
                res.setTrueNegatives(eval.numTrueNegatives(0));
                res.setFalsePositives(eval.numFalsePositives(0));
                res.setFalseNegatives(eval.numFalseNegatives(0));
                res.setTruePositives(eval.numTruePositives(0));
                res.setAUC(eval.areaUnderROC(0));
                res.setFMeasure(eval.fMeasure(0));

                classifierResults.add(res);
                index++;
            }
        }

        return classifierResults;
    }
}
