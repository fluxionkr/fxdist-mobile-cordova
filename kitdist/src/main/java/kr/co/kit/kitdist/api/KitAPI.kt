package kr.co.kit.kitdist.api

interface KitAPI {

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