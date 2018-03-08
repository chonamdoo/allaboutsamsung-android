package de.maxisma.allaboutsamsung

import android.os.Bundle
import android.support.annotation.StyleRes
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import androidx.net.toUri
import de.maxisma.allaboutsamsung.BuildConfig.LEGAL_NOTICE_URL
import de.maxisma.allaboutsamsung.settings.PreferenceHolder
import de.maxisma.allaboutsamsung.settings.newPreferencesActivityIntent
import javax.inject.Inject

abstract class BaseActivity(private val useDefaultMenu: Boolean = true) : AppCompatActivity() {

    @Inject
    lateinit var preferenceHolder: PreferenceHolder

    private var wasDarkThemeEnabled = false

    override fun onPause() {
        super.onPause()
        wasDarkThemeEnabled = preferenceHolder.useDarkTheme
    }

    override fun onResume() {
        super.onResume()
        if (wasDarkThemeEnabled != preferenceHolder.useDarkTheme) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        app.appComponent.inject(this)

        wasDarkThemeEnabled = preferenceHolder.useDarkTheme
        val darkThemeToUse = darkThemeToUse
        if (darkThemeToUse != null && preferenceHolder.useDarkTheme) {
            setTheme(darkThemeToUse)
        }

        super.onCreate(savedInstanceState)
    }

    @StyleRes
    protected open val darkThemeToUse: Int? = R.style.AppTheme_Dark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (useDefaultMenu) {
            menuInflater.inflate(R.menu.activity_base, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.preferences -> startActivity(newPreferencesActivityIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }
}