package org.example.controllers;

import org.example.entities.ClassifierResults;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

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

            /* Default classifier: no cost sensitive, no sampling, no selection */
            evaluateClassifier(classifierResults, i, trainDataset, testDataset, classifiers, "", "", "");

            /* classifier: no cost sensitive, no sampling, feature selection */
            CfsSubsetEval subsetEval = new CfsSubsetEval();
            AttributeSelection filter = new AttributeSelection();

            BestFirst bestFirst = new BestFirst();
            filter.setEvaluator(subsetEval);
            filter.setSearch(bestFirst);
            filter.setInputFormat(trainDataset);

            Instances newTrain = Filter.useFilter(trainDataset, filter);
            Instances newTest = Filter.useFilter(testDataset, filter);

            int numAttrFiltered = newTrain.numAttributes();
            newTrain.setClassIndex(numAttrFiltered - 1);

            evaluateClassifier(classifierResults, i, newTrain, newTest, classifiers, "", "", "best first");

            GreedyStepwise greedyStepwise = new GreedyStepwise();
            greedyStepwise.setSearchBackwards(true);

            filter.setSearch(greedyStepwise);

            newTrain = Filter.useFilter(trainDataset, filter);
            newTest = Filter.useFilter(testDataset, filter);

            evaluateClassifier(classifierResults, i, newTrain, newTest, classifiers, "", "", "greedy");
        }

        return classifierResults;
    }

    private void evaluateClassifier(List<ClassifierResults> classifierResults, int i, Instances trainDataset, Instances testDataset, List<Classifier> classifiers, String costSensitive, String samplingType, String selection) throws Exception {
        Evaluation evaluation = new Evaluation(testDataset);
        int index = 0;

        for (Classifier classifier : classifiers) {
            classifier.buildClassifier(trainDataset);
            evaluation.evaluateModel(classifier, testDataset);

            ClassifierResults res = new ClassifierResults(this.projName, i, CLASSIFIER_NAME[index], costSensitive, samplingType, selection, trainDataset.numInstances(), testDataset.numInstances());

            // classIndex has value 0 or 1 {no, yes} so I choose index 1 to make prediction on 'yes' class
            res.setRec(evaluation.recall(1));
            res.setPreci(evaluation.precision(1));
            res.setKappa(evaluation.kappa());
            res.setTrueNegatives(evaluation.numTrueNegatives(1));
            res.setFalsePositives(evaluation.numFalsePositives(1));
            res.setFalseNegatives(evaluation.numFalseNegatives(1));
            res.setTruePositives(evaluation.numTruePositives(1));
            res.setAUC(evaluation.areaUnderROC(1));
            res.setFMeasure(evaluation.fMeasure(1));

            classifierResults.add(res);
            index++;
        }
    }
}
