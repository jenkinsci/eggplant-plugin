/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggPlant;

import java.io.Serializable;

/**
 *
 * @author TestPlant
 * This stores an individual result line of an eggPlant test
 */
public class eggPlantScriptLine implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = -4707284647292309587L;
    /** step */
    private int step;
    /** message */
    private String message;
    /** image */
    private String image;
    /** test */
    private String text;
    /** imageURL TODO: not working */
    private String imageURL;
    /** time */
    private String time;

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return isPassed
     */
    public boolean isPassed() {
        return (!"FAILURE".equals(message) && !"Exception".equals(message));
    }
}
