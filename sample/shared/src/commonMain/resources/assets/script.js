function callJS() {
    return 'Response from JS';
}

function callNative() {
    window.jsBridge.call('1', 'callNative', '{"name":"callNative"}');
}