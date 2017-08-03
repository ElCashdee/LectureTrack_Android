package com.example.liamc.lecturetrack;

/**
 * Class enabled creation of Student object in order to register.
 * Created 18/07/2017.
 *
 */

public class Student extends User {

    /**
     * Student number of Student.
     */
    private String studentNumber;

    /**
     * Constructor with parameter args for Student object.
     * Calls to super to set name. Sets student number.
     *
     */
    public Student(String name, String emailAddress, String UID,  String studentNumber ){
        super(name, emailAddress, UID);
        this.setStudentNumber(studentNumber);
    }

    /**
     * Default Constructor
     */
    public Student() {

    }

    /**
     * Getter for studentNumber
     * @return studentNumber String
     */
    public String getStudentNumber() {
        return studentNumber;
    }

    /**
     * Setter for studentNumber.
     * @param studentNumber String
     */
    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
        // TODO validation on studentNumber input. Will include on register activity.
    }
}
