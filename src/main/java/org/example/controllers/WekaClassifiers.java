package org.example.controllers;

import org.example.entities.ClassifierResults;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.supervised.instance.SMOTE;

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
            evaluateClassifier(classifierResults, i, trainDataset, testDataset, classifiers, "", "", "", null, null);

            /* classifier: no cost sensitive, no sampling, feature selection (best first) */
            CfsSubsetEval subsetEval = new CfsSubsetEval();
            AttributeSelection featureSelected = new AttributeSelection();

            // Best First selection
            BestFirst bestFirst = new BestFirst();
            featureSelected.setEvaluator(subsetEval);
            featureSelected.setSearch(bestFirst);
            featureSelected.setInputFormat(trainDataset);

            Instances newTrainBest = Filter.useFilter(trainDataset, featureSelected);
            Instances newTestBest = Filter.useFilter(testDataset, featureSelected);

            newTrainBest.setClassIndex(newTrainBest.numAttributes() - 1);
            newTestBest.setClassIndex(newTestBest.numAttributes() - 1);

            evaluateClassifier(classifierResults, i, newTrainBest, newTestBest, classifiers, "", "", "best first", null, null);

            /* classifier: no cost sensitive, sampling (SMOTE), no feature selection */
            FilteredClassifier fc = new FilteredClassifier();

            SMOTE smote = new SMOTE();
            smote.setInputFormat(trainDataset);
            fc.setFilter(smote);

            Instances newTrainSmote = Filter.useFilter(trainDataset, smote);
            newTrainSmote.setClassIndex(newTrainSmote.numAttributes() - 1);

            evaluateClassifier(classifierResults, i, newTrainSmote, testDataset, classifiers, "", "SMOTE", "" , fc, null);

            /* classifier: cost sensitive, no sampling, no feature selection */
            CostSensitiveClassifier cc = new CostSensitiveClassifier();
            evaluateClassifier(classifierResults, i, trainDataset, testDataset, classifiers, "cost sensitive", "", "", null, cc);

            /* classifier: cost sensitive, no sampling, feature selection */
            evaluateClassifier(classifierResults, i, newTrainBest, newTestBest, classifiers, "cost sensitive", "", "best first", null, cc);

            /* classifier: no cost sensitive, sampling, feature selection */
            // Best First selection
            featureSelected.setInputFormat(newTrainSmote);

            // Apply feature selection to training set with smote
            Instances newTrainSmoteBest = Filter.useFilter(newTrainSmote, featureSelected);
            newTrainSmoteBest.setClassIndex(newTrainSmoteBest.numAttributes() - 1);

            Instances newTestSmoteBest = Filter.useFilter(testDataset, featureSelected);
            newTestSmoteBest.setClassIndex(newTestSmoteBest.numAttributes() - 1);

            evaluateClassifier(classifierResults, i, newTrainSmoteBest, newTestSmoteBest, classifiers, "", "SMOTE", "best first" , null, null);
        }

        return classifierResults;
    }

    private void evaluateClassifier(List<ClassifierResults> classifierResults, int i, Instances trainDataset, Instances testDataset, List<Classifier> classifiers, String costSensitive, String samplingType, String selection, FilteredClassifier fc, CostSensitiveClassifier cc) throws Exception {
        Evaluation evaluation = new Evaluation(testDataset);
        int index = 0;

        for (Classifier classifier : classifiers) {
            if (!samplingType.isEmpty() && costSensitive.isEmpty() && fc != null) {
                fc.setClassifier(classifier);
                fc.buildClassifier(trainDataset);
                evaluation.evaluateModel(fc, testDataset);
            } else if (!costSensitive.isEmpty()) {
                setCostSensitive(cc, classifier, trainDataset);
                evaluation.evaluateModel(cc, testDataset);
            } else {
                classifier.buildClassifier(trainDataset);
                evaluation.evaluateModel(classifier, testDataset);
            }

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

    private void setCostSensitive(CostSensitiveClassifier cc, Classifier classifier, Instances trainDataset) throws Exception {
        double cfp = 10.0;
        double cfn = 1.0;

        cc.setClassifier(classifier);
        cc.setCostMatrix(createCostMatrix(cfp, cfn));
        cc.buildClassifier(trainDataset);
    }

    private CostMatrix createCostMatrix(double weightFalsePositive, double weightFalseNegative) {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, weightFalsePositive);
        costMatrix.setCell(0, 1, weightFalseNegative);
        costMatrix.setCell(1, 1, 0.0);

        return costMatrix;
    }
}