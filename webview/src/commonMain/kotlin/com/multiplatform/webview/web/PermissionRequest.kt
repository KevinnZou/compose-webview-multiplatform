package com.multiplatform.webview.web

/**
 * Representation of a permission request.
 *
 * @property origin the URl of the website requesting the permission
 * @property permissions a list of [Permissions][Permission] requested
 */
data class PermissionRequest(val origin: String, val permissions: List<Permission>) {
    /**
     * Different permission types.
     */
    enum class Permission {
        /**
         * Request for microphone access.
         */
        AUDIO,

        /**
         * Request for MIDI access (only on Android).
         */
        MIDI,

        /**
         * Request for media access (only on Android).
         */
        MEDIA,

        /**
         * Request for camera access.
         */
        VIDEO,

        /**
         * Request for location access (only on Android.
         */
        LOCATION
    }
}

/**
 * Possible answers to a permission request.
 */
enum class PermissionRequestResponse {
    /**
     * Grant the permission request.
     */
    GRANT,

    /**
     * Deny the permission request.
     */
    DENY
}

/**
 * A handler for [PermissionRequests][PermissionRequest].
 */
typealias PermissionHandler = (PermissionRequest) -> PermissionRequestResponse
