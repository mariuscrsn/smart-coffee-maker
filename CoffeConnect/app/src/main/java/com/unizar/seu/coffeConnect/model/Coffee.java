package com.unizar.seu.coffeConnect.model;

import com.unizar.seu.coffeConnect.R;

public class Coffee {

    private int water, aroma, idImage;
    private String temp, name;

    public Coffee(String name, int water, int aroma, String temp, int idImage) {
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

    public String getTemp() {
        return temp;
    }

    public int getIdImage() {
        return idImage;
    }
}
