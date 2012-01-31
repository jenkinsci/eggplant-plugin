package eggPlant;

import au.com.bytecode.opencsv.CSVReader;
import java.io.FileReader;

/**
 *
 * @author afisher
 */
public class EggplantParser {
    

    public EggplantParser() {
        
    }
    /**
     * Converts the result files for eggPlant into Jenkins format so that it
     * can be display in Jenkins
     * 
     * @param action    action in which to store the results
     * @param startDir  Job directory where eggPlant was told to put the results
     * @param url       URL for the screenshots TODO: Not working
     * @return 
     */
     boolean parseResult(eggPlantAction action, String startDir, String url, String scriptName, String sut) {

        boolean passed = true;

        try {
            // RunHistory.csv contains the high level results and the filename of the 
            // result lines
            CSVReader reader = new CSVReader(new FileReader(startDir + "/RunHistory.csv"));
            String[] line = reader.readNext();

            // Loop round reading each line
            while ((line = reader.readNext()) != null) {
                eggPlantResult result = new eggPlantResult();
                result.setRunDate(line[0]);
                result.setDuration(line[2]);
                result.setPassed(line[1].equals("Success"));
                result.setErrors(line[3]);
                result.setWarnings(line[4]);
                result.setExceptions(line[5]);
                result.setScript(scriptName);
                result.setSut(sut);
                
                passed = passed & line[1].equals("Success");

                getResultLines(result, startDir + "/" + line[6], url);

                action.getResultList().add(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return passed;
    }

    /**
     * Reads the individual result lines from an eggPlant results file
     * 
     * @param result    parent of the result line
     * @param file      file containing the result lines
     * @param url       url to where images will be stored TODO: Not working
     */
    private void getResultLines(eggPlantResult result, String file, String url) {
        try {
            // These files are tab separated
            CSVReader reader = new CSVReader(new FileReader(file), '\t');
            String[] line = reader.readNext();

            int step = 1;
            // Loop round reading each line
            while ((line = reader.readNext()) != null) {
                eggPlantScriptLine resultLine = new eggPlantScriptLine();
                resultLine.setStep(step++);
                resultLine.setTime(line[0]);
                resultLine.setMessage(line[1]);
                resultLine.setImage(line[2]);
                resultLine.setText(line[3]);
                resultLine.setImageURL(url + "/" + line[2]);  // TODO : How to get the image!!!!

                result.addScriptLine(resultLine);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    
}
