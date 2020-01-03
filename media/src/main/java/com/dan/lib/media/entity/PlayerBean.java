package com.dan.lib.media.entity;

public class PlayerBean {
    //资源
    private String resources;

    private String bg;

    public PlayerBean(String resources) {
        this.resources = resources;
    }

    public PlayerBean(String resources, String bg) {
        this.resources = resources;
        this.bg = bg;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}
