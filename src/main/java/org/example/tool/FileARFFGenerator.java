package org.example.tool;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

public class FileARFFGenerator {
    private final String projectName;

    public FileARFFGenerator(String projectName) {
        this.projectName = projectName;
    }

    public void csvToARFF() throws IOException {
        // Fetch .csv file
        String csvFile = "/Users/andreaandreoli/Desktop/ISW2Falessi/MetricsML/" + this.projectName + "_trainingSet.csv";

        // Where to save .arff file
        String arffFile = "/Users/andreaandreoli/Desktop/ISW2Falessi/MetricsML/" + this.projectName + "_trainingSet.arff";

        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvFile));
        Instances data = loader.getDataSet();

        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(arffFile));
        saver.writeBatch();
    }
}
