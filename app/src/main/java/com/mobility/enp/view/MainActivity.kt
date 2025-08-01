package com.mobility.enp.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.mobility.enp.R
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.data.room.database.DRoom
import com.mobility.enp.databinding.ActivityMainBinding
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.Util
import com.mobility.enp.viewmodel.FranchiseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val franchiseViewModel: FranchiseViewModel by viewModels { FranchiseViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavigationBarAppearance(
            backgroundColor = ContextCompat.getColor(this, R.color.white)
        )

        setupNavigation()
        setListeners()
        setObservers()
        messageLanguageChanged(this)
        applyDisplayCutouts()

    }

    private fun applyDisplayCutouts() {
        val rootView = binding.rootLayout

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setNavigationBarAppearance(@ColorInt backgroundColor: Int) {
        @Suppress("DEPRECATION")
        window.navigationBarColor = backgroundColor

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = true
    }

    private fun setObservers() {
        franchiseViewModel.franchiseModel.observe(this) { franchiseModel ->
            franchiseModel?.let {
                setFranchiserLogoVisible(it)
            }
        }
    }

    fun logoFix(portalKey: String){
        val franchiseModel = Util.franchiseID(portalKey, this)
        franchiseModel?.franchiseLogoToolbar?.let {
            binding.toolbarShared.iconLogo.setImageDrawable(it)
        }
    }

    fun settingsFragmentReset() {
        binding.bottomNavigation.visibility = View.GONE
        binding.toolbarShared.root.visibility = View.VISIBLE
        binding.toolbarShared.backArrow.visibility = View.VISIBLE
        binding.toolbarShared.iconLogo.visibility = View.GONE
        binding.toolbarShared.franchiserFlavorText.visibility = View.INVISIBLE
    }

    fun resetToDefault() {
        binding.toolbarShared.iconLogo.setImageResource(R.drawable.null_svg)
        binding.toolbarShared.franchiserFlavorText.text = ""
        binding.toolbarShared.constraintBlock.setBackgroundColor(
            ContextCompat.getColor(
                this,
                android.R.color.white
            )
        )
        binding.toolbarShared.root.visibility = View.VISIBLE
        binding.bottomNavigation.itemIconTintList =
            ContextCompat.getColorStateList(this, R.color.bottom_nav_color_default)
    }

    private fun setFranchiserLogoVisible(franchiseModel: FranchiseModel?) {
        franchiseModel?.let { data ->
            binding.bottomNavigation.visibility = View.INVISIBLE
            binding.bottomNavigation.itemIconTintList = franchiseModel.navHomeDrawable

            binding.toolbarShared.franchiserFlavorText.visibility = View.VISIBLE
            binding.toolbarShared.franchiserFlavorText.text = data.franchiseFlavorText
            binding.toolbarShared.franchiserFlavorText.setTextColor(data.franchiseFlavorTextColor)
            binding.toolbarShared.iconLogo.setImageDrawable(data.franchiseLogoToolbar)

            binding.toolbarShared.backArrow.setImageResource(data.backButtonResource)

            if (data.enableBackgroundColorOnToolBar) {
                binding.toolbarShared.constraintBlock.setBackgroundColor(data.franchisePrimaryColor)
            } else {
                binding.toolbarShared.constraintBlock.setBackgroundColor(getColor(R.color.white))
            }
            binding.bottomNavigation.visibility = View.VISIBLE
        }
    }

    fun showNavBar() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // Listener za promene destinacija
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d(TAG, "destination: $destination")
            when (destination.id) {

                R.id.homeFragment -> {
                    binding.toolbarShared.root.visibility = View.VISIBLE
                    binding.toolbarShared.backArrow.visibility = View.GONE
                    binding.toolbarShared.iconLogo.visibility = View.VISIBLE

                    if (franchiseViewModel.franchiseModel.value == null) {
                        binding.toolbarShared.franchiserFlavorText.visibility = View.GONE
                        binding.toolbarShared.iconLogo.setImageResource(R.drawable.ic_logo_home_screen_svg)
                        binding.toolbarShared.franchiserFlavorText.text = ""

                    } else {
                        binding.toolbarShared.franchiserFlavorText.visibility = View.VISIBLE
                    }
                }

                R.id.paymentAndPassageFragment, R.id.toolHistoryFragment, R.id.profileFragment, R.id.supportDialog, R.id.notificationDialog, R.id.deactivateAccountDialog, R.id.pdfViewDialog, R.id.noInternetConnectionDialog -> {
                    // Ako je destinacija neki od ovih fragmenata, prikaži BottomNavigationView

                    if (franchiseViewModel.franchiseModel.value == null) {
                        binding.toolbarShared.franchiserFlavorText.visibility = View.GONE
                        binding.toolbarShared.iconLogo.setImageResource(R.drawable.ic_logo_home_screen_svg)
                        binding.toolbarShared.franchiserFlavorText.text = ""

                    } else {
                        binding.toolbarShared.franchiserFlavorText.visibility = View.VISIBLE
                    }

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

                    if (franchiseViewModel.franchiseModel.value == null) {
                        binding.toolbarShared.backArrow.setImageResource(R.drawable.toolbar_shared_back_arrow)
                    } else {
                        franchiseViewModel.franchiseModel.value?.backButtonResource?.let {
                            binding.toolbarShared.backArrow.setImageResource(it)
                        }
                    }

                    binding.bottomNavigation.visibility = View.GONE
                    binding.toolbarShared.root.visibility = View.VISIBLE
                    binding.toolbarShared.backArrow.visibility = View.VISIBLE
                    binding.toolbarShared.iconLogo.visibility = View.GONE
                    binding.toolbarShared.franchiserFlavorText.visibility = View.GONE
                }

                else -> {
                    // Ako je destinacija neki drugi fragment, sakrij i Toolbar i BottomNavigationView
                    binding.bottomNavigation.visibility = View.GONE
                    binding.toolbarShared.root.visibility = View.GONE
                    binding.toolbarShared.franchiserFlavorText.visibility = View.GONE
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

    override fun attachBaseContext(newBase: Context) {
        val locale = when (val userLanguage = SharedPreferencesHelper.getUserLanguage(newBase)) {
            "cyr" -> Locale("sr_Cyrl", "RS")
            "sr", "cnr" -> Locale("sr", "RS")
            else -> Locale(userLanguage)
        }
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val localizedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(localizedContext)
    }

    private fun messageLanguageChanged(context: Context) {
        if (SharedPreferencesHelper.getLanguageChanged(context)) {
            Toast.makeText(
                context,
                getString(R.string.language_changed),
                Toast.LENGTH_SHORT
            ).show()
            SharedPreferencesHelper.setLanguageChanged(context, false)
        }
    }

    companion object {  // class tied static method
        const val TAG = "FirebaseFcm"

        fun logoutOnInvalidToken(context: Context, navController: NavController) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = DRoom.getRoomInstance(context)
                database.loginDao().deleteAll()

                withContext(Dispatchers.Main) {
                    navController.navigate(R.id.action_global_loginFragment)
                }
            }
        }

        fun showSnackMessage(message: String, binding: ActivityMainBinding) {
            Snackbar.make(binding.snackBarContainer, message, Snackbar.LENGTH_LONG)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }

    }

}