/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggPlant;

import au.com.bytecode.opencsv.CSVReader;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EggplantParser  implements FilePath.FileCallable<List<eggPlantResult>>
{
    private final String sut;
    private final String url;

    public EggplantParser(String sut, String url)
    {
        this.sut = sut;
        this.url = url;
    }

    @Override
    public ArrayList<eggPlantResult> invoke(File f, VirtualChannel channel)
    {
        ArrayList<eggPlantResult> results = new ArrayList<eggPlantResult>();
        FileReader fr = null;
        CSVReader reader = null;
        try
        {
            fr = new FileReader(f);
            reader = new CSVReader(fr);

            // skip the header row
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
                result.setScript(f.getParent());
                result.setSut(sut);

                getResultLines(result, f.getParent() + "/" + line[6], url);

                results.add(result);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(eggPlantBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(eggPlantBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            if (reader != null)
            {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(EggplantParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (fr != null)
            {
                try {
                    fr.close();
                } catch (IOException ex) {
                    Logger.getLogger(EggplantParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return results;
    }
    /**
     * Reads the individual result lines from an eggPlant results file
     *
     * @param result    parent of the result line
     * @param file      file containing the result lines
     * @param url       url to where images will be stored TODO: Not working
     */
    private void getResultLines(eggPlantResult result, String file, String url)
    {
        FileReader fr = null;
        CSVReader reader = null;

        try {
            // These files are tab separated
            fr = new FileReader(file);
            reader = new CSVReader(fr, '\t');
            // skip the header row
            String[] line = reader.readNext(); // throw away the header line

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
        finally{
            if (reader != null)
            {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(EggplantParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (fr != null)
            {
                try {
                    fr.close();
                } catch (IOException ex) {
                    Logger.getLogger(EggplantParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
