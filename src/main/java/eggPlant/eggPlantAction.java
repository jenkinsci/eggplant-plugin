package eggPlant;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import java.io.Serializable;

import java.util.ArrayList;

/**
 * @author testplant
 * This file sets the right hand menu in a job run results
 * A user can click on the menu and see the eggPlant Results for the job
 */
public class eggPlantAction implements Action, Serializable {

    /** build */
    private AbstractBuild<?, ?> build;

    /** result */
    private ArrayList<eggPlantResult> resultList = new ArrayList<eggPlantResult>();

    /**
     * @param build
     */
    public eggPlantAction(AbstractBuild<?, ?> build) {
        super();
        this.build = build;
    }

    /**
     * @see hudson.model.Action#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return "eggPlant Results";
    }

    /**
     * @see hudson.model.Action#getIconFileName()
     */
    @Override
    public String getIconFileName() {
//        return "clipboard.gif";
        return "/plugin/eggplant-plugin/eggplantLogo.png";
    }

    /**
     * @see hudson.model.Action#getUrlName()
     */
    @Override
    public String getUrlName() {
        return "eggplant";
    }

    /**
     * @return build
     */
    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    /**
     * @return resultList
     */
    public ArrayList<eggPlantResult> getResultList() {
        return resultList;
    }

}
