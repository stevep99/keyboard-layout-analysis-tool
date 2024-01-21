package io.github.colemakmods.keyboard

class Options {
    var outputFormat = DEFAULT_OUTPUT_FORMAT
    var sfbListSize = DEFAULT_SFB_LIST_SIZE
    var nfbListSize = DEFAULT_NFB_LIST_SIZE

    companion object {
        private const val DEFAULT_OUTPUT_FORMAT = "t"
        private const val DEFAULT_SFB_LIST_SIZE = 10
        private const val DEFAULT_NFB_LIST_SIZE = 5
    }
}
