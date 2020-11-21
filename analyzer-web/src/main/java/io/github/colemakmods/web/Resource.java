package io.github.colemakmods.web;

/**
 * Created by steve on 26/04/15.
 */
public class Resource {

    private String name;
    private String info;
    private String path;
    private String text;
    private boolean active;

    public Resource(String name, String info, String path) {
        this.name = name;
        this.info = info;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getPath() {
        return path;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static Resource lookupName(String name, Resource[] allResources) {
        for(int i=0; i<allResources.length; ++i) {
            if (allResources[i].getName().equalsIgnoreCase(name)) {
                return allResources[i];
            }
        }
        return null;
    }

}
