package com.multiplatform.webview.web

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.internal.synchronized
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

suspend fun test() {
    Cef.init(builder = {
        settings {

        }
    })
}

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
        onError: (Throwable) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) = init(
        appBuilder = builder.build(),
        installDir = builder.installDir,
        onError = onError,
        onRestartRequired = onRestartRequired
    )

    suspend fun init(
        builder: Builder.() -> Unit,
        onError: (Throwable) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) = init(
        builder = Builder().apply(builder),
        onError = onError,
        onRestartRequired = onRestartRequired
    )

    suspend fun init(
        appBuilder: CefAppBuilder,
        installDir: File,
        onError: (Throwable) -> Unit = { },
        onRestartRequired: () -> Unit = { }
    ) {
        val builder = getInitBuilder(appBuilder) ?: return
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

            setInitResult(Result.failure(CefException.ApplicationRestartRequired))
            onRestartRequired.invoke()
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

    private fun getInitBuilder(builder: CefAppBuilder): CefAppBuilder? {
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
            setProgressHandler(::dispatchProgress)
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

        var skipInstallation: Boolean = false
        lateinit var installDir: File
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

        fun args(vararg args: String) = apply {
            _args = args
        }

        fun settings(settings: Settings) = apply {
            _settings = settings
        }

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
                    this.cookieable_schemes_exclude_defaults = _settings.cookieableSchemesExcludeDefaults
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
        var cachePath: String? = null,
        var browserSubProcessPath: String? = null,
        var commandLineArgsDisabled: Boolean = false,
        var cookieableSchemesExcludeDefaults: Boolean = false,
        var cookieableSchemesList: String? = null,
        var javascriptFlags: String? = null,
        var locale: String? = null,
        var localesDirPath: String? = null,
        var logFile: String? = null,
        var logSeverity: LogSeverity = LogSeverity.Default,
        var packLoadingDisabled: Boolean = false,
        var persistSessionCookies: Boolean = false,
        var remoteDebuggingPort: Int = 0,
        var resourcesDirPath: String? = null,
        var uncaughtExceptionStackSize: Int = 0,
        var userAgent: String? = null,
        var userAgentProduct: String? = null,
        var windowlessRenderingEnabled: Boolean = false
    ) {

        fun logSeverity(severity: CefSettings.LogSeverity) = apply {
            logSeverity = LogSeverity.fromJCefSeverity(severity)
        }

        /**
         * This way we don't need to expose the jcef module
         */
        sealed interface LogSeverity {
            data object Default : LogSeverity
            data object Verbose : LogSeverity
            data object Info : LogSeverity
            data object Warning : LogSeverity
            data object Error : LogSeverity
            data object Fatal : LogSeverity
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
                fun fromJCefSeverity(severity: CefSettings.LogSeverity): LogSeverity = when (severity) {
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