function callJS() {
    return 'Response from JS';
}

function callAndroid() {
    window.jsBridge.call('1', 'callAndroid', '{"name":"callAndroid"}');
}

function callIOS() {
    window.webkit.messageHandlers.jsBridge.postMessage("{\"id\":\"1\",\"methodName\":\"callIOS\",\"params\":\"{\\\"type\\\":\\\"1\\\"}\"}");
}

function callDesktop() {
    window.cefQuery({
            request: "{\"id\":\"1\",\"methodName\":\"callIOS\",\"params\":\"{\\\"type\\\":\\\"1\\\"}\"}",
            onSuccess: function(response) {
                // 处理Java应用程序的响应
            },
            onFailure: function(errorCode, errorMessage) {
                // 处理错误
            }
        });
}