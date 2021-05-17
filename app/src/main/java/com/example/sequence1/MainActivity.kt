package com.example.sequence1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // affiche le menu si la méthode renvoie vrai
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        // R.menu.menu dénote le fichier  res/menu/menu.xml
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.menu_settings -> {
                //afficher l'activité SettingsActivity
                val iSA = Intent(this, SettingsActivity::class.java)
                startActivity(iSA)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}