package com.multiplatform.webview.web

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.EnumProgress
import me.friwi.jcefmaven.IProgressHandler
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler
import me.friwi.jcefmaven.impl.step.check.CefInstallationChecker
import org.cef.CefApp
import org.cef.CefClient
import org.cef.CefSettings
import java.io.File

data object Cef {

    private val state: MutableStateFlow<State> = MutableStateFlow(State.New)
    private var initError: Throwable? = null
    private var cefAppInstance: CefApp? = null

    private val progressHandlers = mutableSetOf<IProgressHandler>(ConsoleProgressHandler())
    private var progressState = EnumProgress.LOCATING
    private var progressValue = EnumProgress.NO_ESTIMATION

    private val cefApp: CefApp
        get() = checkNotNull(cefAppInstance) {
            CefException.NotInitialized
        }

    suspend fun init(
        builder: Builder,
        initProgress: InitProgress? = null,
        onError: (Throwable) -> Unit = { },
        onRestartRequired: () -> Unit = { },
    ) = init(
        appBuilder = builder.build(),
        installDir = builder.installDir,
        initProgress = initProgress,
        onError = onError,
        onRestartRequired = onRestartRequired
    )

    suspend fun init(
        builder: Builder.() -> Unit,
        initProgress: InitProgress.Builder.() -> Unit,
        onError: (Throwable) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) = init(
        builder = Builder().apply(builder),
        initProgress = InitProgress.Builder().apply(initProgress).build(),
        onError = onError,
        onRestartRequired = onRestartRequired,
    )

    suspend fun init(
        appBuilder: CefAppBuilder,
        installDir: File,
        initProgress: InitProgress? = null,
        onError: (Throwable) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) {
        val builder = getInitBuilder(appBuilder, initProgress) ?: return
        val isInstallOk = CefInstallationChecker.checkInstallation(installDir)

        if (isInstallOk) {
            val result = runCatching {
                builder.build()
            }
            setInitResult(result)
            result.exceptionOrNull()?.let(onError)
        } else {
            try {
                builder.install()
            } catch (error: Throwable) {
                setInitResult(Result.failure(error))
                onError.invoke(error)
            }

            val result = runCatching {
                builder.build()
            }

            setInitResult(result)
            if (result.isFailure) {
                setInitResult(Result.failure(CefException.ApplicationRestartRequired))
                onRestartRequired.invoke()
            }
        }
    }

    suspend fun newClient(onProgress: IProgressHandler? = null): CefClient {
        return when (state.value) {
            State.New -> throw CefException.NotInitialized
            State.Disposed -> throw CefException.Disposed
            State.Error -> throw CefException.Error(initError)
            State.Initialized -> cefApp.createClient()
            State.Initializing -> {
                val added = onProgress?.let { handler ->
                    handler.handleProgress(progressState, progressValue)
                    progressHandlers.add(handler)
                }

                state.first { it != State.Initializing }

                if (added == true) {
                    progressHandlers.remove(onProgress)
                }

                return newClient(onProgress)
            }
        }
    }

    fun dispose() {
        when (state.value) {
            State.New, State.Disposed, State.Error -> return
            State.Initializing -> {
                runBlocking {
                    state.first { it != State.Initializing }
                }

                return dispose()
            }

            State.Initialized -> {
                state.value = State.Disposed
                cefAppInstance?.dispose()
                cefAppInstance = null
            }
        }
    }

    private fun getInitBuilder(
        builder: CefAppBuilder,
        initProgress: InitProgress?
    ): CefAppBuilder? {
        val currentState = state.value

        when (currentState) {
            State.Disposed -> throw CefException.Disposed
            State.Initializing, State.Initialized -> return null
            State.New, State.Error -> state.value = State.Initializing
        }

        if (currentState == State.Error) {
            initError = null
        }

        return builder.apply {
            setProgressHandler { state, percent ->
                dispatchProgress(state, percent)

                when (state) {
                    null -> {
                        /** Could be null in Java, just for safety reasons */
                    }

                    EnumProgress.LOCATING -> initProgress?.locating()
                    EnumProgress.DOWNLOADING -> initProgress?.downloading(percent)
                    EnumProgress.EXTRACTING -> initProgress?.extracting()
                    EnumProgress.INSTALL -> initProgress?.install()
                    EnumProgress.INITIALIZING -> initProgress?.initializing()
                    EnumProgress.INITIALIZED -> initProgress?.initialized()
                    else -> {
                        /** Just for safety reasons */
                    }
                }
            }
        }
    }

    private fun setInitResult(result: Result<CefApp>) {
        val nextState = if (result.isSuccess) {
            cefAppInstance = result.getOrThrow()
            State.Initialized
        } else {
            initError = result.exceptionOrNull()
            State.Error
        }

        check(state.compareAndSet(State.Initializing, nextState)) {
            "State.Initializing was expected."
        }
    }

    private fun dispatchProgress(state: EnumProgress, value: Float) {
        progressState = state
        progressValue = value

        progressHandlers.forEach { handler ->
            handler.handleProgress(state, value)
        }
    }

    private sealed interface State {
        data object New : State
        data object Initializing : State
        data object Initialized : State
        data object Error : State
        data object Disposed : State
    }

    class Builder {
        private var _mirrors: Array<out String> = arrayOf()
        private var _args: Array<out String> = arrayOf()
        private var _settings: Settings = Settings()

        /**
         * If installation skipping is enabled, no checks against the installation directory will be performed and the download,
         * installation and verification of the jcef natives has to be performed by the individual developer.
         */
        var skipInstallation: Boolean = false

        /**
         * Sets the install directory to use. Defaults to "./jcef-bundle".
         */
        var installDir: File = File("jcef-bundle")

        /**
         * Attach your own adapter to handle certain events in CEF.
         */
        var appHandler: MavenCefAppHandlerAdapter? = null

        /**
         * Set mirror urls that should be used when downloading jcef. First element will be attempted first.
         * Mirror urls can contain placeholders that are replaced when a fetch is attempted:
         *
         * {mvn_version}: The version of jcefmaven (e.g. 100.0.14.3) <br/>
         * {platform}: The desired platform for the download (e.g. linux-amd64) <br/>
         * {tag}: The desired version tag for the download (e.g. jcef-08efede+cef-100.0.14+g4e5ba66+chromium-100.0.4896.75)
         */
        fun mirrors(vararg mirror: String) = apply {
            _mirrors = mirror
        }

        /**
         * Add one or multiple arguments to pass to the JCef library.
         * Arguments may contain spaces.
         *
         * Due to installation using maven some arguments may be overwritten
         * again depending on your platform. Make sure to not specify arguments
         * that break the installation process (e.g. subprocess path, resources path...)!
         *
         * @param args the arguments to add
         */
        fun args(vararg args: String) = apply {
            _args = args
        }

        /**
         * Set [Settings] to change
         * configuration parameters.
         *
         * Due to installation using maven some settings may be overwritten
         * again depending on your platform.
         */
        fun settings(settings: Settings) = apply {
            _settings = settings
        }

        /**
         * Retrieve the embedded [Settings] instance to change
         * configuration parameters.
         *
         * Due to installation using maven some settings may be overwritten
         * again depending on your platform.
         */
        fun settings(builder: Settings.() -> Unit) = apply {
            settings(_settings.apply(builder))
        }

        fun build(): CefAppBuilder {
            return CefAppBuilder().apply {
                if (this@Builder._mirrors.isNotEmpty()) {
                    this.mirrors = this@Builder._mirrors.toList()
                }

                if (this@Builder._args.isNotEmpty()) {
                    this.addJcefArgs(*this@Builder._args)
                }

                this.setAppHandler(this@Builder.appHandler)
                this.skipInstallation = this@Builder.skipInstallation
                this.setInstallDir(this@Builder.installDir)

                this.cefSettings.apply {
                    this.cache_path = _settings.cachePath
                    this.browser_subprocess_path = _settings.browserSubProcessPath
                    this.command_line_args_disabled = _settings.commandLineArgsDisabled
                    this.cookieable_schemes_exclude_defaults =
                        _settings.cookieableSchemesExcludeDefaults
                    this.cookieable_schemes_list = _settings.cookieableSchemesList
                    this.javascript_flags = _settings.javascriptFlags
                    this.locale = _settings.locale
                    this.locales_dir_path = _settings.localesDirPath
                    this.log_file = _settings.logFile
                    this.log_severity = _settings.logSeverity.toJCefSeverity()
                    this.pack_loading_disabled = _settings.packLoadingDisabled
                    this.persist_session_cookies = _settings.persistSessionCookies
                    this.remote_debugging_port = _settings.remoteDebuggingPort
                    this.resources_dir_path = _settings.resourcesDirPath
                    this.uncaught_exception_stack_size = _settings.uncaughtExceptionStackSize
                    this.user_agent = _settings.userAgent
                    this.user_agent_product = _settings.userAgentProduct
                    this.windowless_rendering_enabled = _settings.windowlessRenderingEnabled
                }
            }
        }
    }

    data class Settings(
        /**
         * The location where cache data will be stored on disk. If empty an in-memory
         * cache will be used for some features and a temporary disk cache for others.
         * HTML5 databases such as localStorage will only persist across sessions if a
         * cache path is specified.
         */
        var cachePath: String? = null,

        /**
         * The path to a separate executable that will be launched for sub-processes.
         * By default the browser process executable is used. See the comments on
         * CefExecuteProcess() for details. Also configurable using the
         * "browser-subprocess-path" command-line switch.
         */
        var browserSubProcessPath: String? = null,

        /**
         * Set to true to disable configuration of browser process features using
         * standard CEF and Chromium command-line arguments. Configuration can still
         * be specified using CEF data structures or via the
         * CefApp::OnBeforeCommandLineProcessing() method.
         */
        var commandLineArgsDisabled: Boolean = false,
        var cookieableSchemesExcludeDefaults: Boolean = false,
        var cookieableSchemesList: String? = null,

        /**
         * Custom flags that will be used when initializing the V8 JavaScript engine.
         * The consequences of using custom flags may not be well tested. Also
         * configurable using the "js-flags" command-line switch.
         */
        var javascriptFlags: String? = null,

        /**
         * The locale string that will be passed to Blink. If empty the default
         * locale of "en-US" will be used. This value is ignored on Linux where locale
         * is determined using environment variable parsing with the precedence order:
         * LANGUAGE, LC_ALL, LC_MESSAGES and LANG. Also configurable using the "lang"
         * command-line switch.
         */
        var locale: String? = null,

        /**
         * The fully qualified path for the locales directory. If this value is empty
         * the locales directory must be located in the module directory. This value
         * is ignored on Mac OS X where pack files are always loaded from the app
         * bundle Resources directory. Also configurable using the "locales-dir-path"
         * command-line switch.
         */
        var localesDirPath: String? = null,

        /**
         * The directory and file name to use for the debug log. If empty, the
         * default name of "debug.log" will be used and the file will be written
         * to the application directory. Also configurable using the "log-file"
         * command-line switch.
         */
        var logFile: String? = null,

        /**
         * The log severity. Only messages of this severity level or higher will be
         * logged. Also configurable using the "log-severity" command-line switch with
         * a value of "verbose", "info", "warning", "error", "error-report" or
         * "disable".
         */
        var logSeverity: LogSeverity = LogSeverity.Default,

        /**
         * Set to true to disable loading of pack files for resources and locales.
         * A resource bundle handler must be provided for the browser and render
         * processes via CefApp::GetResourceBundleHandler() if loading of pack files
         * is disabled. Also configurable using the "disable-pack-loading" command-
         * line switch.
         */
        var packLoadingDisabled: Boolean = false,

        /**
         * To persist session cookies (cookies without an expiry date or validity
         * interval) by default when using the global cookie manager set this value to
         * true. Session cookies are generally intended to be transient and most Web
         * browsers do not persist them. A |cache_path| value must also be specified to
         * enable this feature. Also configurable using the "persist-session-cookies"
         * command-line switch.
         */
        var persistSessionCookies: Boolean = false,

        /**
         * Set to a value between 1024 and 65535 to enable remote debugging on the
         * specified port. For example, if 8080 is specified the remote debugging URL
         * will be http: *localhost:8080. CEF can be remotely debugged from any CEF or
         * Chrome browser window. Also configurable using the "remote-debugging-port"
         * command-line switch.
         */
        var remoteDebuggingPort: Int = 0,

        /**
         * The fully qualified path for the resources directory. If this value is
         * empty the cef.pak and/or devtools_resources.pak files must be located in
         * the module directory on Windows/Linux or the app bundle Resources directory
         * on Mac OS X. Also configurable using the "resources-dir-path" command-line
         * switch.
         */
        var resourcesDirPath: String? = null,

        /**
         * The number of stack trace frames to capture for uncaught exceptions.
         * Specify a positive value to enable the CefV8ContextHandler::
         * OnUncaughtException() callback. Specify 0 (default value) and
         * OnUncaughtException() will not be called. Also configurable using the
         * "uncaught-exception-stack-size" command-line switch.
         */
        var uncaughtExceptionStackSize: Int = 0,

        /**
         * Value that will be returned as the User-Agent HTTP header. If empty the
         * default User-Agent string will be used. Also configurable using the
         * "user-agent" command-line switch.
         */
        var userAgent: String? = null,

        /**
         * Value that will be inserted as the product portion of the default
         * User-Agent string. If empty the Chromium product version will be used. If
         * |userAgent| is specified this value will be ignored. Also configurable
         * using the "user_agent_product" command-line switch.
         */
        var userAgentProduct: String? = null,

        /**
         * Set to true to enable windowless (off-screen) rendering support. Do not
         * enable this value if the application does not use windowless rendering as
         * it may reduce rendering performance on some systems.
         */
        var windowlessRenderingEnabled: Boolean = false
    ) {

        /**
         * The log severity. Only messages of this severity level or higher will be
         * logged. Also configurable using the "log-severity" command-line switch with
         * a value of "verbose", "info", "warning", "error", "error-report" or
         * "disable".
         */
        fun logSeverity(severity: CefSettings.LogSeverity) = apply {
            logSeverity = LogSeverity.fromJCefSeverity(severity)
        }

        /**
         * Log severity levels.
         *
         * This way we don't need to expose the jcef module.
         */
        sealed interface LogSeverity {

            /**
             * Default logging (currently INFO logging).
             */
            data object Default : LogSeverity

            /**
             * Verbose logging.
             */
            data object Verbose : LogSeverity

            /**
             * INFO logging.
             */
            data object Info : LogSeverity

            /**
             * WARNING logging.
             */
            data object Warning : LogSeverity

            /**
             * ERROR logging.
             */
            data object Error : LogSeverity

            /**
             * FATAL logging.
             */
            data object Fatal : LogSeverity

            /**
             * Completely disable logging.
             */
            data object Disable : LogSeverity

            fun toJCefSeverity(): CefSettings.LogSeverity = when (this) {
                Default -> CefSettings.LogSeverity.LOGSEVERITY_DEFAULT
                Verbose -> CefSettings.LogSeverity.LOGSEVERITY_VERBOSE
                Info -> CefSettings.LogSeverity.LOGSEVERITY_INFO
                Warning -> CefSettings.LogSeverity.LOGSEVERITY_WARNING
                Error -> CefSettings.LogSeverity.LOGSEVERITY_ERROR
                Fatal -> CefSettings.LogSeverity.LOGSEVERITY_FATAL
                Disable -> CefSettings.LogSeverity.LOGSEVERITY_DISABLE
                else -> CefSettings.LogSeverity.LOGSEVERITY_DEFAULT
            }

            companion object {
                fun fromJCefSeverity(severity: CefSettings.LogSeverity): LogSeverity =
                    when (severity) {
                        CefSettings.LogSeverity.LOGSEVERITY_DEFAULT -> Default
                        CefSettings.LogSeverity.LOGSEVERITY_VERBOSE -> Verbose
                        CefSettings.LogSeverity.LOGSEVERITY_INFO -> Info
                        CefSettings.LogSeverity.LOGSEVERITY_WARNING -> Warning
                        CefSettings.LogSeverity.LOGSEVERITY_ERROR -> Error
                        CefSettings.LogSeverity.LOGSEVERITY_FATAL -> Fatal
                        CefSettings.LogSeverity.LOGSEVERITY_DISABLE -> Disable
                        else -> Default
                    }
            }
        }
    }
}