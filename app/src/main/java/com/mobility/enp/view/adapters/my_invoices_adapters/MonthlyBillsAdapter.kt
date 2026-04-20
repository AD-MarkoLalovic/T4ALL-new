package com.mobility.enp.view.adapters.my_invoices_adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.refactor.Data
import com.mobility.enp.data.model.api_my_invoices.refactor.Month
import com.mobility.enp.data.model.api_my_invoices.refactor.MyInvoicesResponse
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.databinding.ItemBillBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestFlow
import com.mobility.enp.view.adapters.tool_history.first_screen.HistorySerialAdapter
import com.mobility.enp.viewmodel.MyInvoicesViewModel
import kotlinx.coroutines.flow.MutableStateFlow

//First ADAPTER
class MonthlyBillsAdapter(
    private val data: Data,
    private var viewModel: MyInvoicesViewModel,
    private val spinnerInterface: TriggerSpinner,
    val lifecycleOwner: LifecycleOwner,
    private val montYearListener: MontYearListener,
    private val franchiserResource: FranchiseModel?, private val state: (Unit) -> Unit
) : ListAdapter<Month, MonthlyBillsAdapter.MonthlyBillsViewHolder>(DIFF_CALLBACK) {

    private val monthlyBillsArray: ArrayList<Month> = ArrayList(data.months)

    private val spinnerInt = spinnerInterface

    private var currentPage = data.currentPage
    private var lastPage = data.lastPage

    private val itemStateMap: MutableMap<Int, Boolean> = mutableMapOf()

    init {
        submitList(data.months.toList())
    }

    companion object {
        const val TAG = "MonthlyBillsAdapter"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Month>() {
            override fun areItemsTheSame(oldItem: Month, newItem: Month): Boolean {
                return oldItem.month?.value == newItem.month?.value &&
                        oldItem.year == newItem.year
            }

            override fun areContentsTheSame(oldItem: Month, newItem: Month): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun resetAdapter() {
        submitList(emptyList())
        currentPage = 0
        lastPage = 0
        itemStateMap.clear()
    }

    inner class MonthlyBillsViewHolder(
        val binding: ItemBillBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            month: Month,
            viewModel: MyInvoicesViewModel,
            spinnerInt: TriggerSpinner,
            montYearListener: MontYearListener
        ) {
            // Povezivanje meseca sa layout-om
            binding.monthly = month

            state(Unit)

            // Kreiranje stringa sa dostupnim valutama
            val availableCurrency = StringBuilder()
            month.totalCurrency?.let {
                for (currency in it) {
                    availableCurrency.append(currency.currency?.value)
                    availableCurrency.append(",")
                }
                // Uklanjanje poslednjeg zareza
                if (availableCurrency.isNotEmpty()) {
                    availableCurrency.deleteCharAt(availableCurrency.length - 1)
                }
            }

            Log.d(TAG, "filtered currency: $availableCurrency")

            // Postavljanje adaptera za prikaz valuta
            month.totalCurrency?.let {
                val invoicesTotalCurrencyAdapter = InvoicesTotalCurrencyAdapter(it)
                binding.rvTotalMonthlyInvoices.adapter = invoicesTotalCurrencyAdapter
            }

            // Formatiranje godine i meseca
            val currentMonthly = binding.monthly
            val monthValue = currentMonthly?.month?.value ?: ""
            val year = currentMonthly?.year ?: ""
            val montYear = "$year-$monthValue"

            // Provera stanja i ažuriranje UI-ja
            val isExpanded = itemStateMap[bindingAdapterPosition] ?: false
            updateUI(isExpanded)

            val savedData = viewModel.getSavedBillDetails(montYear)

            if (savedData != null) {
                val newState = !(itemStateMap[bindingAdapterPosition] ?: false)
                itemStateMap[bindingAdapterPosition] = newState
                updateUI(newState)

                setAdapterData(savedData, availableCurrency.toString())
            }

            // Klik na strelicu za otvaranje/zatvaranje
            binding.arrowDown.setOnClickListener {
                val newState = !(itemStateMap[bindingAdapterPosition] ?: false)
                itemStateMap[bindingAdapterPosition] = newState
                updateUI(newState)

                if (newState) { // Ako se otvara
                    spinnerInt.onStartSpinner()
                    if (viewModel.isNetworkAvailable()) {
                        montYearListener.onMontYearSelected(montYear)

                        val billDetailsFlow =
                            MutableStateFlow<SubmitResult<BillsDetailsResponse>>(SubmitResult.Loading)

                        collectLatestFlow(lifecycleOwner, billDetailsFlow) { serverResponse ->
                            when (serverResponse) {
                                is SubmitResult.Success -> {
                                    spinnerInterface.onStopSpinner()

                                    serverResponse.data.let { data ->
                                        viewModel.saveBill(montYear, data)
                                        setAdapterData(data, availableCurrency.toString())
                                    }

                                }

                                is SubmitResult.FailureServerError -> {
                                    spinnerInterface.onStopSpinner()
                                    logError(binding.root.context.resources.getString(R.string.server_error_msg))
                                }

                                is SubmitResult.FailureApiError -> {
                                    spinnerInterface.onStopSpinner()
                                    logError(binding.root.context.resources.getString(R.string.api_call_error))
                                }

                                else -> {
                                    spinnerInterface.onStopSpinner()
                                }
                            }
                            binding.executePendingBindings()
                        }


                        spinnerInterface.onStartSpinner()
                        viewModel.fetchBillDetailsNew(
                            billDetailsFlow,
                            montYear,
                            availableCurrency.toString()
                        )

                        franchiserResource?.let { data ->
                            binding.arrowDown.setImageDrawable(
                                ContextCompat.getDrawable(
                                    binding.root.context,
                                    data.upArrowResource
                                )
                            )
                        } ?: run {
                            binding.arrowDown.setImageDrawable(
                                ContextCompat.getDrawable(
                                    binding.root.context,
                                    R.drawable.ic_arrow_up
                                )
                            )
                        }

                    } else {
                        Toast.makeText(
                            binding.root.context,
                            binding.root.context.getText(R.string.no_internet),
                            Toast.LENGTH_LONG
                        ).show()
                        spinnerInt.onStopSpinner()
                    }
                } else { // Ako se zatvara
                    binding.recyclerViewMonthlyBills.visibility = View.GONE
                    binding.scrollView.visibility = View.GONE
                    franchiserResource?.let { data ->
                        binding.arrowDown.setImageDrawable(
                            ContextCompat.getDrawable(binding.root.context, data.downArrowResource)
                        )
                    } ?: run {
                        binding.arrowDown.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context,
                                R.drawable.ic_arrow_down
                            )
                        )
                    }
                }
            }

            // Izvršavanje svih vezanih promena
            binding.executePendingBindings()
        }

        fun reset() {
            updateUI(false)
        }

        private fun setAdapterData(data: BillsDetailsResponse, availableCurrency: String) {
            val billsDetailsAdapter = BillsDetailsAdapter(
                data.data,
                viewModel,
                lifecycleOwner,
                spinnerInt,
                availableCurrency.toString()
            )
            binding.recyclerViewMonthlyBills.adapter =
                billsDetailsAdapter
            billsDetailsAdapter.submitList(data.data)

            if (data.data
                    .bills.isNotEmpty()
            ) {
                val heightInDp: Int = when (data.data.bills.size) {
                    1 -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_one_item
                    )

                    2 -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_two_items
                    )

                    3 -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_three_items
                    )

                    else -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_more_items
                    )
                }
                binding.scrollView.layoutParams.height = heightInDp
                binding.scrollView.requestLayout()

                binding.recyclerViewMonthlyBills.visibility =
                    View.VISIBLE
                binding.scrollView.visibility = View.VISIBLE
            }
            spinnerInt.onStopSpinner()
        }

        private fun updateUI(isExpanded: Boolean) {
            if (isExpanded) {
                binding.recyclerViewMonthlyBills.visibility = View.VISIBLE
                binding.scrollView.visibility = View.VISIBLE
                franchiserResource?.let { data ->
                    binding.arrowDown.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            data.upArrowResource
                        )
                    )
                } ?: run {
                    binding.arrowDown.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_arrow_up
                        )
                    )
                }
            } else {
                binding.recyclerViewMonthlyBills.visibility = View.GONE
                binding.scrollView.visibility = View.GONE
                franchiserResource?.let { data ->
                    binding.arrowDown.setImageDrawable(
                        ContextCompat.getDrawable(binding.root.context, data.downArrowResource)
                    )
                } ?: run {
                    binding.arrowDown.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_arrow_down
                        )
                    )
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyBillsViewHolder {
        return MonthlyBillsViewHolder(
            ItemBillBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MonthlyBillsViewHolder, position: Int) {
        val currentBill = getItem(holder.bindingAdapterPosition)

        holder.reset()

        holder.binding.recyclerViewMonthlyBills.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                // Provera da li postoji roditeljski kontejner koji podržava skrolovanje
                if (holder.binding.recyclerViewMonthlyBills.hasNestedScrollingParent()) {
                    // Blokiranje roditeljskog kontejner da ne obrađuje ovaj događaj skrolovanja
                    holder.binding.recyclerViewMonthlyBills.parent?.requestDisallowInterceptTouchEvent(
                        true
                    )
                }
            } else if (event.action == MotionEvent.ACTION_UP) {
                // Pozivanje performClick kada se detektuje klik
                holder.binding.recyclerViewMonthlyBills.performClick()
            }
            false
        }

        holder.bind(
            currentBill, viewModel, spinnerInt, montYearListener
        )
        checkDataFill(holder.bindingAdapterPosition, currentBill, holder.binding.root.context)
    }

    private fun checkDataFill(position: Int, currentBill: Month, context: Context) {
        Log.d(
            TAG,
            "onBindViewHolder: adapter pos $position arrayTotal ${monthlyBillsArray.size - 1} totalItems ${data.total}"
        )
        if (currentList.lastOrNull() == currentBill && lastPage > currentPage) {

            val paginationUpdate =
                MutableStateFlow<SubmitResult<MyInvoicesResponse>>(SubmitResult.Loading)

            collectLatestFlow(lifecycleOwner, paginationUpdate) { serverResponse ->
                when (serverResponse) {
                    is SubmitResult.Success -> {
                        spinnerInterface.onStopSpinner()
                        serverResponse?.let { response ->
                            currentPage = response.data.data?.currentPage ?: 0

                            val newList = currentList.toMutableList()
                            newList.addAll(response.data.data!!.months)
                            submitList(newList)
                        }
                    }

                    is SubmitResult.FailureServerError -> {
                        spinnerInterface.onStopSpinner()
                        logError(context.resources.getString(R.string.server_error_msg))
                    }

                    is SubmitResult.FailureApiError -> {
                        spinnerInterface.onStopSpinner()
                        logError(context.resources.getString(R.string.api_call_error))
                    }

                    else -> {
                        spinnerInterface.onStopSpinner()
                    }
                }
            }

            spinnerInterface.onStartSpinner()
            spinnerInterface.pagingUpdate(currentPage + 1, paginationUpdate)
        }
    }

    private fun logError(string: String) {
        Log.d(HistorySerialAdapter.TAG, "showError: $string")
    }

    interface TriggerSpinner {
        fun onStartSpinner()
        fun onStopSpinner()
        fun pagingUpdate(nextPage: Int, flow: MutableStateFlow<SubmitResult<MyInvoicesResponse>>)
        fun pagingUpdateBill(
            nextPage: Int,
            flow: MutableStateFlow<SubmitResult<BillsDetailsResponse>>,
            availableCurrencies: String
        )

        fun requestNotificationFromUser(userPermission: BillsDetailsAdapter.UserPermission)
    }

    interface MontYearListener {
        fun onMontYearSelected(montYear: String)
    }
}
