package com.gyleedev.build_logic

class NativeLib {

    /**
     * A native method that is implemented by the 'build_logic' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'build_logic' library on application startup.
        init {
            System.loadLibrary("build_logic")
        }
    }
}