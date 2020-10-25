package io.github.colemakmods.keyboard;

import java.io.IOException;
import java.util.List;

/**
 * Created by steve on 10/05/15.
 */
public interface KeyboardAnalysisReport {
    String generate(LayoutResults layoutResults) throws IOException;
}
