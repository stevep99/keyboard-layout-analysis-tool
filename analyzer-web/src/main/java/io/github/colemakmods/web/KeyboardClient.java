package io.github.colemakmods.web;

import io.github.colemakmods.chars.BigramDataLoader;
import io.github.colemakmods.chars.BigramFreq;
import io.github.colemakmods.chars.CharDataLoader;
import io.github.colemakmods.chars.CharFreq;
import io.github.colemakmods.keyboard.KeyboardLayout;
import io.github.colemakmods.keyboard.KeyboardMapping;
import io.github.colemakmods.web.report.KeyboardAnalysisWebHTMLReport;
import io.github.colemakmods.web.teavm.HTMLTextAreaElement;
import io.github.colemakmods.keyboard.KeyboardConfig;
import io.github.colemakmods.keyboard.KeyboardAnalysis;
import io.github.colemakmods.keyboard.report.KeyboardAnalysisReport;
import io.github.colemakmods.keyboard.LayoutResults;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLOptionElement;
import org.teavm.jso.dom.html.HTMLSelectElement;

import java.util.List;
import java.io.IOException;

public class KeyboardClient {
    public static final String VERSION = "v1.33";
    private static final String DEFAULT_FREQ_RESOURCE = "en";
    private static final int DEFAULT_BIGRAM_LIST_SIZE = 5;

    private static Window window = Window.current();
    private static HTMLDocument document = HTMLDocument.current();
    private static HTMLElement versionText = document.getElementById("version-text");
    private static HTMLSelectElement layoutSelect = (HTMLSelectElement) document.getElementById("layout-select");
    private static HTMLSelectElement configSelect = (HTMLSelectElement) document.getElementById("config-select");
    private static HTMLTextAreaElement layoutInput = (HTMLTextAreaElement)document.getElementById("layout-input");
    private static HTMLTextAreaElement configInput = (HTMLTextAreaElement) document.getElementById("config-input");
    private static HTMLTextAreaElement modalLayoutInput = (HTMLTextAreaElement)document.getElementById("modal-layout-input");
    private static HTMLTextAreaElement modalConfigInput = (HTMLTextAreaElement) document.getElementById("modal-config-input");
    private static HTMLElement saveInputButton = document.getElementById("save-input-button");
    private static HTMLElement closeInputButton = document.getElementById("close-input-button");
    private static HTMLElement analyzeButton = document.getElementById("analyze-button");
    private static HTMLElement keyboardPanelFingers = document.getElementById("keyboard-panel-fingers");
    private static HTMLElement keyboardPanelHeatmap = document.getElementById("keyboard-panel-heatmap");
    private static HTMLElement outputPanel = document.getElementById("output-panel");

    private static boolean readyState = false;
    private static Resource selectedFreqResource;
    private static int bigramListSize = DEFAULT_BIGRAM_LIST_SIZE;

    public static void main(String[] args) {
        //System.out.println("KeyboardClient start");

        versionText.setInnerHTML(VERSION);

        setOutput("Initializing...");
        setKeyboadHeatmapPanel(null);

        String freqResourceParam = DEFAULT_FREQ_RESOURCE;

        String fullURL = window.getLocation().getFullURL();
        if (fullURL.contains("?")) {
            String queryString = fullURL.substring(fullURL.indexOf('?') + 1);
            String[] queryArgs = queryString.split("&");
            for (String queryArg : queryArgs) {
                String[] nameVal = queryArg.split("=");
                if ("freq".equals(nameVal[0])) {
                    freqResourceParam = nameVal[1];
                } else if ("bigrams".equals(nameVal[0])) {
                    try {
                        bigramListSize = Integer.parseInt(nameVal[1]);
                    } catch (NumberFormatException ex) {}
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
        HTMLOptionElement layoutOptionCustom = (HTMLOptionElement) document.getElementById("layout-option-custom");
        layoutSelect.getOptions().remove(0);
        for (Resource keyboardResource : ResourceStatic.ALL_LAYOUTS) {
            HTMLOptionElement layoutOption = (HTMLOptionElement)layoutOptionCustom.cloneNode(true);
            layoutOption.setText(keyboardResource.getName());
            layoutOption.setDisabled(keyboardResource.getPath().isEmpty());
            layoutSelect.getOptions().add(layoutOption);
        }
        layoutSelect.getOptions().add(layoutOptionCustom);
        layoutSelect.addEventListener("change", event -> {
            int selected = layoutSelect.getSelectedIndex();
            if (selected < ResourceStatic.ALL_LAYOUTS.length) {
                //update input layout unless "custom" is selected
                Resource keyboardResource = ResourceStatic.ALL_LAYOUTS[selected];
                if (! keyboardResource.getPath().isEmpty()) {
                    setLayoutInput(keyboardResource.getText());
                    selectConfigOption(keyboardResource.getInfo());

                    refreshInputKeyboardPanel();
                    setKeyboadHeatmapPanel(null);
                    setOutput(null);
                }
            }
        });

        //config selector
        HTMLOptionElement configOptionCustom = (HTMLOptionElement) document.getElementById("config-option-custom");
        configSelect.getOptions().remove(0);
        for (Resource configResource : ResourceStatic.ALL_CONFIGS) {
            HTMLOptionElement configOption = (HTMLOptionElement)configOptionCustom.cloneNode(true);
            configOption.setText(configResource.getName());
            configOption.setTitle(configResource.getInfo());
            configSelect.getOptions().add(configOption);
        }
        configSelect.getOptions().add(configOptionCustom);
        configSelect.addEventListener("change", event -> {
            int selected = configSelect.getSelectedIndex();
            if (selected < ResourceStatic.ALL_CONFIGS.length) {
                //update input config unless "custom" is selected
                Resource configResource = ResourceStatic.ALL_CONFIGS[selected];
                setConfigInput(configResource.getText());

                refreshInputKeyboardPanel();
                setKeyboadHeatmapPanel(null);
                setOutput(null);
            }
        });

        saveInputButton.addEventListener("click", event -> {
            //copy the layout text from the modal to the input field
            if (!modalLayoutInput.getValue().equals(layoutInput.getValue())) {
                layoutInput.setValue(modalLayoutInput.getValue());
                //select the "Custom" option if config has changed
                layoutSelect.setSelectedIndex(ResourceStatic.ALL_LAYOUTS.length);
            }

            //copy the config text from the modal to the input field
            if (!modalConfigInput.getValue().equals(configInput.getValue())) {
                configInput.setValue(modalConfigInput.getValue());
                //select the "Custom" option if config has changed
                configSelect.setSelectedIndex(ResourceStatic.ALL_CONFIGS.length);
            }

            //close the modal dialog
            closeInputButton.click();

            refreshInputKeyboardPanel();
            setKeyboadHeatmapPanel(null);
            setOutput(null);
        });

        //button to perform analysis
        analyzeButton.addEventListener("click", event -> {
            if (readyState) {
                performAnalyze();
            }
        });
    }

    private static void selectConfigOption(String configName) {
        for (int i=0; i<configSelect.getOptions().getLength(); ++i) {
            if (configName.equals(configSelect.getOptions().item(i).getText())) {
                configSelect.setSelectedIndex(i);
                Resource configResource = ResourceStatic.ALL_CONFIGS[i];
                setConfigInput(configResource.getText());
                break;
            }
        }
    }

    private static void refreshInputKeyboardPanel() {
        KeyboardLayout keyboardLayout = new KeyboardLayout(layoutSelect.getValue());
        KeyboardMapping.initialize(keyboardLayout, layoutInput.getValue());
        KeyboardConfig.initialize(keyboardLayout, configInput.getValue());
        HTMLKeyboardRenderer keyboardRenderer = new HTMLKeyboardRenderer(keyboardLayout, null);

        if (keyboardPanelFingers.getFirstChild() != null) {
            keyboardPanelFingers.clear();
        }

        HTMLElement fingersElement = keyboardRenderer.generate(document, false);
        if (fingersElement != null) {
            keyboardPanelFingers.appendChild(fingersElement);
            keyboardPanelFingers.setAttribute("style", "display:block");
        } else {
            keyboardPanelFingers.setAttribute("style", "display:none");
        }

    }

    public static void setReadyState(boolean state) {
        readyState = state;
        if (readyState) {
            //set Mod-DH as the default layout
            layoutSelect.setSelectedIndex(0);
            Resource keyboardResource = ResourceStatic.ALL_LAYOUTS[layoutSelect.getSelectedIndex()];
            setLayoutInput(keyboardResource.getText());
            //select Ergonomic as default config
            configSelect.setSelectedIndex(2);
            Resource configResource = ResourceStatic.ALL_CONFIGS[configSelect.getSelectedIndex()];
            setConfigInput(configResource.getText());

            refreshInputKeyboardPanel();
        }
    }

    private static void performAnalyze() {
        KeyboardLayout keyboardLayout = new KeyboardLayout(layoutSelect.getValue());
        KeyboardMapping.initialize(keyboardLayout, layoutInput.getValue());
        KeyboardConfig.initialize(keyboardLayout, configInput.getValue());

        List<String> messages = keyboardLayout.validate();
        if (!messages.isEmpty()) {
            StringBuffer sb = new StringBuffer("\n[A layout configuration error occurred]\n");
            for (String message : messages) {
                sb.append(message).append('\n');
            }
            setOutput(sb.toString());
            return;
        }

        if (selectedFreqResource == null || selectedFreqResource.getText().length() == 0) {
            setOutput("\n[ An error occurred. Frequency data was missing. ]\n");
            return;
        }

//        keyboardLayout.dumpLayout(System.out);
//        keyboardLayout.dumpConfig(System.out);

        List<CharFreq> charFreqs = new CharDataLoader().initialize(keyboardLayout.generateAlphabet(), selectedFreqResource.getText());
        List<BigramFreq> bigramFreqs = new BigramDataLoader().initialize(keyboardLayout.generateAlphabet(), selectedFreqResource.getText());

        KeyboardAnalysis ka = new KeyboardAnalysis();
        LayoutResults layoutResults = ka.performAnalysis(keyboardLayout, charFreqs, bigramFreqs);

        HTMLKeyboardRenderer keyboardRenderer = new HTMLKeyboardRenderer(keyboardLayout, layoutResults.getKeyFreq());
        setKeyboadHeatmapPanel(keyboardRenderer.generate(document, true));

        try {
            KeyboardAnalysisReport report = new KeyboardAnalysisWebHTMLReport(bigramListSize);

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

    public static void setKeyboadHeatmapPanel(HTMLElement heatmapElt) {
        if (keyboardPanelHeatmap.getFirstChild() != null) {
            keyboardPanelHeatmap.clear();
        }

        if (heatmapElt != null) {
            keyboardPanelHeatmap.appendChild(heatmapElt);
            keyboardPanelHeatmap.setAttribute("style", "display:block");
        } else {
            keyboardPanelHeatmap.setAttribute("style", "display:none");
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
                freqMessage = "Using frequency information: " + selectedFreqResource.getInfo();
            }
            outputPanel.setInnerHTML(freqMessage + "\n\nReady.");
        }

    }

}
