package com.HiWord9.RPRenames;

public class Rename {
    private String[] name;

    public Rename(String[] name) {
        this.name = name;
    }

    public String[] getName() {
        return name;
    }

    public String getName(int a) {
        return name[a];
    }

    public void setName(String[] name) {
        this.name = name;
    }
}
