package io.github.colemakmods.sa

import io.github.colemakmods.keyboard.LayoutResults

class SFBEffortModel : EffortModel {
    override fun getTotalEffort(layoutResults: LayoutResults): Double {
        return layoutResults.totalFingerEffort()
    }
}
