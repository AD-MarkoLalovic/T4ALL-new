package com.mobility.enp.view.adapters.my_invoices_adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.DataMonthly
import com.mobility.enp.data.model.api_my_invoices.Month
import com.mobility.enp.data.model.api_my_invoices.MyInvoicesResponse
import com.mobility.enp.data.model.franchise.FranchiseModel
import com.mobility.enp.databinding.ItemBillBinding
import com.mobility.enp.viewmodel.MyInvoicesViewModel

//First ADAPTER
class MonthlyBillsAdapter(
    private val data: DataMonthly,
    private var viewModel: MyInvoicesViewModel,
    private val errorBody: MutableLiveData<ErrorBody>,
    private val spinnerInterface: TriggerSpinner,
    val lifecycleOwner: LifecycleOwner,
    private val montYearListener: MontYearListener,
    private val franchiserResource: FranchiseModel?
) : RecyclerView.Adapter<MonthlyBillsAdapter.MonthlyBillsViewHolder>() {

    private val monthlyBillsArray: ArrayList<Month> = ArrayList(data.months)

    private val spinnerInt = spinnerInterface

    private var currentPage = data.currentPage
    private val lastPage = data.lastPage

    private val itemStateMap: MutableMap<Int, Boolean> = mutableMapOf()

    companion object {
        const val TAG = "MonthlyBillsAdapter"
    }

    inner class MonthlyBillsViewHolder(
        val binding: ItemBillBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            month: Month,
            viewModel: MyInvoicesViewModel,
            error: MutableLiveData<ErrorBody>,
            spinnerInt: TriggerSpinner,
            montYearListener: MontYearListener
        ) {
            // Povezivanje meseca sa layout-om
            binding.monthly = month

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

            // Klik na strelicu za otvaranje/zatvaranje
            binding.arrowDown.setOnClickListener {
                val newState = !(itemStateMap[bindingAdapterPosition] ?: false)
                itemStateMap[bindingAdapterPosition] = newState
                updateUI(newState)

                if (newState) { // Ako se otvara
                    spinnerInt.onStartSpinner()
                    if (viewModel.isNetworkAvailable()) {
                        montYearListener.onMontYearSelected(montYear)

                        viewModel.fetchBillDetailsNew(object : FetchBillsDetails {
                            override fun onOK(bill: BillsDetailsResponse) {
                                bill.data.let {
                                    val billsDetailsAdapter = BillsDetailsAdapter(
                                        it,
                                        viewModel,
                                        error,
                                        lifecycleOwner,
                                        spinnerInt,
                                        availableCurrency.toString()
                                    )
                                    binding.recyclerViewMonthlyBills.adapter = billsDetailsAdapter
                                    billsDetailsAdapter.submitList(it)

                                    if (it.bills.isNotEmpty()) {
                                        val heightInDp: Int = when (it.bills.size) {
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

                                        binding.recyclerViewMonthlyBills.visibility = View.VISIBLE
                                        binding.scrollView.visibility = View.VISIBLE
                                    }
                                    spinnerInt.onStopSpinner()
                                }
                            }

                            override fun onFailed() {
                                spinnerInt.onStopSpinner()
                            }
                        }, montYear, availableCurrency.toString(), error)


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
        val currentBill = monthlyBillsArray[holder.bindingAdapterPosition]

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
            currentBill, viewModel, errorBody, spinnerInt, montYearListener
        )
        checkDataFill(holder.bindingAdapterPosition, currentBill)
    }

    private fun checkDataFill(position: Int, currentBill: Month) {
        Log.d(
            TAG,
            "onBindViewHolder: adapter pos $position arrayTotal ${monthlyBillsArray.size - 1} totalItems ${data.total}"
        )
        if (monthlyBillsArray[monthlyBillsArray.size - 1] == currentBill && lastPage > currentPage) {
            val data: MutableLiveData<MyInvoicesResponse> = MutableLiveData()

            data.observe(lifecycleOwner) { dataResponse ->
                spinnerInterface.onStopSpinner()
                dataResponse?.let {
                    currentPage = it.data.currentPage

                    for (month: Month in it.data.months) {
                        monthlyBillsArray.add(month)
                        notifyItemChanged(monthlyBillsArray.size - 1)
                    }

                    data.removeObservers(lifecycleOwner)
                }
            }
            spinnerInterface.onStartSpinner()
            spinnerInterface.pagingUpdate(currentPage + 1, data)
        }
    }

    override fun getItemCount() = monthlyBillsArray.size

    interface FetchBillsDetails {
        fun onOK(bill: BillsDetailsResponse)
        fun onFailed()
    }

    interface TriggerSpinner {
        fun onStartSpinner()
        fun onStopSpinner()
        fun pagingUpdate(nextPage: Int, data: MutableLiveData<MyInvoicesResponse>)
        fun pagingUpdateBill(
            nextPage: Int,
            data: MutableLiveData<BillsDetailsResponse>,
            availableCurrencies: String
        )

        fun requestNotificationFromUser(userPermission: BillsDetailsAdapter.UserPermission)
    }

    interface MontYearListener {
        fun onMontYearSelected(montYear: String)
    }
}
