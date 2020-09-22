package io.github.colemakmods.web;

import org.teavm.jso.ajax.ReadyStateChangeHandler;
import org.teavm.jso.ajax.XMLHttpRequest;

/**
 * Created by steve on 19/04/15.
 */
public class ResourceLoader {

    private Resource resource;

    public static Resource loadFrequencyResource(String freqResourceName) {
        for (Resource freqResource : ResourceStatic.ALL_FREQS) {
            if (freqResourceName.equalsIgnoreCase(freqResource.getName())) {
                freqResource.setActive(true);
                ResourceLoader rl = new ResourceLoader(freqResource);
                rl.loadFile();
                return freqResource;
            }
        }
        return null;
    }

    public static void loadKeyboardResources() {
        for (Resource keyboardResource : ResourceStatic.ALL_LAYOUTS) {
            if (! keyboardResource.getPath().isEmpty()) {
                keyboardResource.setActive(true);
                ResourceLoader rl = new ResourceLoader(keyboardResource);
                rl.loadFile();
            }
        }
    }

    public static void loadConfigResources() {
        for (Resource configResource : ResourceStatic.ALL_CONFIGS) {
            configResource.setActive(true);
            ResourceLoader rl = new ResourceLoader(configResource);
            rl.loadFile();
        }
    }

    public ResourceLoader(final Resource resource) {
        this.resource = resource;
    }

    public void loadFile() {
        //System.out.println("loadFile " + path);
        final XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
            @Override
            public void stateChanged() {
                if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                    if (xhr.getStatus() == 200) {
                        receiveResponse(xhr.getResponseText());
                    }
                }
            }
        });
        xhr.open("GET", "resources/" + resource.getPath());
        xhr.send();
    }

    private void receiveResponse(String text) {
        resource.setText(text);
        System.out.println("loaded " + resource.getPath());
        updateLoadingState();
    }

    private static void updateLoadingState() {
        int loaded = 0;
        int total = 0;
        for (Resource freqResource : ResourceStatic.ALL_FREQS) {
            if (freqResource.isActive()) ++total;
            if (freqResource.getText() != null) ++loaded;
        }
        for (Resource keyboardResource : ResourceStatic.ALL_LAYOUTS) {
            if (keyboardResource.isActive()) ++total;
            if (keyboardResource.getText() != null) ++loaded;
        }
        for (Resource configResource : ResourceStatic.ALL_CONFIGS) {
            if (configResource.isActive()) ++total;
            if (configResource.getText() != null) ++loaded;
        }
        if (total == 0) {
            //prevent division by zero
            total = 1;
        }
        if (loaded < total) {
            KeyboardClient.setOutput("Loading..." + 100 * loaded / total + "%");
        } else {
            KeyboardClient.setOutput(null);
            KeyboardClient.setReadyState(true);
        }
    }
}
