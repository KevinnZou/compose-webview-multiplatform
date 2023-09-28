package com.multiplatform.webview.web

interface InitProgress {

    fun locating() {}

    fun downloading(progress: Float) {}

    fun extracting() {}

    fun install() {}

    fun initializing() {}

    fun initialized() {}

    class Builder {
        private var locatingCallback: () -> Unit = { }
        private var downloadingCallback: (Float) -> Unit = { }
        private var extractingCallback: () -> Unit = { }
        private var installCallback: () -> Unit = { }
        private var initializingCallback: () -> Unit = { }
        private var initializedCallback: () -> Unit = { }

        fun onLocating(callback: () -> Unit) = apply {
            locatingCallback = callback
        }

        fun onDownloading(callback: (progress: Float) -> Unit) = apply {
            downloadingCallback = callback
        }

        fun onExtracting(callback: () -> Unit) = apply {
            extractingCallback = callback
        }

        fun onInstall(callback: () -> Unit) = apply {
            installCallback = callback
        }

        fun onInitializing(callback: () -> Unit) = apply {
            initializingCallback = callback
        }

        fun onInitialized(callback: () -> Unit) = apply {
            initializedCallback = callback
        }

        fun build(): InitProgress = object : InitProgress {
            override fun locating() {
                locatingCallback()
            }

            override fun downloading(progress: Float) {
                downloadingCallback(progress)
            }

            override fun extracting() {
                extractingCallback()
            }

            override fun install() {
                installCallback()
            }

            override fun initializing() {
                initializingCallback()
            }

            override fun initialized() {
                initializedCallback()
            }
        }
    }
}