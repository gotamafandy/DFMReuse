package com.adrena.dfm.common.view

interface FeatureModule {
    val name: String
    val displayName: String
}

object Modules {

    object ForgotPassword: FeatureModule {
        override val name = "forgotpassword"
        override val displayName = "Forgot password"
    }
}