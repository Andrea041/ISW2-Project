package org.example.controllers;

import org.example.entities.ClassifierResults;
import org.example.entities.ClassifierSettings;
import org.example.entities.JavaClass;
import org.example.entities.Release;
import org.example.enumeration.ClassifierProperty;
import org.example.tool.FileCSVGenerator;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
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
import weka.filters.supervised.instance.SMOTE;

import java.util.ArrayList;
import java.util.List;

public class WekaClassifiers {
    private final String projName;
    private final int walkPass;
    private final List<Release> acumeRelease;

    /* Create List of classifiers to use */
    private final List<Classifier> classifiers = new ArrayList<>(List.of(new NaiveBayes(), new RandomForest(), new IBk()));

    private static final String RESOURCES = "src/main/resources/";

    private static final String NAIVE_BAYES = "naive bayes";
    private static final String RANDOM_FOREST = "random forest";
    private static final String IBK = "IBK";

    private static final String[] CLASSIFIER_NAME = {NAIVE_BAYES, RANDOM_FOREST, IBK};

    public WekaClassifiers(String projName, int walkPass, List<Release> acumeRelease) {
        this.projName = projName;
        this.walkPass = walkPass;
        this.acumeRelease = acumeRelease;
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

            ClassifierSettings settings = new ClassifierSettings();

            /* Default classifier: no cost sensitive, no sampling, no selection */
            evaluateClassifier(classifierResults, i, trainDataset, testDataset, settings, null, null);

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

            settings.setFeatureSelection(ClassifierProperty.SELECTION.getValue());

            evaluateClassifier(classifierResults, i, newTrainBest, newTestBest, settings, null, null);

            /* classifier: no cost sensitive, sampling (SMOTE), no feature selection */
            FilteredClassifier fc = new FilteredClassifier();

            SMOTE smote = new SMOTE();
            smote.setInputFormat(trainDataset);
            fc.setFilter(smote);

            settings.reset();
            settings.setSampling(ClassifierProperty.SAMPLING_TYPE.getValue());

            evaluateClassifier(classifierResults, i, trainDataset, testDataset, settings, fc, null);

            /* classifier: cost sensitive, no sampling, no feature selection */
            settings.reset();
            settings.setCostSensitive(ClassifierProperty.COST_SENSITIVE.getValue());

            CostSensitiveClassifier cc = new CostSensitiveClassifier();
            evaluateClassifier(classifierResults, i, trainDataset, testDataset, settings, null, cc);

            /* classifier: cost sensitive, no sampling, feature selection */
            settings.setFeatureSelection(ClassifierProperty.SELECTION.getValue());
            evaluateClassifier(classifierResults, i, newTrainBest, newTestBest, settings, null, cc);

            /* classifier: no cost sensitive, sampling, feature selection */
            // Best First selection
            settings.reset();
            settings.setSampling(ClassifierProperty.SAMPLING_TYPE.getValue());
            settings.setFeatureSelection(ClassifierProperty.SELECTION.getValue());

            featureSelected.setInputFormat(trainDataset);

            // Apply feature selection to training set with smote
            Instances newTrainSmoteBest = Filter.useFilter(trainDataset, featureSelected);
            newTrainSmoteBest.setClassIndex(newTrainSmoteBest.numAttributes() - 1);

            Instances newTestSmoteBest = Filter.useFilter(testDataset, featureSelected);
            newTestSmoteBest.setClassIndex(newTestSmoteBest.numAttributes() - 1);

            evaluateClassifier(classifierResults, i, newTrainSmoteBest, newTestSmoteBest, settings , fc, null);
        }

        return classifierResults;
    }

    private void evaluateClassifier(List<ClassifierResults> classifierResults, int i, Instances trainDataset, Instances testDataset, ClassifierSettings settings, FilteredClassifier fc, CostSensitiveClassifier cc) throws Exception {
        Evaluation evaluation = new Evaluation(testDataset);
        int index = 0;
        String combinationClassifier = settings.getCostSensitive() + settings.getFeatureSelection() + settings.getSampling();

        for (Classifier classifier : classifiers) {
            if (!settings.getSampling().isEmpty() && settings.getCostSensitive().isEmpty() && fc != null) {
                fc.setClassifier(classifier);
                fc.buildClassifier(trainDataset);
                evaluation.evaluateModel(fc, testDataset);

                makePrediction(fc, testDataset, i, index, combinationClassifier);
            } else if (!settings.getCostSensitive().isEmpty()) {
                setCostSensitive(cc, classifier, trainDataset);
                evaluation.evaluateModel(cc, testDataset);

                makePrediction(cc, testDataset, i, index, combinationClassifier);
            } else {
                classifier.buildClassifier(trainDataset);
                evaluation.evaluateModel(classifier, testDataset);

                makePrediction(classifier, testDataset, i, index, combinationClassifier);
            }

            ClassifierResults res = new ClassifierResults(this.projName, i, CLASSIFIER_NAME[index], settings, trainDataset.numInstances(), testDataset.numInstances());

            // classIndex has value 0 or 1 {no, yes} so I choose index 1 to make prediction on 'yes' class
            res.setRec(evaluation.recall(1));
            res.setPreci(evaluation.precision(1));
            res.setKappa(evaluation.kappa());
            res.setTrueNegatives(evaluation.numTrueNegatives(1));
            res.setFalsePositives(evaluation.numFalsePositives(1));
            res.setFalseNegatives(evaluation.numFalseNegatives(1));
            res.setTruePositives(evaluation.numTruePositives(1));
            res.setAuc(evaluation.areaUnderROC(1));
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

    private void makePrediction(Classifier classifier, Instances testDataset, int indexTestSet, int indexClassifier, String combination) throws Exception {
        int numTesting = testDataset.numInstances();
        List<JavaClass> acumeClasses = new ArrayList<>(acumeRelease.get(indexTestSet - 2).getJavaClassList());

        for (int i = 0; i < numTesting; i++) {
            // Get the prediction probability distribution
            double[] predictionDistribution = classifier.distributionForInstance(testDataset.instance(i));

            // Take index 1 that is prediction label "YES"
            double prediction = predictionDistribution[1];
            acumeClasses.get(i).setPrediction(prediction);
        }

        FileCSVGenerator csv = new FileCSVGenerator(RESOURCES, projName);
        csv.generateAcumeFile(acumeClasses, CLASSIFIER_NAME[indexClassifier], combination, indexTestSet);
    }
}
