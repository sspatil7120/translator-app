package com.thinkitive.translator;
import org.apache.tools.ant.DirectoryScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.Properties;
import java.util.Set;
@Component
public class TransformerSource {
    private String inputFile;
    private String baseDir;
    private String csvFilepath;
    private String frenchFile;
    private String englishFile;
    @Autowired
    public TransformerSource(@Value("${app.inputFile}") String inputFile_val, @Value("${app.baseDir}") String baseDir_val, @Value("${app.csvFilepath}") String csvFilepath_val, @Value("${app.frenchFile}") String frenchFile_val, @Value("${app.englishFile}") String EnglishFile_val) throws IOException {
        this.inputFile = inputFile_val;
        this.baseDir = baseDir_val;
        this.csvFilepath = csvFilepath_val;
        this.frenchFile = frenchFile_val;
        this.englishFile = EnglishFile_val;
        getTargetFiles(inputFile, frenchFile);
        getTargetFiles(inputFile, englishFile);
    }

    public void getTargetFiles(String path, String fileType) throws IOException {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{fileType});
        scanner.setBasedir(baseDir);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        getTranslationProperties(files, path);
    }

    public void getTranslationProperties(String[] files, String path) throws IOException {
        String tempPath = path;
        for (int i = 0; i < files.length; i++) {
            //Get path of the Files and read properties of each File
            path += files[i];
            String delimiter = ",";
            FileReader inputFileReader = null;
            File inputFile = new File(path);
            inputFileReader = new FileReader(inputFile);
            Properties properties = new Properties();
            try {
                properties.load(inputFileReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Set<String> keys = properties.stringPropertyNames();
            /* Step 2 Read the CSV of translation */
            for (String key : keys) {
                String[] splitField = null;
                String csvFile = csvFilepath;
                File file = new File(csvFile);
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                br.readLine();
                String line = "";
                String cellArr[];
                while ((line = br.readLine()) != null) {
                    cellArr = line.split(delimiter);
                    if (cellArr[0].equals(key)) {
                        properties.setProperty(cellArr[0], cellArr[1]);
                    }
                }
                br.close();
            }
            /* file code to update each file */
            FileReader reader = null;
            FileWriter writer = null;
            File file = new File(path);
            try {
                reader = new FileReader(file);
                writer = new FileWriter(file);
                Properties p = new Properties();
                p.load(reader);
                for (String key : keys) {
                    p.setProperty(key, properties.getProperty(key));
                }
                p.store(writer, "write to file");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            path = tempPath;
        }
    }

}
