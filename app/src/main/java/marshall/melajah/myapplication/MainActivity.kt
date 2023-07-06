package marshall.melajah.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import marshall.melajah.myapplication.databinding.ActivityMainBinding
import marshall.melajah.myapplication.fragments.AkunFrag
import marshall.melajah.myapplication.fragments.TransaksiFrag

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeCurrentFragment(TransaksiFrag())

        val accountFragment = AkunFrag()
        var buttonprofil= findViewById<ImageView>(R.id.ivprofil)
        var buttonA = findViewById<ImageView>(R.id.ivanalisis)
        var buttonBer = findViewById<ImageView>(R.id.ivberanda)

        buttonprofil.setOnClickListener{
            val intent = Intent(this, InsertData::class.java)
            startActivity(intent)
        }

        buttonA.setOnClickListener{
            makeCurrentFragment(accountFragment)
        }

        buttonBer.setOnClickListener{
            makeCurrentFragment(TransaksiFrag())
        }

        //---Bottom Nabvigation Method 2 :
//        val transactionFragment = TransactionFragment()
//        val accountFragment = AccountFragment()
//        binding.chipAppBar.setItemSelected(R.id.ic_transaction,true)
//        makeCurrentFragment(transactionFragment)
//        binding.chipAppBar.setOnItemSelectedListener { //when the bottom nav clicked
//            when (it){
//                R.id.ic_transaction -> makeCurrentFragment(transactionFragment)
//                R.id.ic_account -> makeCurrentFragment(accountFragment)
//            }
//            val b = true
//            b
//        }
        //------

    }

//    private fun bottom_menu() { //method 1
//        binding.chipAppBar.setOnItemSelectedListener {
//            when (it) {
//                R.id.ic_transaction -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fl_wrapper, TransactionFragment()).commit()
//                }
//                R.id.ic_account -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.fl_wrapper, AccountFragment()).commit()
//                }
//            }
//        }
//    }

    private fun makeCurrentFragment(fragment: Fragment) { //method 2
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

}