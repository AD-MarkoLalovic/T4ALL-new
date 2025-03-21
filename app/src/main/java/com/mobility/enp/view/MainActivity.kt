package com.mobility.enp.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.mobility.enp.R
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.databinding.ActivityMainBinding
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val franchiseViewModel: FranchiseViewModel by viewModels { FranchiseViewModel.Factory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        setListeners()
        setObservers()
        setExistingLanguage(this)
    }

    private fun setObservers() {
        franchiseViewModel.franchiseModel.observe(this) { franchiseModel ->
            franchiseModel?.let {
                setFranchiserLogoVisible(it)
            }
        }
    }

    fun setDefaultLogo() {
        binding.toolbarShared.iconLogo.setImageResource(R.drawable.ic_logo_home_screen)
        binding.toolbarShared.franchiserFlavorText.text = ""
        binding.toolbarShared.constraintBlock.setBackgroundColor(
            ContextCompat.getColor(
                this,
                android.R.color.white
            )
        )
//        binding.toolbarShared.root.visibility = View.GONE
    }

    fun hideLogo(hideLogo: Boolean) {
        if (hideLogo) {
            binding.toolbarShared.iconLogo.visibility = View.INVISIBLE
            binding.bottomNavigation.visibility = View.GONE
        } else {
            binding.toolbarShared.iconLogo.visibility = View.VISIBLE
            binding.bottomNavigation.visibility = View.VISIBLE
        }
    }

    private fun setFranchiserLogoVisible(franchiseModel: FranchiseModel?) {
        franchiseModel?.let { data ->
            binding.toolbarShared.franchiserFlavorText.visibility = View.VISIBLE
            binding.toolbarShared.franchiserFlavorText.text = franchiseModel.franchiseFlavorText
            binding.toolbarShared.iconLogo.setImageDrawable(data.franchiseLogoToolbar)

            binding.toolbarShared.backArrow.setImageResource(franchiseModel.backButtonResource)

            if (data.enableBackgroundColorOnToolBar) {
                binding.toolbarShared.constraintBlock.setBackgroundColor(data.franchisePrimaryColor)
            } else {
                binding.toolbarShared.constraintBlock.setBackgroundColor(getColor(R.color.white))
            }

            binding.bottomNavigation.itemIconTintList = franchiseModel.navHomeDrawable
        }
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
                R.id.termsAndPrivacyFragment, R.id.cardFragment, R.id.noInternetConnectionDialog, R.id.loginNoInternetConnectionDialog -> {
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

        // Dodavanje provere da se ne reaguje na klik ako je destinacija već aktivna
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (navController.currentDestination?.id == item.itemId) {
                return@setOnItemSelectedListener false // Ne reaguj ako je trenutna destinacija ista
            }
            navController.navigate(item.itemId)
            true
        }
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
        val sharedPreferences = context.getSharedPreferences("AppLanguage", Context.MODE_PRIVATE)
        val userLanguage = sharedPreferences.getString("user_language", "sr") ?: "sr"

        val lang = when (userLanguage) {
            "cyr" -> Locale("sr_Cyrl", "RS")
            "sr", "cnr" -> Locale("sr", "RS")
            else -> Locale(userLanguage)
        }

        Locale.setDefault(lang)

        val configuration = resources.configuration
        configuration.setLocale(lang)

        context.resources.updateConfiguration(
            configuration,
            context.resources.displayMetrics
        )

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