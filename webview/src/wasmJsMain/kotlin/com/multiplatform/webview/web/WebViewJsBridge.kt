package com.multiplatform.webview.web

/**
 * Creates JavaScript bridge code that can be used for communication between Kotlin and JavaScript
 * @param jsBridgeName Name of the JavaScript bridge object in browser window
 * @param isIife Whether to wrap the code in an Immediately Invoked Function Expression
 * @return JavaScript code for bridge implementation
 */
internal fun createJsBridgeScript(
    jsBridgeName: String,
    isIife: Boolean = false,
): String {
    val bridgeObjectCode =
        """
        window.$jsBridgeName = {
            _callbacks: {},
            _callbackId: 0,
            
            postMessage: function(methodName, params, callbackId) {
                // Send as JSON string instead of object to ensure proper parsing
                var messageData = JSON.stringify({
                    type: 'kmpJsBridge',
                    action: methodName,
                    params: params,
                    callbackId: callbackId || 0
                });
                parent.postMessage(messageData, '*');
            },
            
            onCallback: function(callbackId, message) {
                var callback = this._callbacks[callbackId];
                if (callback) {
                    callback(message);
                    delete this._callbacks[callbackId];
                }
            },
            
            call: function(action, params, callback) {
                var callbackId = 0;
                if (callback) {
                    callbackId = ++this._callbackId;
                    this._callbacks[callbackId] = callback;
                }
                this.postMessage(action, params, callbackId);
                return callbackId;
            },
            
            // Standard API as per documentation
            callNative: function(methodName, params, callback) {
                return this.call(methodName, params, callback);
            }
        };
        
        // Listen for callback messages from parent
        window.addEventListener('message', function(event) {
            try {
                var data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
                if (data && data.type === 'kmpJsBridgeCallback') {
                    window.$jsBridgeName.onCallback(data.callbackId, data.message);
                }
            } catch (e) {
                console.error('Error processing callback message:', e);
            }
        });
        """.trimIndent()

    return if (isIife) {
        """
        (function() {
            $bridgeObjectCode
        })();
        """.trimIndent()
    } else {
        bridgeObjectCode
    }
}

/**
 * Helper function to inject JS bridge into HTML content
 *
 * @param htmlContent HTML content to inject the bridge into
 * @param jsBridgeName Name of the bridge object in JavaScript
 * @return HTML content with bridge injected
 */
fun injectJsBridgeToHtml(
    htmlContent: String,
    jsBridgeName: String,
): String {
    // Only inject if it has a proper bridge implementation
    // We look for specific bridge functions like callNative in the content
    if (htmlContent.contains("window.$jsBridgeName") &&
        htmlContent.contains("$jsBridgeName.callNative") &&
        htmlContent.contains("$jsBridgeName._callbacks")
    ) {
        return htmlContent
    }

    // Create bridge initialization script wrapped in script tags
    val bridgeScriptContent = createJsBridgeScript(jsBridgeName)
    val bridgeScript =
        """
        <script>
        // Initialize JS bridge
        $bridgeScriptContent
        </script>
        """.trimIndent()

    // Insert script before end of head tag
    if (htmlContent.contains("</head>")) {
        return htmlContent.replace("</head>", "$bridgeScript</head>")
    }

    // If no head tag, insert after opening body tag
    if (htmlContent.contains("<body>") || htmlContent.contains("<body ")) {
        val bodyPattern = "<body[^>]*>".toRegex()
        return bodyPattern.replace(htmlContent, "$0$bridgeScript")
    }

    // As a last resort, prepend to the content
    return "$bridgeScript$htmlContent"
}
