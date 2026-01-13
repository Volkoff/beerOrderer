package com.example.beerorderer

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.example.beerorderer.data.Currency
import com.example.beerorderer.databinding.ActivityMainBinding
import com.example.beerorderer.viewmodel.BeerViewModel

/**
 * Main Activity following MVVM architecture
 * Handles configuration changes automatically via ViewModel
 */
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: BeerViewModel by viewModels()
    private var currentCurrencyMenuItem: MenuItem? = null
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Ensure NavHostFragment exists and get its NavController
        var navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
        if (navHostFragment == null) {
            navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitNow()
        }
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener {
            // Navigate to SecondFragment to show orders
            navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        // Observe order count and update FAB badge
        viewModel.orderCount.observe(this) { count ->
            binding.fab.contentDescription = "View Orders ($count)"
            // Optional: You can add a badge to the FAB here
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        // Set initial currency menu item
        currentCurrencyMenuItem = menu.findItem(R.id.currency_usd)
        currentCurrencyMenuItem?.isChecked = true

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.currency_usd -> {
                changeCurrency(Currency.USD, item)
                true
            }
            R.id.currency_eur -> {
                changeCurrency(Currency.EUR, item)
                true
            }
            R.id.currency_czk -> {
                changeCurrency(Currency.CZK, item)
                true
            }
            R.id.action_settings -> {
                // Open SettingsFragment directly
                val fragment = SettingsFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment)
                    .addToBackStack(null)
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun changeCurrency(currency: Currency, menuItem: MenuItem) {
        viewModel.setCurrency(currency)
        currentCurrencyMenuItem?.isChecked = false
        menuItem.isChecked = true
        currentCurrencyMenuItem = menuItem

        Snackbar.make(
            binding.root,
            "Currency changed to ${currency.code}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}