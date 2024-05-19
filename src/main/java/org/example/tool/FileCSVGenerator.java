package org.example.tool;

import org.example.entities.JavaClass;
import org.example.entities.Release;
import org.example.entities.Ticket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileCSVGenerator {
    private final String directoryPath;
    private final String projName;
    private static final String TESTING = "testing" + File.separator;
    private static final String TRAINING = "training" + File.separator;
    private static final String OTHERFILES = "otherFiles" + File.separator;
    private static final String FIVE_RUN_TRAINING = TRAINING + "five_Run" + File.separator;
    private static final String THREE_RUN_TRAINING = TRAINING + "three_Run" + File.separator;
    private static final String FIVE_RUN_TESTING = TESTING + "five_Run" + File.separator;
    private static final String THREE_RUN_TESTING = TESTING + "three_Run" + File.separator;
    private static final String TRAINING_CSV_FIVE = FIVE_RUN_TRAINING + "CSV" + File.separator;
    private static final String TRAINING_ARFF_FIVE = FIVE_RUN_TRAINING + "ARFF" + File.separator;
    private static final String TRAINING_CSV_THREE = THREE_RUN_TRAINING + "CSV" + File.separator;
    private static final String TRAINING_ARFF_THREE = THREE_RUN_TRAINING + "ARFF" + File.separator;
    private static final String TESTING_CSV_FIVE = FIVE_RUN_TESTING + "CSV" + File.separator;
    private static final String TESTING_ARFF_FIVE = FIVE_RUN_TESTING + "ARFF" + File.separator;
    private static final String TESTING_CSV_THREE = THREE_RUN_TESTING + "CSV" + File.separator;
    private static final String TESTING_ARFF_THREE = THREE_RUN_TESTING + "ARFF" + File.separator;

    public FileCSVGenerator(String directoryPath, String projName) throws IOException {
        this.projName = projName;
        this.directoryPath = directoryPath + projName.toLowerCase() + File.separator;

        String[] subPaths = {
                "",
                TRAINING,
                TESTING,
                OTHERFILES,
                FIVE_RUN_TRAINING,
                THREE_RUN_TRAINING,
                FIVE_RUN_TESTING,
                THREE_RUN_TESTING,
                TRAINING_CSV_FIVE,
                TRAINING_ARFF_FIVE,
                TRAINING_CSV_THREE,
                TRAINING_ARFF_THREE,
                TESTING_CSV_FIVE,
                TESTING_ARFF_FIVE,
                TESTING_CSV_THREE,
                TESTING_ARFF_THREE
        };

        createDirectories(subPaths);
    }

    private void createDirectories(String[] subPaths) throws IOException {
        for (String subPath : subPaths) {
            Path path = Paths.get(this.directoryPath + subPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
    }

    private void writeToFile(FileWriter fileWriter, String content) throws IOException {
        fileWriter.append(content);
        fileWriter.append("\n");
    }

    private void closeWriter(FileWriter fileWriter) {
        if (fileWriter != null) {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
            }
        }
    }

    public void generateReleaseInfo(List<Release> releases) {
        FileWriter fileWriter = null;

        try {
            String fileTitle = this.directoryPath + OTHERFILES + this.projName + "_releaseList.csv";

            fileWriter = new FileWriter(fileTitle);

            writeToFile(fileWriter, "Index,Version ID,Version Name,Date");

            for (int i = 0; i < releases.size(); i++) {
                Release release = releases.get(i);
                int index = i + 1;

                writeToFile(fileWriter, index + "," + release.getVersionID() + "," +
                        release.getName() + "," + release.getDate().toString());
            }

        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "An error occurred while generating release info", e);
        } finally {
            closeWriter(fileWriter);
        }
    }

    public void generateTicketInfo(List<Ticket> ticketList) {
        FileWriter fileWriter = null;

        try {
            String fileTitle = this.directoryPath + OTHERFILES + this.projName + "_ticketList.csv";
            fileWriter = new FileWriter(fileTitle);

            writeToFile(fileWriter, "Key,Injected Version,Opening Version,Fixed Version, Affected Version List");

            for (Ticket ticket : ticketList) {
                String affectedVersions = getAffectedVersionsAsString(ticket);
                writeToFile(fileWriter, ticket.getTicketKey() + "," + ticket.getInjectedVersion().getName() + "," +
                        ticket.getOpeningVersion().getName() + "," + ticket.getFixedVersion().getName() + "," +
                        affectedVersions);
            }

        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        } finally {
            closeWriter(fileWriter);
        }
    }

    private String getAffectedVersionsAsString(Ticket ticket) {
        List<Release> affectedVersions = ticket.getAffectedVersionsList();
        StringBuilder stringBuilder = new StringBuilder();
        for (Release release : affectedVersions) {
            stringBuilder.append(release.getName()).append(" ");
        }
        return stringBuilder.toString();
    }

    public void generateTrainingSet(List<Release> releaseList, int walk) {
        FileWriter fileWriter = null;

        try {
            String fileTitle = this.directoryPath + TRAINING_CSV_FIVE + this.projName + "_trainingSet" + walk + ".csv";
            fileWriter = new FileWriter(fileTitle);

            writeToFile(fileWriter, "Version ID,Class Name,LOC,LOC_touched,Age,Revision number,Fix number,Author Number,LOC_added,MAX_LOC_added,AVG_LOC_added,churn,MAX_churn,Buggyness");

            for (int i = 0; i < releaseList.size(); i++) {
                Release release = releaseList.get(i);
                int index = i + 1;

                for (JavaClass javaClass : release.getJavaClassList()) {
                    writeClasses(fileWriter, index, javaClass);
                }
            }

        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        } finally {
            closeWriter(fileWriter);
        }
    }

    public void generateTestingSet(List<JavaClass> javaClassList, int testIndex) {
        FileWriter fileWriter = null;

        try {
            String fileTitle = this.directoryPath + TESTING_CSV_FIVE + this.projName + "_testingSet" + testIndex + ".csv";
            fileWriter = new FileWriter(fileTitle);

            writeToFile(fileWriter, "Version ID,Class Name,LOC,LOC_touched,Age,Revision number,Fix number,Author Number,LOC_added,MAX_LOC_added,AVG_LOC_added,churn,MAX_churn,Buggyness");

            for (JavaClass javaClass : javaClassList) {
                writeClasses(fileWriter, testIndex+1, javaClass);
            }

        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        } finally {
            closeWriter(fileWriter);
        }
    }

    private void writeClasses(FileWriter fileWriter, int index, JavaClass javaClass) throws IOException {
        writeToFile(fileWriter, index + "," + javaClass.getName() + "," +
                javaClass.getLocSize() + "," + javaClass.getLocTouched() + "," +
                javaClass.getAge().getYears() + "," + javaClass.getRevisionNumber() + "," +
                javaClass.getFixNumber() + "," + javaClass.getAuthorNumber() + "," +
                javaClass.getLocAdded() + "," + javaClass.getMaxLOCAdded() + "," +
                javaClass.getAvgLOCAdded() + "," + javaClass.getChurn() + "," +
                javaClass.getMaxChurn() + "," + javaClass.getBuggy());
    }

    public void moveFilesCSV() throws IOException {
        for (int i = 4; i <= 6; i++) {
            Path fileTrainingCSV = Path.of(this.directoryPath + TRAINING_CSV_FIVE + this.projName + "_trainingSet" + i + ".csv");
            Path destinationTrainingCSV = Path.of(this.directoryPath + TRAINING_CSV_THREE);
            Path targetTrainingCSV = destinationTrainingCSV.resolve(fileTrainingCSV.getFileName());
            Files.copy(fileTrainingCSV, targetTrainingCSV, StandardCopyOption.REPLACE_EXISTING);

            Path fileTestingCSV = Path.of(this.directoryPath + TESTING_CSV_FIVE + this.projName + "_testingSet" + i + ".csv");
            Path destinationTestingCSV = Path.of(this.directoryPath + TESTING_CSV_THREE);
            Path targetTestingCSV = destinationTestingCSV.resolve(fileTestingCSV.getFileName());
            Files.copy(fileTestingCSV, targetTestingCSV, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void moveFilesARFF() throws IOException {
        for (int i = 4; i <= 6; i++) {
            Path fileTrainingARFF = Path.of(this.directoryPath + TRAINING_ARFF_FIVE + this.projName + "_trainingSet" + i + ".arff");
            Path destinationTrainingARFF = Path.of(this.directoryPath + TRAINING_ARFF_THREE);
            Path targetTrainingARFF = destinationTrainingARFF.resolve(fileTrainingARFF.getFileName());
            Files.copy(fileTrainingARFF, targetTrainingARFF, StandardCopyOption.REPLACE_EXISTING);

            Path fileTestingARFF = Path.of(this.directoryPath + TESTING_ARFF_FIVE + this.projName + "_testingSet" + i + ".arff");
            Path destinationTestingARFF = Path.of(this.directoryPath + TESTING_ARFF_THREE);
            Path targetTestingARFF = destinationTestingARFF.resolve(fileTestingARFF.getFileName());
            Files.copy(fileTestingARFF, targetTestingARFF, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
