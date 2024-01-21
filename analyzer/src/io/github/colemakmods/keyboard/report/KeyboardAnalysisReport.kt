package io.github.colemakmods.keyboard.report

import io.github.colemakmods.keyboard.LayoutResults
import java.io.IOException

/**
 * Created by steve on 10/05/15.
 */
interface KeyboardAnalysisReport {

    @Throws(IOException::class)
    fun generate(layoutResults: LayoutResults): String

}
