package com.unizar.seu.coffeConnect.model;

import java.io.Serializable;

public class Coffee implements Serializable {

    private static final String[] tempLevel = {"BAJA", "MEDIA", "ALTA"};
    private int water, aroma, temp, idImage;
    private String name;

    public Coffee(String name, int water, int aroma, int temp, int idImage) {
        this.water = water;
        this.aroma = aroma;
        this.temp = temp;
        this.name = name;
        this.idImage = idImage;
    }

    public String getName() {
        return name;
    }

    public int getWater() {
        return water;
    }

    public int getAroma() {
        return aroma;
    }

    public int getTemp() {
        return temp;
    }

    public String getTempName(){ return tempLevel[temp]; }

    public int getIdImage() {
        return idImage;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public void setAroma(int aroma) {
        this.aroma = aroma;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
