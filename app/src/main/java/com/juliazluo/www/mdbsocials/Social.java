package com.juliazluo.www.mdbsocials;

/**
 * Created by julia on 2017-02-19.
 */

public class Social implements Comparable<Social> {

    private String id, name, email, imageName;
    private long numRSVP, timestamp;

    public Social(String id, String name, String email, long numRSVP, String imageName, long timestamp) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.numRSVP = numRSVP;
        this.imageName = imageName;
        this.timestamp = timestamp;
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

    public long getNumRSVP() {
        return numRSVP;
    }

    public void setNumRSVP(long numRSVP) {
        this.numRSVP = numRSVP;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(Social other) {
        return (int) (other.timestamp - this.timestamp);
    }
}
