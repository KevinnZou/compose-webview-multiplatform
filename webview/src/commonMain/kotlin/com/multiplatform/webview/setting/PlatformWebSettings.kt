package com.multiplatform.webview.setting

/**
 * Created By Kevin Zou On 2023/9/20
 */
sealed class PlatformWebSettings {
    data class AndroidWebSettings(
        /**
         * Control whether algorithmic darkening is allowed.
         *
         * <p class="note">
         * <b>Note:</b> This API and the behaviour described only apply to apps with
         * {@code targetSdkVersion} &ge; {@link android.os.Build.VERSION_CODES#TIRAMISU}.
         *
         * <p>
         * WebView always sets the media query {@code prefers-color-scheme} according to the app's
         * theme attribute {@link android.R.styleable#Theme_isLightTheme isLightTheme}, i.e.
         * {@code prefers-color-scheme} is {@code light} if isLightTheme is true or not specified,
         * otherwise it is {@code dark}. This means that the web content's light or dark style will
         * be applied automatically to match the app's theme if the content supports it.
         *
         * <p>
         * Algorithmic darkening is disallowed by default.
         * <p>
         * If the app's theme is dark and it allows algorithmic darkening, WebView will attempt to
         * darken web content using an algorithm, if the content doesn't define its own dark styles
         * and doesn't explicitly disable darkening.
         *
         * <p>
         * If Android is applying Force Dark to WebView then WebView will ignore the value of
         * this setting and behave as if it were set to true.
         *
         * <p>
         * The deprecated {@link #setForceDark} and related API are no-ops in apps with
         * {@code targetSdkVersion} &ge; {@link android.os.Build.VERSION_CODES#TIRAMISU},
         * but they still apply to apps with
         * {@code targetSdkVersion} &lt; {@link android.os.Build.VERSION_CODES#TIRAMISU}.
         *
         * <p>
         * The below table summarizes how APIs work with different apps.
         *
         * <table border="2" width="85%" align="center" cellpadding="5">
         *     <thead>
         *         <tr>
         *             <th>App</th>
         *             <th>Web content which uses {@code prefers-color-scheme}</th>
         *             <th>Web content which does not use {@code prefers-color-scheme}</th>
         *         </tr>
         *     </thead>
         *     <tbody>
         *     <tr>
         *         <td>App with {@code isLightTheme} True or not set</td>
         *         <td>Renders with the light theme defined by the content author.</td>
         *         <td>Renders with the default styling defined by the content author.</td>
         *     </tr>
         *     <tr>
         *         <td>App with Android forceDark in effect</td>
         *         <td>Renders with the dark theme defined by the content author.</td>
         *         <td>Renders with the styling modified to dark colors by an algorithm
         *             if allowed by the content author.</td>
         *     </tr>
         *     <tr>
         *         <td>App with {@code isLightTheme} False,
         *            {@code targetSdkVersion} &lt; {@link android.os.Build.VERSION_CODES#TIRAMISU},
         *             and has {@code FORCE_DARK_AUTO}</td>
         *         <td>Renders with the dark theme defined by the content author.</td>
         *         <td>Renders with the default styling defined by the content author.</td>
         *     </tr>
         *     <tr>
         *         <td>App with {@code isLightTheme} False,
         *            {@code targetSdkVersion} &ge; {@link android.os.Build.VERSION_CODES#TIRAMISU},
         *             and {@code setAlgorithmicDarkening(false)}</td>
         *         <td>Renders with the dark theme defined by the content author.</td>
         *         <td>Renders with the default styling defined by the content author.</td>
         *     </tr>
         *     <tr>
         *         <td>App with {@code isLightTheme} False,
         *            {@code targetSdkVersion} &ge; {@link android.os.Build.VERSION_CODES#TIRAMISU},
         *             and {@code setAlgorithmicDarkening(true)}</td>
         *         <td>Renders with the dark theme defined by the content author.</td>
         *         <td>Renders with the styling modified to dark colors by an algorithm if allowed
         *             by the content author.</td>
         *     </tr>
         *     </tbody>
         * </table>
         * </p>
         *
         */
        var isAlgorithmicDarkeningAllowed: Boolean = false,
        /**
         * whether Safe Browsing is enabled. Safe Browsing allows WebView to
         * protect against malware and phishing attacks by verifying the links.
         */
        var safeBrowsingEnabled: Boolean = false
    ): PlatformWebSettings()

    data object DesktopWebSettings: PlatformWebSettings()

    data object IOSWebSettings: PlatformWebSettings()
}