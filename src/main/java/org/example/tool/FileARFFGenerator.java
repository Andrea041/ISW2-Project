package org.example.tool;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;

public class FileARFFGenerator {
    private final String projectName;
    private final int index;
    private static final String PATH = "src/main/resources/";

    public FileARFFGenerator(String projectName, int index) {
        this.projectName = projectName;
        this.index = index;
    }

    public void csvToARFFTraining() throws Exception {
        // Fetch .csv file
        String csvFile = PATH + this.projectName.toLowerCase() + "/training/CSV/" + this.projectName + "_trainingSet" + index + ".csv";

        // Where to save .arff file
        String arffFile = PATH + this.projectName.toLowerCase() + "/training/ARFF/" + this.projectName + "_trainingSet" + index + ".arff";

        csvToARFF(csvFile, arffFile);
    }

    public void csvToARFFTesting() throws Exception {
        // Fetch .csv file
        String csvFile = PATH + this.projectName.toLowerCase() + "/testing/CSV/" + this.projectName + "_testingSet" + index + ".csv";

        // Where to save .arff file
        String arffFile = PATH + this.projectName.toLowerCase() + "/testing/ARFF/" + this.projectName + "_testingSet" + index + ".arff";

        csvToARFF(csvFile, arffFile);
    }

    private void csvToARFF(String csvFile, String arffFile) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvFile));
        Instances data = loader.getDataSet();

        int[] indicesToRemove = {0, 1};
        Remove remove = new Remove();
        remove.setAttributeIndicesArray(indicesToRemove);
        remove.setInputFormat(data);
        Instances newData = Filter.useFilter(data, remove);

        ArffSaver saver = new ArffSaver();
        saver.setInstances(newData);
        saver.setFile(new File(arffFile));
        saver.writeBatch();
    }
}
