package com.fenchtose.contactsdemo;

import java.util.ArrayList;

/**
 * Created by aseem on 2/19/16.
 */
public class Patient {
    private String firstname;
    private String lastname;
    private String email;
    private ArrayList<String> dates;
    private long phonenumber;
    //private boolean registered;
    ///private char[] passcode;
    //private String Location;
    public Patient(String firstname, String lastname, String email,int phonenumber) {
        //this.registered=false;
        //this.passcode=null;
        this.email=email;
        this.firstname=firstname;
        this.lastname=lastname;
        this.phonenumber=phonenumber;
        this.dates = new ArrayList<String>();
    }
    public void setName(String first,String last) {
        firstname = first;
        lastname= last;
    }
    public ArrayList<String> setDates()
    {
        return dates;
    }
    public void setDates( ArrayList<String> settupdate) {
        dates= settupdate;

    }
    public void addDates(String date)
    {
        dates.add(date);
    }
    public String getName()
    {
        return firstname+" "+lastname;
    }
    public String getFirstname()
    {
        return firstname;
    }
    public String getLastname()
    {
        return lastname;
    }
    public void setNumber(long number) {

        phonenumber= number;
    }
    public void setEmail(String email_address)
    {
        email=email_address;
    }
    public String getEmail()
    {
        return email;
    }
}
