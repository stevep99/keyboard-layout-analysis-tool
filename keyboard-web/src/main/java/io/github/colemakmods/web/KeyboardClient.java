package io.github.colemakmods.web;

import io.github.colemakmods.chars.BigramFreq;
import io.github.colemakmods.chars.CharFreq;
import io.github.colemakmods.keyboard.KeyboardLayout;
import io.github.colemakmods.web.teavm.HTMLTextAreaElement;
import io.github.colemakmods.keyboard.FingerConfig;
import io.github.colemakmods.keyboard.KeyboardAnalysis;
import io.github.colemakmods.keyboard.KeyboardAnalysisReport;
import io.github.colemakmods.keyboard.LayoutResults;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.*;

import java.io.IOException;

public class KeyboardClient {
    public static final String VERSION = "v1.19";
    private static final String DEFAULT_FREQ_RESOURCE = "en";

    private static Window window = Window.current();
    private static HTMLDocument document = HTMLDocument.current();
    private static HTMLElement versionText = document.getElementById("version-text");
    private static HTMLSelectElement layoutSelect = (HTMLSelectElement) document.getElementById("layout-select");
    private static HTMLSelectElement configSelect = (HTMLSelectElement) document.getElementById("config-select");
    private static HTMLTextAreaElement layoutInput = (HTMLTextAreaElement)document.getElementById("layout-input");
    private static HTMLTextAreaElement configInput = (HTMLTextAreaElement) document.getElementById("config-input");
    private static HTMLElement analyzeButton = document.getElementById("analyze-button");
    private static HTMLElement keyboardPanel = document.getElementById("keyboard-panel");
    private static HTMLElement outputPanel = document.getElementById("output-panel");
    private static boolean readyState = false;
    private static Resource selectedFreqResource;

    public static void main(String[] args) {
        //System.out.println("KeyboardClient start");

        versionText.setInnerHTML(VERSION);

        setOutput("Initializing...");
        setKeyboadPanel(null);

        String freqResourceParam = DEFAULT_FREQ_RESOURCE;

        String fullURL = window.getLocation().getFullURL();
        if (fullURL.contains("?")) {
            String queryString = fullURL.substring(fullURL.indexOf('?') + 1);
            String[] queryArgs = queryString.split("&");
            for (String queryArg : queryArgs) {
                String[] nameVal = queryArg.split("=");
                if ("freq".equals(nameVal[0])) {
                    freqResourceParam = nameVal[1];
                }
            }
        }
        System.out.println("Using frequency resource: " + freqResourceParam);

        selectedFreqResource = ResourceLoader.loadFrequencyResource(freqResourceParam);
        if (selectedFreqResource != null) {
            System.out.println("Using frequency resource: " + selectedFreqResource.getName());
        }
        ResourceLoader.loadKeyboardResources();
        ResourceLoader.loadConfigResources();

        //layout selector
        HTMLOptionElement layoutOptionDummy = (HTMLOptionElement) document.getElementById("layoutOptionDummy");
        layoutSelect.getOptions().remove(0);
        for (Resource keyboardResource : ResourceStatic.ALL_KEYBOARDS) {
            HTMLOptionElement layoutOption = (HTMLOptionElement)layoutOptionDummy.cloneNode(true);
            layoutOption.setText(keyboardResource.getName());
            layoutSelect.getOptions().add(layoutOption);
        }
        layoutSelect.addEventListener("change", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                Resource keyboardResource = ResourceStatic.ALL_KEYBOARDS[layoutSelect.getSelectedIndex()];
                setLayoutInput(keyboardResource.getText());
                setKeyboadPanel(null);
                setOutput(null);
            }
        });

        //config selector
        HTMLOptionElement configOptionDummy = (HTMLOptionElement) document.getElementById("configOptionDummy");
        configSelect.getOptions().remove(0);
        for (Resource configResource : ResourceStatic.ALL_CONFIGS) {
            HTMLOptionElement configOption = (HTMLOptionElement)configOptionDummy.cloneNode(true);
            configOption.setText(configResource.getName());
            configOption.setTitle(configResource.getTitle());
            configSelect.getOptions().add(configOption);
        }
        configSelect.setSelectedIndex(2); //select Ergonomic as default option
        configSelect.addEventListener("change", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                Resource configResource = ResourceStatic.ALL_CONFIGS[configSelect.getSelectedIndex()];
                setConfigInput(configResource.getText());
                setKeyboadPanel(null);
                setOutput(null);
            }
        });

        analyzeButton.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event event) {
                if (readyState) {
                    performAnalyze();
                }
            }
        });
    }

    public static void setReadyState(boolean state) {
        readyState = state;
        if (readyState) {
            Resource keyboardResource = ResourceStatic.ALL_KEYBOARDS[layoutSelect.getSelectedIndex()];
            setLayoutInput(keyboardResource.getText());
            Resource configResource = ResourceStatic.ALL_CONFIGS[configSelect.getSelectedIndex()];
            setConfigInput(configResource.getText());
        }
    }

    private static void performAnalyze() {
        KeyboardLayout keyboardLayout = new KeyboardLayout();
        keyboardLayout.parse(layoutInput.getValue(), "");

        FingerConfig fingerConfig = new FingerConfig();
        fingerConfig.parse(keyboardLayout, configInput.getValue());

        if (selectedFreqResource == null || selectedFreqResource.getText().length() == 0) {
            setOutput("\n[ An error occurred. Frequency data was missing. ]\n");
            return;
        }

        CharFreq[] charFreqs = CharFreq.initialize(keyboardLayout.getAlphabet(), selectedFreqResource.getText());
        BigramFreq[] bigramFreqs = BigramFreq.initialize(keyboardLayout.getAlphabet(), selectedFreqResource.getText());

        //layout.dump(System.out);

        HTMLKeyboardRenderer keyboardRenderer = new HTMLKeyboardRenderer(keyboardLayout);
        HTMLElement keyboardElt = keyboardRenderer.generate(document);
        setKeyboadPanel(keyboardElt);

        KeyboardAnalysis ka = new KeyboardAnalysis();
        LayoutResults layoutResults = ka.performAnalysis(keyboardLayout, charFreqs, bigramFreqs);

        try {
            KeyboardAnalysisReport report = new KeyboardAnalysisWebHTMLReport(5);

            String output = report.generate(layoutResults);
            setOutput(output);

        } catch (IOException e) {
            //e.printStackTrace();
            setOutput("\n[ An error occurred. Please check inputs ]\n");
        }

    }

    public static void setLayoutInput(String input) {
        if (layoutInput.getFirstChild() != null) {
            layoutInput.removeChild(layoutInput.getFirstChild());
        }
        layoutInput.appendChild(document.createTextNode(input));
        layoutInput.setValue(input);
    }

    public static void setConfigInput(String input) {
        if (configInput.getFirstChild() != null) {
            configInput.removeChild(configInput.getFirstChild());
        }
        configInput.appendChild(document.createTextNode(input));
        configInput.setValue(input);
    }

    public static void setKeyboadPanel(HTMLElement keyboardElt) {
        if (keyboardPanel.getFirstChild() != null) {
            keyboardPanel.removeChild(keyboardPanel.getFirstChild());
        }
        if (keyboardElt != null) {
            keyboardPanel.appendChild(keyboardElt);
            keyboardPanel.setHidden(false);
        } else {
            keyboardPanel.setHidden(true);
        }
    }

    public static void setOutput(String output) {
        if (outputPanel.getFirstChild() != null) {
            outputPanel.removeChild(outputPanel.getFirstChild());
        }
        if (output != null) {
            //outputPanel.appendChild(document.createTextNode(output));
            outputPanel.setInnerHTML(output);

        } else {

            String freqMessage = "";
            if (selectedFreqResource == null) {
                freqMessage = "No frequency data found";
            } else {
                freqMessage = "Using frequency information: " + selectedFreqResource.getTitle();
            }
            outputPanel.setInnerHTML(freqMessage + "\n\nReady.");
        }

    }

}
