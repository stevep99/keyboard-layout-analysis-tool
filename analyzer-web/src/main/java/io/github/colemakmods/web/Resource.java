package io.github.colemakmods.web;

/**
 * Created by steve on 26/04/15.
 */
public class Resource {

    private String name;
    private String title;
    private String path;
    private String text;
    private boolean active;

    public Resource(String name, String title, String path) {
        this.name = name;
        this.title = title;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
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
