package me.aartikov.replica.simple_sample.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.commit
import me.aartikov.replica.simple_sample.R
import me.aartikov.replica.simple_sample.core.message.ui.MessagePopup
import me.aartikov.replica.simple_sample.features.pokemons.ui.PokemonsFragment
import org.koin.android.ext.android.get

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setupMessagePopup()
        if (savedInstanceState == null) {
            setupInitialFragment()
        }
    }

    private fun setupInitialFragment() {
        supportFragmentManager.commit {
            val fragment = PokemonsFragment()
            add(R.id.container, fragment)
            setPrimaryNavigationFragment(fragment)
        }
    }

    private fun setupMessagePopup() {
        get<MessagePopup>().setup(this)
    }
}