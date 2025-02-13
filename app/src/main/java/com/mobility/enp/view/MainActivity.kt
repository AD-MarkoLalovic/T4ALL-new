package com.mobility.enp.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.mobility.enp.R
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        setListeners()
        setExistingLanguage(this)
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // Listener za promene destinacija
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d(TAG, "destination: $destination")
            when (destination.id) {
                R.id.homeFragment, R.id.paymentAndPassageFragment, R.id.toolHistoryFragment, R.id.profileFragment, R.id.supportDialog, R.id.notificationDialog, R.id.deactivateAccountDialog, R.id.pdfViewDialog -> {
                    // Ako je destinacija neki od ovih fragmenata, prikaži BottomNavigationView
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.toolbarShared.root.visibility = View.VISIBLE
                    binding.toolbarShared.backArrow.visibility = View.GONE
                    binding.toolbarShared.iconLogo.visibility = View.VISIBLE
                }

                R.id.basicInformationFragment, R.id.changePasswordFragment, R.id.invoicesFragment,
                R.id.myTagsFragment2, R.id.addTagFragment, R.id.refundRequestFragment2, R.id.tagPickerRequestFragment,
                R.id.settingsFragment, R.id.toolHistorySearchFragment, R.id.toolHistorySearchResultFragment,
                R.id.termsAndPrivacyFragment, R.id.cardFragment , R.id.noInternetConnectionDialog , R.id.loginNoInternetConnectionDialog-> {
                    // Ako je destinacija neki od ovih fragmenata, prikaži Toolbar i sakrij BottomNavigationView
                    binding.bottomNavigation.visibility = View.GONE
                    binding.toolbarShared.root.visibility = View.VISIBLE
                    binding.toolbarShared.backArrow.visibility = View.VISIBLE
                    binding.toolbarShared.iconLogo.visibility = View.GONE
                }

                else -> {
                    // Ako je destinacija neki drugi fragment, sakrij i Toolbar i BottomNavigationView
                    binding.bottomNavigation.visibility = View.GONE
                    binding.toolbarShared.root.visibility = View.GONE
                }
            }
        }

        // Povezujemo BottomNavigationView sa NavController-om
        binding.bottomNavigation.setupWithNavController(navController)
    }


    private fun setListeners() {
        onBackPressedDispatcher.addCallback(this) {
            if (navController.popBackStack()) {
                // backstack not clear
            } else {
                finish()
            }
        }

        binding.toolbarShared.backArrow.setOnClickListener {
            navController.popBackStack()
        }

        binding.toolbarShared.iconNotification.setOnClickListener { _ ->
            navController.navigate(R.id.action_global_notificationDialog)
        }
    }

    private fun setExistingLanguage(context: Context) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val database = DRoom.getRoomInstance(applicationContext)
                val userLanguageTable = database.languageDao()?.fetchAllowedUsers()

                userLanguageTable?.userLanguage?.let { languageKey ->
                    val locale = if (languageKey.isNotEmpty()) {
                        Locale(languageKey)
                    } else {
                        Locale.getDefault()
                    }

                    // Promena konfiguracije na glavnoj niti
                    withContext(Dispatchers.Main) {
                        val configuration = Configuration(context.resources.configuration)
                        configuration.setLocale(locale)

                        context.resources.updateConfiguration(
                            configuration, context.resources.displayMetrics
                        )
                    }
                }
            }
        }
    }

    companion object {  // class tied static method
        const val TAG = "FirebaseFcm"
        const val defCountryCode = "sr"

        fun setLocale(context: Context, languageKey: String) {

            val locale: Locale

            if (languageKey.isNotEmpty()) {
                locale = Locale(languageKey)
            } else {
                locale = Locale(defCountryCode)
            }

            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)

            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

            setStringTranslations(context)
        }

        private fun setStringTranslations(context: Context) {  // fixes stored room data translations
            CoroutineScope(Dispatchers.IO).launch {
                val database = DRoom.getRoomInstance(context)
                val promotions = database.promotionsDao().getPromotionsList()

                for (promotion in promotions) {
                    when (promotion.countryCode) {
                        "RS" -> {
                            promotion.title = context.getString(R.string.serbian_passage)
                            promotion.description =
                                context.getString(R.string.tag_device_payment_method_serbia)
                        }

                        "MK" -> {
                            promotion.title = context.getString(R.string.north_macedonian_passage)
                            promotion.description =
                                context.getString(R.string.tag_device_payment_method_north_macedonia)
                        }

                        "ME" -> {
                            promotion.title = context.getString(R.string.montenegro_passage)
                            promotion.description =
                                context.getString(R.string.tag_device_payment_method_montenegro)
                        }
                    }
                }

                database.promotionsDao().upsertPromotion(promotions)
            }
        }

        fun logoutOnInvalidToken(context: Context, navController: NavController) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = DRoom.getRoomInstance(context)
                database.loginDao().deleteAll()
            }
            CoroutineScope(Dispatchers.Main).launch {
                while (navController.popBackStack()) {
                }
                navController.navigate(R.id.action_global_loginFragment)
            }
        }

        fun showSnackMessage(message: String, binding: ActivityMainBinding) {
            Snackbar.make(binding.snackBarContainer, message, Snackbar.LENGTH_LONG)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }

    }

}