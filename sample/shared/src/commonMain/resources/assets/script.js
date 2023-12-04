function callJS() {
    return 'Response from JS';
}

function callAndroid() {
    window.jsBridge.call('1', 'callAndroid', '{"name":"callAndroid"}');
}

function callIOS() {
    window.webkit.messageHandlers.jsBridge.postMessage("{\"id\":\"1\",\"methodName\":\"callIOS\",\"params\":\"{\\\"type\\\":\\\"1\\\"}\"}");
}