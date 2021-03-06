package com.example.sequence2

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sequence2.api.Provider
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val activityScope = CoroutineScope(
        SupervisorJob() +
                Dispatchers.Main
    )
    private val CAT = "EDPMR"
    private var edtPseudo: EditText? = null
    private var edtPass: EditText? = null
    private var cbRemember: CheckBox? = null
    private var btnOK: Button? = null
    private var sp: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private fun alerter(s: String) {
        Log.i(CAT, s)
        val t = Toast.makeText(this, s, Toast.LENGTH_SHORT)
        t.show()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(CAT, "onCreate")

        //get ui elements
        btnOK = findViewById(R.id.btnOK)
        edtPseudo = findViewById(R.id.pseudo)
        edtPass = findViewById(R.id.pass)
        cbRemember = findViewById(R.id.cbRemember)


        //initialize sp and editor
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        val smartCastSp = sp
        if (smartCastSp != null){
            editor = smartCastSp.edit()
        }


        //set on click listeners
        val smartCastBtn = btnOK
        if(smartCastBtn != null){
            smartCastBtn.setOnClickListener(this)
        }
        val smartCastCheckBox = cbRemember
        if(smartCastCheckBox != null){
            smartCastCheckBox.setOnClickListener(this)
        }
    }

    // display menu if method returns true
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    // called upon item selection. returns true if item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id){
            R.id.menu_settings -> {
                alerter("Menu : click sur pr??f??rences")
                val iGP = Intent(this, GestionPreferences::class.java)
                startActivity(iGP)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Log.i(CAT, "onResume")

        btnOK?.isEnabled = verifReseau()



    }

    override fun onStart() {
        super.onStart()
        Log.i(CAT, "onStart")

        // Relire les pr??f??rences partag??es de l'application
        val cbR = sp!!.getBoolean("remember", true)

        //acutaliser l'??tat de la case ?? cocher
        cbRemember!!.isChecked = cbR

        //SI la case est coch??e, on utilise les pr??f??rences pour d??finir le login
        if(cbRemember!!.isChecked){
            val pseudo = sp!!.getString("login", "login inconnu")
            val pass = sp!!.getString("pass", "pass inconnu")
            edtPseudo!!.setText(pseudo)
            edtPass!!.setText(pass)
        } else{
            // Sinon, le champ doit etre vide
            edtPseudo!!.setText("")
            edtPass!!.setText("")
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(CAT, "onRestart")
    }



    override fun onClick(v: View){
        when (v.id){
            R.id.btnOK -> {
                alerter(edtPseudo!!.text.toString())
                alerter(edtPass!!.text.toString())

                //on enregistre le login dans les pr??f??rences
                if(cbRemember!!.isChecked){
                    editor!!.putString("login", edtPseudo!!.text.toString())
                    editor!!.putString("pass", edtPass!!.text.toString())
                    editor!!.commit()
                }

                login(edtPseudo!!.text.toString(), edtPass!!.text.toString())


            }

            R.id.cbRemember -> {
                alerter("click sur CB")

                //On clique sur la case : il faut mettre ?? jour les pr??f??rences
                editor!!.putBoolean("remember", cbRemember!!.isChecked)
                editor!!.commit()
                if(!cbRemember!!.isChecked){
                    // on supprime le login de pr??f??rences
                    editor!!.putString("login", "")
                    editor!!.putString("pass", "")
                    editor!!.commit()


                }
            }

            R.id.pseudo -> alerter("Veuillez entrer votre pseudo")

        }
    }

    private fun login(pseudo: String, pass: String) {

        activityScope.launch{
            try{
                if(verifReseau()){
                    val authResp = Provider.authenticate(pseudo, pass)
                    Log.i(CAT, authResp.toString())
                    if (authResp.success){
                        editor!!.putString("hash", authResp.hash)
                        editor!!.commit()

                        // Fabrication d'un bundle de donn??es
                        val bdl = Bundle()
                        bdl.putString("string",pseudo)
                        //Changer d'activit??
                        val versChoixList: Intent
                        // Intent explicite
                        versChoixList = Intent(this@MainActivity, ChoixListActivity::class.java)
                        // Ajout d'un bundle a l'intent
                        versChoixList.putExtras(bdl)
                        startActivity(versChoixList)
                    }
                    else{
                        Log.i(CAT, "authentification failed")
                        alerter("authentification failed")
                    }
                } else{
                    Log.i(CAT, "no connection")
                    alerter("no connection")
                }

            } catch (e: Exception){
                Log.i(CAT, "Error: " + e.message + pseudo + pass)
            }
        }
    }


    fun verifReseau(): Boolean {
        // On v??rifie si le r??seau est disponible,
        // si oui on change le statut du bouton de connexion
        val cnMngr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cnMngr.activeNetworkInfo
        var sType = "Aucun r??seau d??tect??"
        var bStatut = false
        if (netInfo != null) {
            val netState = netInfo.state
            if (netState.compareTo(NetworkInfo.State.CONNECTED) == 0) {
                bStatut = true
                val netType = netInfo.type
                when (netType) {
                    ConnectivityManager.TYPE_MOBILE -> sType = "R??seau mobile d??tect??"
                    ConnectivityManager.TYPE_WIFI -> sType = "R??seau wifi d??tect??"
                }
            }
        }
        Log.i(CAT, sType)
        return bStatut
    }


}

