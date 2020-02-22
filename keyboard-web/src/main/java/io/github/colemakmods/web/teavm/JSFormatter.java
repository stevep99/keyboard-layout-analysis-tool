package io.github.colemakmods.web.teavm;

import org.teavm.jso.JSBody;

/**
 * Created by steve on 27/04/15.
 */
public class JSFormatter {

    @JSBody(params = { "f", "n" }, script = "return f.toFixed(n);")
    public static native String toFixed(float f, int n);

    @JSBody(params = { "d", "n" }, script = "return d.toFixed(n);")
    public static native String toFixed(double d, int n);

}
