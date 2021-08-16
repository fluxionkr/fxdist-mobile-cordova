package kr.fluxion.fxdist.data

data class Error(val code: Int, val message: String? = null)

interface ErrorCode {

    @Retention(AnnotationRetention.SOURCE)
    annotation class Common {
        companion object {
            const val APP_ROOTED = 1001
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    annotation class Auth {
        companion object {
            const val EMPTY_DATA = 2001
            const val FAILED = 2002
            const val IS_NOT_AUTO_LOGIN = 2003
        }
    }
}