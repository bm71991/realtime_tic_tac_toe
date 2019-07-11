package com.bm.android.tictactoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.bm.android.tictactoe.user_access.*
import com.google.firebase.auth.FirebaseAuth

class TicTacToeActivity : AppCompatActivity(),
    LoginFragment.LoginFragmentInterface,
    SignupFragment.SignupFragmentInterface,
    SignupSuccessFragment.SignupSuccessFragmentInterface {
    private val fm: FragmentManager by lazy {
        supportFragmentManager
    }
    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var userAccessVm: UserAccessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_container)
        userAccessVm = ViewModelProviders.of(this).get(UserAccessViewModel::class.java)
        val fragment = fm.findFragmentById(R.id.fragment_container)

        if (fragment == null)   {
            addFirstFragment(LoginFragment())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater

        if (mAuth.currentUser != null) {
            inflater.inflate(R.menu.user_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_log_out -> onLogoutUser()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun addFirstFragment(fragment: Fragment) {
        fm.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }

    private fun replaceFragment(fragment: Fragment) {
        fm.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


    private fun addPreviousToBackStack(fragment: Fragment, previousFragmentTag:String) {
        fm.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(previousFragmentTag)
            .commit()
    }

    /* used by menu_log_out item */
    private fun onLogoutUser()  {
        mAuth.signOut()
        onStartLoginFragment()
        invalidateOptionsMenu()
    }

    /* CALLBACKS */
    /* used in LoginFragment */
    override fun onStartSignupFragment()    {
        userAccessVm.clearSignupStatus()
        replaceFragment(SignupFragment())
    }

    /* used in SignupFragment */
    override fun onStartSignupSuccessFragment() {
        replaceFragment(SignupSuccessFragment())
    }

    /* Used in TicTacToeActivity.onLogoutUser(), SignupSuccessFragment */
    override fun onStartLoginFragment()  {
        userAccessVm.clearLoginStatus()
        replaceFragment(LoginFragment())
    }

    override fun onStartLoginSuccessFragment() {
        invalidateOptionsMenu()
        replaceFragment(LoginSuccessFragment())
    }
}


