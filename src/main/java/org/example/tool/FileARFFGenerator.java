package org.example.tool;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

public class FileARFFGenerator {
    private final String projectName;
    private final int index;

    public FileARFFGenerator(String projectName, int index) {
        this.projectName = projectName;
        this.index = index;
    }

    public void csvToARFFTraining() throws IOException {
        // Fetch .csv file
        String csvFile = "src/main/resources/" + this.projectName.toLowerCase() + "/training/five_run/CSV/" + this.projectName + "_trainingSet" + index + ".csv";

        // Where to save .arff file
        String arffFile = "src/main/resources/" + this.projectName.toLowerCase() + "/training/five_run/ARFF/" + this.projectName + "_trainingSet" + index + ".arff";

        csvToARFF(csvFile, arffFile);
    }

    public void csvToARFFTesting() throws IOException {
        // Fetch .csv file
        String csvFile = "src/main/resources/" + this.projectName.toLowerCase() + "/testing/five_run/CSV/" + this.projectName + "_testingSet" + index + ".csv";

        // Where to save .arff file
        String arffFile = "src/main/resources/" + this.projectName.toLowerCase() + "/testing/five_run/ARFF/" + this.projectName + "_testingSet" + index + ".arff";

        csvToARFF(csvFile, arffFile);
    }

    private void csvToARFF(String csvFile, String arffFile) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvFile));
        Instances data = loader.getDataSet();

        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(arffFile));
        saver.writeBatch();
    }
}
