package kr.co.aiblab.ondago.api

interface OndagoAPI {

    interface Auth {
        companion object {
            const val LOGIN = "/apps/login"
        }
    }

    interface Location {
        companion object {
            const val SEND_INFO = "/apps/gps"
        }
    }
}