package com.kevinnzou.sample.res

object HtmlRes {
    val html =
        """
        <html>
        <head>
            <title>Compose WebView Multiplatform</title>
            <style>
                body {
                    background-color: e0e8f0; 
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    flex-direction: column;
                    height: 100vh; 
                    margin: 0;
                }
                h1, h2 {
                    text-align: center; 
                    color: ffffff; 
                }
                @media (prefers-color-scheme: dark) {
                  body {
                    background-color: white;
                  }
                  h1, h2 {
                    color: black; 
                  }
                }
            </style>
        </head>
        <body>
            <script type="text/javascript">
                function callJS() {
                    return 'Response from JS';
                }
                function callDesktop() {
                    window.cefQuery({
                            request: "1_callDesktop_{\"message\":\"1\"}",
                            onSuccess: function(response) {
                                // 处理Java应用程序的响应
                            },
                            onFailure: function(errorCode, errorMessage) {
                                // 处理错误
                            }
                        });
                }
                function callNative() {
                    window.kmpJsBridge.callNative("Greet",JSON.stringify({message: "Hello"}),
                            function (data) {
                                document.getElementById("subtitle").innerText = data;
                                console.log("Greet from Native: " + data);
                            }
                        );
                }
            </script>
            <h1>Compose WebView Multiplatform</h1>
            <h2 id="subtitle">Basic Html Test</h2>
            <button onclick="callNative()">callNative</button>
        </body>
        </html>
        """.trimIndent()
}
