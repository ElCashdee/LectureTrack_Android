package com.example.liamc.lecturetrack;

/**
 * Module class. Allows creation of Module object that refers to a taught module on which
 * HR tracking is offered. Utilised to conveniently store the code, title and convener of the Module
 * so these can be displayed in a list to users as available Modules with which to register for HR tracking.
 *
 * moduleCode, moduleTitle and moduleConvener instance variables are Strings.
 */

public class Module {

    // instance vars

    /**
     * Module's Unique Code String
     */
    private String moduleCode;

    /**
     * Module's title String
     */
    private String moduleTitle;


    /**
     * Module's convener or main lecturer
     */
    private String moduleConvener;


    // constructors

    /**
     * Constructor with args. Allows creation of new Module object,
     * must be passed moduleCode, moduleTitle and moduleConvener as String.
     *
     * Calls to setters to set these values.
     *
     * @param moduleCode String
     * @param moduleTitle String
     * @param moduleConvener String
     */
    public Module(String moduleCode, String moduleTitle, String moduleConvener) {
        this.setModuleCode(moduleCode);
        this.setModuleTitle(moduleTitle);
        this.setModuleConvenor(moduleConvener);

    }


    // getters and setters

    /**
     * Method returns moduleCode
     * @return moduleCode String
     */
    public String getModuleCode() {
        return moduleCode;
    }

    /**
     * Setter for moduleCode
     * @param moduleCode String
     */
    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    /**
     * Getter for moduleTitle
     * @return moduleTitle String
     */
    public String getModuleTitle() {
        return moduleTitle;
    }

    /**
     * Setter for moduleTitle.
     * @param moduleTitle String
     */
    public void setModuleTitle(String moduleTitle) {
        this.moduleTitle = moduleTitle;
    }

    /**
     * Getter for moduleConvener
     * @return moduleConvener String
     */
    public String getModuleConvener() {
        return moduleConvener;
    }

    /**
     * Setter for moduleConvener
     * @param moduleConvener String
     */
    public void setModuleConvenor(String moduleConvener) {
        this.moduleConvener = moduleConvener;
    }
}
