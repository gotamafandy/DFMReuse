package com.adrena.dfm.partner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.adrena.dfm.common.view.Modules
import com.adrena.dfm.common.view.SplitActivity

class MainActivity : SplitActivity() {

    private lateinit var btnOpenForgotPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenForgotPassword = findViewById(R.id.forgot_password)

        btnOpenForgotPassword.setOnClickListener {
            loadModule(Modules.ForgotPassword) {
                val intent = Intent().setClassName(BuildConfig.APPLICATION_ID, "com.adrena.dfm.forgotpassword.ForgotPasswordActivity")

                startActivity(intent)
            }
        }
    }
}
