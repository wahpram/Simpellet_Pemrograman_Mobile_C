package marshall.melajah.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import marshall.melajah.myapplication.LoginActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import marshall.melajah.myapplication.R
import marshall.melajah.myapplication.entity.Transaksi

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AkunFrag.newInstance] factory method to
 * create an instance of this fragment.
 */
class AkunFrag : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // Initialize Firebase Auth and database
    private var auth: FirebaseAuth = Firebase.auth
    private var user = Firebase.auth.currentUser
    private val uid = user?.uid //get user id from database
    private var dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference(uid!!)

    //initialize var for storing amount value from db
    var amountExpense: Double = 0.0
    var amountIncome: Double = 0.0
    var allTimeExpense: Double = 0.0
    var allTimeIncome: Double = 0.0

    private var dateStart: Long = 0
    private var dateEnd: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logout()  //logout button clicked

        accountDetails() //Output Account details from firebase
//
//        showAllTimeRecap()

        setInitDate() //initialized or set the current date data to this month date range, it is default date range when the fragment is open

        Handler().postDelayed({ //to make setupPieChart() and showAllTimeRecap() start after fetchAmount(), otherwise the setupPieChart() just show 0.0 value
            showAllTimeRecap() //show all time recap text

        }, 200)

//        dateRangePicker() //date range picker

        swipeRefresh()
    }

    private fun swipeRefresh() {
        val swipeRefreshLayout: SwipeRefreshLayout = requireView().findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener { //call getTransaction() back to refresh the recyclerview
            accountDetails()
            showAllTimeRecap()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setInitDate() {
//        val dateRangeButton: Button = requireView().findViewById(R.id.buttonDate)

        val currentDate = Date()
        val cal: Calendar = Calendar.getInstance(TimeZone.getDefault())
        cal.time = currentDate

        val startDay = cal.getActualMinimum(Calendar.DAY_OF_MONTH) //get the first date of the month
        cal.set(Calendar.DAY_OF_MONTH, startDay)
        val startDate = cal.time
        dateStart= startDate.time //convert to millis

        val endDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH) //get the last date of the month
        cal.set(Calendar.DAY_OF_MONTH, endDay)
        val endDate = cal.time
        dateEnd= endDate.time //convert to millis

        fetchAmount(dateStart, dateEnd) //call fetch amount so showAllTimeRecap() can be executed
//        dateRangeButton.text = "This Month"
    }

//    private fun dateRangePicker() { // Material design date range picker : https://material.io/components/date-pickers/android
//        val dateRangeButton: Button = requireView().findViewById(R.id.buttonDate)
//        dateRangeButton.setOnClickListener { //when date range picker clicked
//            // Opens the date range picker with the range of the first day of
//            // the month to today selected.
//            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
//                .setTitleText("Select Date")
//                .setSelection(
//                    Pair(
//                        dateStart,
//                        dateEnd
//                    )
//                ).build()
//            datePicker.show(parentFragmentManager, "DatePicker")
//
//            // Setting up the event for when ok is clicked
//            datePicker.addOnPositiveButtonClickListener {
//                //convert the result from string to long type :
//                val dateString = datePicker.selection.toString()
//                val date: String = dateString.filter { it.isDigit() } //only takes digit value
//                //divide the start and end date value :
//                val pickedDateStart = date.substring(0,13).toLong()
//                val pickedDateEnd  = date.substring(13).toLong()
//                dateRangeButton.text = convertDate(pickedDateStart, pickedDateEnd) //call function to convert millis to string
//                fetchAmount(pickedDateStart, pickedDateEnd) //show the report based on date range
//
//                Handler().postDelayed({
//
//                }, 200)
//            }
//        }
//    }

    private fun accountDetails() {
        val tvName: TextView = requireView().findViewById(R.id.tvName)
        val tvEmail: TextView = requireView().findViewById(R.id.tvEmail)
        val tvPicture: TextView = requireView().findViewById(R.id.picture)
        val verified: CardView = requireView().findViewById(R.id.verified)
        val notVerified: CardView = requireView().findViewById(R.id.notVerified)

        user?.reload() //reload user, so the verified badge can be change once the user have already verified the email.
        user?.let {
            // Name and email address
            val userName = user!!.displayName
            val email = user!!.email

            if (user!!.isEmailVerified){ //check if user email already verified
                verified.visibility = View.VISIBLE
                notVerified.visibility = View.GONE

                verified.setOnClickListener {
                    Toast.makeText(this@AkunFrag.activity, "Your account is verified!", Toast.LENGTH_LONG).show()
                }
            }else{
                notVerified.visibility = View.VISIBLE
                verified.visibility = View.GONE

                notVerified.setOnClickListener {
                    user?.sendEmailVerification()?.addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@AkunFrag.activity, "Check Your Email! (Including Spam)", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@AkunFrag.activity, "${it.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            val splitValue = email?.split("@") //
            val name = splitValue?.get(0)
            tvName.text = name.toString()
            tvPicture.text = name?.get(0).toString().uppercase()
            tvEmail.text = email.toString()

            if (userName != null) {
                tvName.text = userName.toString()
                tvPicture.text = userName[0].toString().uppercase()
            }

        }
    }

    private fun logout() {
        val btnLogout: ImageButton = requireView().findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            Intent(this.activity, LoginActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //tujuan flag agar tidak bisa menggunakan back
                activity?.startActivity(it)
            }
        }
    }

    private fun showAllTimeRecap() {
        //---show recap after calculation---
        val tvNetAmount: TextView = requireView().findViewById(R.id.netAmount)
        val tvAmountExpense: TextView = requireView().findViewById(R.id.expenseAmount)
        val tvAmountIncome: TextView = requireView().findViewById(R.id.incomeAmount)

        tvNetAmount.text = "${allTimeIncome-allTimeExpense}"
        tvAmountExpense.text = "$allTimeExpense"
        tvAmountIncome.text = "$allTimeIncome"
    }



    private fun fetchAmount(dateStart: Long, dateEnd: Long) { //show and calculate transaction recap
        var amountExpenseTemp = 0.0
        var amountIncomeTemp = 0.0

        val transactionList: ArrayList<Transaksi> = arrayListOf<Transaksi>()

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                if (snapshot.exists()) {
                    for (transactionSnap in snapshot.children) {
                        val transactionData =
                            transactionSnap.getValue(Transaksi::class.java) //reference data class
                        transactionList.add(transactionData!!)
                    }
                }
                //separate expanse amount and income amount, and show it based on the range date :
                for ((i) in transactionList.withIndex()){
                    if (transactionList[i].type == 1 &&
                        transactionList[i].date!! > dateStart-86400000 && //minus by 1 day
                        transactionList[i].date!! <= dateEnd){
                        amountExpenseTemp += transactionList[i].amount!!
                    }else if (transactionList[i].type == 2 &&
                        transactionList[i].date!! > dateStart-86400000 &&
                        transactionList[i].date!! <= dateEnd){
                        amountIncomeTemp += transactionList[i].amount!!
                    }
                }
                amountExpense= amountExpenseTemp
                amountIncome = amountIncomeTemp

                var amountExpenseTemp = 0.0 //reset
                var amountIncomeTemp = 0.0

                //take all amount expense and income :
                for ((i) in transactionList.withIndex()){
                    if (transactionList[i].type == 1 ){
                        amountExpenseTemp += transactionList[i].amount!!
                    }else if (transactionList[i].type == 2){
                        amountIncomeTemp += transactionList[i].amount!!
                    }
                }
                allTimeExpense = amountExpenseTemp
                allTimeIncome = amountIncomeTemp

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun convertDate(dateStart: Long, dateEnd: Long): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date1 = Date(dateStart)
        val date2 = Date(dateEnd)
        val result1 = simpleDateFormat.format(date1)
        val result2 = simpleDateFormat.format(date2)
        return "$result1 - $result2"
    }

    override fun onResume() {
        super.onResume()

        showAllTimeRecap() //show all time recap text
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AkunFrag().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
