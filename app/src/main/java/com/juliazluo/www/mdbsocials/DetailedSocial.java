package com.juliazluo.www.mdbsocials;

/**
 * Created by julia on 2017-03-02.
 */

public class DetailedSocial {

    private String id, name, email, imageName, description, date;
    private int numRSVP;

    public DetailedSocial(String id, String name, String email, String imageName, String description,
                          String date, int numRSVP) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageName = imageName;
        this.description = description;
        this.date = date;
        this.numRSVP = numRSVP;
    }

    public DetailedSocial() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNumRSVP() {
        return numRSVP;
    }

    public void setNumRSVP(int numRSVP) {
        this.numRSVP = numRSVP;
    }
}
