package io.github.colemakmods.web.teavm;

import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.JSProperty;

/**
 * Created by steve on 23/04/15.
 */
public interface HTMLTextAreaElement extends HTMLElement {

    @JSProperty
    String getValue();

    @JSProperty
    void setValue(String value);
}
