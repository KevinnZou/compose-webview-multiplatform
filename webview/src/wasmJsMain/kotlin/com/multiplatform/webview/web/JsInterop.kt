package com.multiplatform.webview.web

import org.w3c.dom.Element

/**
 * Check if navigation back is possible
 */
@JsFun(
    "(element) => { try { return element.contentWindow && element.contentWindow.history && " +
        "element.contentWindow.history.length > 1; } catch(e) { return false; } }",
)
external fun checkCanGoBackJs(element: Element): Boolean

/**
 * Check if navigation forward is possible (always returns false for iframes)
 */
@JsFun("(element) => { try { return false; } catch(e) { return false; } }")
external fun checkCanGoForwardJs(element: Element): Boolean

/**
 * Navigate back in browser history
 */
@JsFun(
    "(element) => { try { if (element.contentWindow && element.contentWindow.history) { " +
        "element.contentWindow.history.back(); } } catch(e) { console.error('Error going back:', e);" +
        " } }",
)
external fun navigateBackJs(element: Element)

/**
 * Navigate forward in browser history
 */
@JsFun(
    "(element) => { try { if (element.contentWindow && element.contentWindow.history) { " +
        "element.contentWindow.history.forward(); } } catch(e) { " +
        "console.error('Error going forward:', e); } }",
)
external fun navigateForwardJs(element: Element)

/**
 * Reload the current page
 */
@JsFun(
    "(element) => { try { if (element.contentWindow && element.contentWindow.location) { " +
        "element.contentWindow.location.reload(); } } catch(e) { " +
        "console.error('Error reloading:', e); } }",
)
external fun reloadJs(element: Element)

/**
 * Stop loading the current page
 */
@JsFun(
    "(element) => { try { if (element.contentWindow && element.contentWindow.stop) {" +
        " element.contentWindow.stop(); } } catch(e) { " +
        "console.error('Error stopping load:', e); } }",
)
external fun stopLoadingJs(element: Element)

/**
 * Set the URL of an iframe
 */
@JsFun(
    "(element, url) => { try { element.src = url; } catch(e) { " +
        "console.error('Error setting URL:', e); } }",
)
external fun setUrlJs(
    element: Element,
    url: String,
)

/**
 * Set HTML content of an iframe via srcdoc attribute
 */
@JsFun(
    "(element, content) => { try { element.srcdoc = content; } catch(e) { " +
        "console.error('Error setting HTML:', e); } }",
)
external fun setHtmlContentJs(
    element: Element,
    content: String,
)

/**
 * Evaluate JavaScript in the iframe context
 */
@JsFun(
    "(element, script) => { try { return element.contentWindow && element.contentWindow.eval ?" +
        " String(element.contentWindow.eval(script)) : ''; } catch(err) { " +
        "return 'Error: ' + err.message; } }",
)
external fun evaluateScriptJs(
    element: Element,
    script: String,
): String

/**
 * Get the title from an iframe document
 */
@JsFun(
    "(iframe) => { try { return iframe.contentDocument ? iframe.contentDocument.title : null; }" +
        " catch(e) { return null; } }",
)
external fun getIframeTitleJs(iframe: Element): String?

/**
 * Get the current URL from an iframe
 */
@JsFun(
    "(iframe) => { try { return iframe.contentWindow && iframe.contentWindow.location ? " +
        "iframe.contentWindow.location.href : null; } catch(e) { return iframe.src || null; } }",
)
external fun getIframeUrlJs(iframe: Element): String?

/**
 * Set a style property on an element
 */
@JsFun("(element, property, value) => { element.style[property] = value; }")
external fun setStyleJs(
    element: Element,
    property: String,
    value: String,
)

/**
 * Add a content identifier to an iframe for history tracking
 */
@JsFun(
"""
(iframe) => { 
  try {
    if (iframe.contentWindow) {
      const uniqueId = Math.random().toString(36).substring(2, 15);
      iframe.contentWindow.history.replaceState({id: uniqueId}, '',
       iframe.contentWindow.location.href);
    }
  } catch(e) { 
    console.error("Error adding content identifier:", e); 
  }
}
""",
)
external fun addContentIdentifierJs(iframe: Element)

/**
 * Log a message to the console
 */
@JsFun("(message) => { console.log(message); }")
external fun consoleLogJs(message: String)

/**
 * Log an info message to the console
 */
@JsFun("(message) => { console.info(message); }")
external fun consoleInfoJs(message: String)

/**
 * Log an error message to the console
 */
@JsFun("(message) => { console.error(message); }")
external fun consoleErrorJs(message: String)

/**
 * Position an element with coordinates
 */
@JsFun(
    """(element, width, height, x, y) => {
  element.style.width = width + 'px';
  element.style.height = height + 'px';
  element.style.left = x + 'px';
  element.style.top = y + 'px';
}""",
)
external fun changeCoordinates(
    element: Element,
    width: Float,
    height: Float,
    x: Float,
    y: Float,
)

/**
 * Initialize an element with basic styling
 */
@JsFun(
    """(element) => {
  element.style.position = 'absolute';
  element.style.margin = '0px';
}""",
)
external fun initializingElement(element: Element)

/**
 * Request focus on an element
 */
@JsFun("(element) => { element.focus(); }")
external fun requestFocus(element: Element)
