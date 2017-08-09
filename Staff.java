package com.example.liamc.lecturetrack;

/**
 * Class that enables creation of Staff object with firstName, lastName and staffNumber.
 * Created 18/07/17
 */

public class Staff extends User {

    /**
     * String staffNumber
     */
    private String staffNumber;

    /**
     * Constructor with parameter arguments.
     * @param name String
     * @param emailAddress String
     * @param staffNumber String
     */
    public Staff(String name, String emailAddress, String UID,  String staffNumber) {
        super(name, emailAddress, UID);
        this.setStaffNumber(staffNumber);
    }

    /**
     * Default Constructor
     */
    public Staff() {

    }

    /**
     * Getter for staffNumber
     * @return staffNumber String
     */
    public String getStaffNumber() {
        return staffNumber;
    }

    /**
     * Setter for staffNumber
     * @param staffNumber String
     */
    public void setStaffNumber(String staffNumber) {
        this.staffNumber = staffNumber;
    }
}
