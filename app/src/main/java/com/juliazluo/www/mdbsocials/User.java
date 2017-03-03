package com.juliazluo.www.mdbsocials;

/**
 * Created by julia on 2017-02-23.
 */

public class User {

    private String name, imageURI;

    public User(String name, String imageURI) {
        this.name = name;
        this.imageURI = imageURI;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }
}
