package de.maxisma.allaboutsamsung

import android.app.Activity
import android.app.Application
import android.content.Context
import android.support.v4.app.Fragment
import android.webkit.WebView
import de.maxisma.allaboutsamsung.utils.IOPool
import kotlinx.coroutines.experimental.launch

class App : Application() {

    val appComponent: AppComponent =
        DaggerAppComponent.builder().appModule(AppModule(this)).build()

    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        launch(IOPool) {
            appComponent.db.postDao.deleteOld()
        }
    }
}

val Context.app: App
    get() = applicationContext as App

val Activity.app: App
    get() = application as App

val Fragment.app: App
    get() = activity?.app ?: context!!.app