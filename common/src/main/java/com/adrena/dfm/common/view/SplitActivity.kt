package com.adrena.dfm.common.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.adrena.dfm.common.R
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

abstract class SplitActivity : AppCompatActivity(), SplitInstallStateUpdatedListener {

    private var mOnSuccess: ((String) -> Unit)? = null

    private val mDisplayName = HashMap<String, String>()

    private val mInstallDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(this)

        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        builder.setPositiveButton(R.string.try_again) { _, _ -> }

        builder.create()
    }

    private val mSplitInstallManager: SplitInstallManager by lazy {
        SplitInstallManagerFactory.create(this)
    }

    fun loadModule(module: FeatureModule, func: (module: String) -> Unit) {
        mOnSuccess = func

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.KITKAT) {
            mOnSuccess?.invoke(module.name)

            return
        } else if (mSplitInstallManager.installedModules.contains(module.name)) {
            mOnSuccess?.invoke(module.name)

            return
        }

        mDisplayName[module.name] = module.displayName

        installModule(module.name, module.displayName)
    }

    override fun onStateUpdate(state: SplitInstallSessionState?) {
        val sessionId = state?.sessionId()

        mInstallDialog.getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.VISIBLE
        mInstallDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            sessionId?.let {
                mSplitInstallManager.cancelInstall(it)
            }
        }

        when (state?.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                mInstallDialog.setMessage(getString(R.string.downloading_module))
            }
            SplitInstallSessionStatus.INSTALLING -> {
                mInstallDialog.setMessage(getString(R.string.installing_module))
            }
            SplitInstallSessionStatus.INSTALLED -> {
                val name = state.moduleNames()[0]

                mInstallDialog.dismiss()

                SplitCompat.install(this)

                mDisplayName.remove(name)

                mOnSuccess?.invoke(name)
            }
            SplitInstallSessionStatus.FAILED -> {
                mInstallDialog.setMessage(getString(R.string.module_installation_failed))

                mInstallDialog.getButton(DialogInterface.BUTTON_POSITIVE).visibility = View.VISIBLE
                mInstallDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val name = state.moduleNames()[0]

                    installModule(state.moduleNames()[0], mDisplayName[name] ?: name)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mSplitInstallManager.registerListener(this)
    }

    override fun onPause() {
        mSplitInstallManager.unregisterListener(this)

        super.onPause()
    }

    private fun installModule(name: String, displayName: String) {

        val request = SplitInstallRequest
            .newBuilder()
            .addModule(name)
            .build()

        if (mInstallDialog.isShowing) {
            mInstallDialog.dismiss()
        }

        mSplitInstallManager.startInstall(request)

        mInstallDialog.setTitle(displayName)
        mInstallDialog.setMessage(getString(R.string.preparing))
        mInstallDialog.setCancelable(false)

        mInstallDialog.show()

        mInstallDialog.getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
        mInstallDialog.getButton(DialogInterface.BUTTON_POSITIVE).visibility = View.GONE
    }
}