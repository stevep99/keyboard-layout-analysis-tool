package io.github.colemakmods.sa

import io.github.colemakmods.keyboard.LayoutResults

interface EffortModel {
    fun getTotalEffort(layoutResults: LayoutResults): Double
}
