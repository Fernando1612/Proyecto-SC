/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.materialstudies.reply.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity

//Bibliotecas importantes
//Bibliotecas para el Bluethoot
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
//Biblioteca para el reconocimiento de voz
import android.speech.RecognizerIntent

import android.content.Intent
import android.graphics.Color
import android.os.Bundle

import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.materialstudies.reply.R
import com.materialstudies.reply.data.EmailStore
import com.materialstudies.reply.databinding.ActivityMainBinding
import com.materialstudies.reply.ui.compose.ComposeFragmentDirections
import com.materialstudies.reply.ui.email.EmailFragmentArgs
import com.materialstudies.reply.ui.home.HomeFragmentDirections
import com.materialstudies.reply.ui.home.Mailbox
import com.materialstudies.reply.ui.nav.AlphaSlideAction
import com.materialstudies.reply.ui.nav.BottomNavDrawerFragment
import com.materialstudies.reply.ui.nav.ChangeSettingsMenuStateAction
import com.materialstudies.reply.ui.nav.HalfClockwiseRotateSlideAction
import com.materialstudies.reply.ui.nav.HalfCounterClockwiseRotateSlideAction
import com.materialstudies.reply.ui.nav.NavigationAdapter
import com.materialstudies.reply.ui.nav.NavigationModelItem
import com.materialstudies.reply.ui.nav.ShowHideFabStateAction
import com.materialstudies.reply.ui.search.SearchFragmentDirections
import com.materialstudies.reply.util.contentView
import java.io.IOException
import java.util.*
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.collections.ArrayList





class MainActivity : AppCompatActivity(),
                     Toolbar.OnMenuItemClickListener,
                     NavController.OnDestinationChangedListener,
                     NavigationAdapter.NavigationAdapterListener {

    //Variables a ocupar
    val addres = "98:D3:51:F5:BF:94"        //Direccion del bluethoot
    var isBtConnect = false                 //Variable para combrobar si esta conenctado
    val myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")        //Identificador unico
    var btSocket: BluetoothSocket? = null       //Socket para del bluethoot

    var imageBlue: ImageView? = null
    var foco_sala: ImageView? = null
    var foco_cocina: ImageView? = null


    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)
    private val bottomNavDrawer: BottomNavDrawerFragment by lazy(NONE) {
        supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as BottomNavDrawerFragment
    }

    //val blue: ImageView = findViewById(R.id.bottom_app_bar_logo)

    // Keep track of the current Email being viewed, if any, in order to pass the correct email id
    // to ComposeFragment when this Activity's FAB is clicked.
    private var currentEmailId = -1L

    val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                ?.childFragmentManager
                ?.fragments
                ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpBottomNavigationAndFab()
        imageBlue = findViewById(R.id.bottom_app_bar_logo)
        foco_sala = findViewById(R.id.foco_sala)
        foco_cocina = findViewById(R.id.foco_cocina)
    }

    private fun setUpBottomNavigationAndFab() {
        // Wrap binding.run to ensure ContentViewBindingDelegate is calling this Activity's
        // setContentView before accessing views
        binding.run {
            findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener(
                this@MainActivity
            )
        }

        // Set a custom animation for showing and hiding the FAB
        binding.fab.apply {
            setShowMotionSpecResource(R.animator.fab_show)
            setHideMotionSpecResource(R.animator.fab_hide)
            //Acción al presionar el boton del microfono
            setOnClickListener {
                //Iniciar el reconocimiento de voz
                val inte = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                //Guardar el resultado de la actividad
                inte.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                //Utilizar el idioma por defecto del celular
                inte.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                //Mesaje de instrucciones por hacer
                inte.putExtra(RecognizerIntent.EXTRA_PROMPT, "¿Qué luz deseas encender?")
                //Iniciar la acticidad con el resultado almacenado
                startActivityForResult(inte,100)
            }
        }


        bottomNavDrawer.apply {
            addOnSlideAction(HalfClockwiseRotateSlideAction(binding.bottomAppBarChevron))
            addOnSlideAction(AlphaSlideAction(binding.bottomAppBarTitle, true))
            addOnStateChangedAction(ShowHideFabStateAction(binding.fab))
            addOnStateChangedAction(ChangeSettingsMenuStateAction { showSettings ->
                // Toggle between the current destination's BAB menu and the menu which should
                // be displayed when the BottomNavigationDrawer is open.
                binding.bottomAppBar.replaceMenu(if (showSettings) {
                    R.menu.bottom_app_bar_settings_menu
                } else {
                    getBottomAppBarMenuForDestination()
                })
            })

            addOnSandwichSlideAction(HalfCounterClockwiseRotateSlideAction(binding.bottomAppBarChevron))
            addNavigationListener(this@MainActivity)
        }


        // Set up the BottomAppBar menu
        binding.bottomAppBar.apply {
            setNavigationOnClickListener {
                bottomNavDrawer.toggle()
            }
            setOnMenuItemClickListener(this@MainActivity)
        }

        // Set up the BottomNavigationDrawer's open/close affordance
        binding.bottomAppBarContentContainer.setOnClickListener {
            if (isBtConnect == true){
                disconectBT()
            }else if (isBtConnect == false){
                connectBT()
            }
            //bottomNavDrawer.toggle()
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        // Set the currentEmail being viewed so when the FAB is pressed, the correct email
        // reply is created. In a real app, this should be done in a ViewModel but is done
        // here to keep things simple. Here we're also setting the configuration of the
        // BottomAppBar and FAB based on the current destination.
        when (destination.id) {
            R.id.homeFragment -> {
                currentEmailId = -1
                setBottomAppBarForHome(getBottomAppBarMenuForDestination(destination))
            }
            R.id.emailFragment -> {
                currentEmailId =
                    if (arguments == null) -1 else EmailFragmentArgs.fromBundle(arguments).emailId
                setBottomAppBarForEmail(getBottomAppBarMenuForDestination(destination))
            }
            R.id.composeFragment -> {
                currentEmailId = -1
                setBottomAppBarForCompose()
            }
            R.id.searchFragment -> {
                currentEmailId = -1
                setBottomAppBarForSearch()
            }
        }
    }

    /**
     * Helper function which returns the menu which should be displayed for the current
     * destination.
     *
     * Used both when the destination has changed, centralizing destination-to-menu mapping, as
     * well as switching between the alternate menu used when the BottomNavigationDrawer is
     * open and closed.
     */
    @MenuRes
    private fun getBottomAppBarMenuForDestination(destination: NavDestination? = null): Int {
        val dest = destination ?: findNavController(R.id.nav_host_fragment).currentDestination
        return when (dest?.id) {
            R.id.homeFragment -> R.menu.bottom_app_bar_home_menu
            R.id.emailFragment -> R.menu.bottom_app_bar_email_menu
            else -> R.menu.bottom_app_bar_home_menu
        }
    }

    private fun setBottomAppBarForHome(@MenuRes menuRes: Int) {
        binding.run {
            fab.setImageState(intArrayOf(-android.R.attr.state_activated), true)
            bottomAppBar.visibility = View.VISIBLE
            bottomAppBar.replaceMenu(menuRes)
            fab.contentDescription = getString(R.string.fab_compose_email_content_description)
            bottomAppBarTitle.visibility = View.VISIBLE
            bottomAppBar.performShow()
            fab.show()
        }
    }

    private fun setBottomAppBarForEmail(@MenuRes menuRes: Int) {
        binding.run {
            fab.setImageState(intArrayOf(android.R.attr.state_activated), true)
            bottomAppBar.visibility = View.VISIBLE
            bottomAppBar.replaceMenu(menuRes)
            fab.contentDescription = getString(R.string.fab_reply_email_content_description)
            bottomAppBarTitle.visibility = View.INVISIBLE
            bottomAppBar.performShow()
            fab.show()
        }
    }

    private fun setBottomAppBarForCompose() {
        hideBottomAppBar()
    }

    private fun setBottomAppBarForSearch() {
        hideBottomAppBar()
        binding.fab.hide()
    }

    private fun hideBottomAppBar() {
        binding.run {
            bottomAppBar.performHide()
            // Get a handle on the animator that hides the bottom app bar so we can wait to hide
            // the fab and bottom app bar until after it's exit animation finishes.
            bottomAppBar.animate().setListener(object : AnimatorListenerAdapter() {
                var isCanceled = false
                override fun onAnimationEnd(animation: Animator?) {
                    if (isCanceled) return

                    // Hide the BottomAppBar to avoid it showing above the keyboard
                    // when composing a new email.
                    bottomAppBar.visibility = View.GONE
                    fab.visibility = View.INVISIBLE
                }
                override fun onAnimationCancel(animation: Animator?) {
                    isCanceled = true
                }
            })
        }
    }

    override fun onNavMenuItemClicked(item: NavigationModelItem.NavMenuItem) {
        // Swap the list of emails for the given mailbox
        navigateToHome(item.titleRes, item.mailbox)


    }

    override fun onNavEmailFolderClicked(folder: NavigationModelItem.NavEmailFolder) {
        // Do nothing
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_settings -> {
                bottomNavDrawer.close()
                showDarkThemeMenu()
            }
            R.id.menu_search -> navigateToSearch()
            R.id.menu_email_star -> {
                EmailStore.update(currentEmailId) { isStarred = !isStarred }
            }
            R.id.menu_email_delete -> {
                EmailStore.delete(currentEmailId)
                findNavController(R.id.nav_host_fragment).popBackStack()
            }
        }
        return true
    }

    private fun showDarkThemeMenu() {
        MenuBottomSheetDialogFragment
            .newInstance(R.menu.dark_theme_bottom_sheet_menu)
            .show(supportFragmentManager, null)
    }

    fun navigateToHome(@StringRes titleRes: Int, mailbox: Mailbox) {
        binding.bottomAppBarTitle.text = getString(titleRes)
        currentNavigationFragment?.apply {
            exitTransition = MaterialFadeThrough().apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
        }
        val directions = HomeFragmentDirections.actionGlobalHomeFragment(mailbox)
        findNavController(R.id.nav_host_fragment).navigate(directions)
    }

    private fun navigateToCompose() {
        currentNavigationFragment?.apply {
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
        }
        val directions = ComposeFragmentDirections.actionGlobalComposeFragment(currentEmailId)
        findNavController(R.id.nav_host_fragment).navigate(directions)
    }

    private fun navigateToSearch() {
        currentNavigationFragment?.apply {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
                duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            }
        }
        val directions = SearchFragmentDirections.actionGlobalSearchFragment()
        findNavController(R.id.nav_host_fragment).navigate(directions)
    }

    /**
     * Set this Activity's night mode based on a user's in-app selection.
     */
    private fun onDarkThemeMenuItemSelected(itemId: Int): Boolean {
        val nightMode = when (itemId) {
            R.id.menu_light -> AppCompatDelegate.MODE_NIGHT_NO
            R.id.menu_dark -> AppCompatDelegate.MODE_NIGHT_YES
            R.id.menu_battery_saver -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            R.id.menu_system_default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> return false
        }

        delegate.localNightMode = nightMode
        return true
    }

    //Resultado de la actividad
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Comprobar que la actividad se realizo correctamente
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            //Guardar el resultado
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            //Con el mensaje guardado, se manda a llamar al metodo sendText
            sendText(result?.get(0).toString().toLowerCase())
            }
        }

    //Metodo para conectar el bluethoot
    private fun connectBT(){
        var connectSuccess: Boolean = true
        //Comprobar que el bluethoot no este conectado
        if(btSocket == null || !isBtConnect){
            try {
                //Adaptador para el bluethoot
                var myBluetooth = BluetoothAdapter.getDefaultAdapter()
                //Asignar dispositivo utilizando la dirección del bluetooht
                val dispositivo: BluetoothDevice = myBluetooth.getRemoteDevice(addres)
                //Conexion del bluethoot mediande sockets
                btSocket.let { btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID)}
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                btSocket?.connect()
                isBtConnect = true
                connectSuccess = true
                //Cambio de imagen
                imageBlue!!.setImageResource(R.drawable.ic_bluetooth)
                //Mesanje de salida
                Toast.makeText(this@MainActivity, "Conectado", Toast.LENGTH_SHORT).show()
            }catch (e: Exception){
                Toast.makeText(this@MainActivity, "No se pudo conectar", Toast.LENGTH_SHORT).show()
                print(e.printStackTrace())
            }
        }else{ connectSuccess = false }
    }

    //Metodo para desconectar el bluethoot
    private fun disconectBT(){
        //Comprobamos que existe una conexion
        if(btSocket != null){
            //Cerramos la conexion
            btSocket?.close()
            //Asignamos el socket como nulo
            btSocket = null
            isBtConnect = false
            //Mandamos mensaje de salida
            Toast.makeText(this@MainActivity,"Desconectado",Toast.LENGTH_SHORT).show()
            //CAmbiamos la imagen
            imageBlue!!.setImageResource(R.drawable.ic_bluetooth_disabled)
        }
    }

    //Metodo para mandar el mensaje
    private fun sendText(texto: String) {
        //Comprobamos que exista una conexion
        if (btSocket != null) {
            try {
                //Mandamos nuestro mensaje guardado como un arreglo de bytes
                btSocket?.outputStream?.write(texto.toByteArray())
                //Metodo para cambiar la imagen
                changeImage(texto)
            } catch (e: IOException) {
                print(e.printStackTrace())
            }
        }
    }

    private fun changeImage(texto: String){
        if(texto.equals("enciende las luces")) {
            foco_sala!!.setImageResource(R.drawable.foco_encendido)
            foco_cocina!!.setImageResource(R.drawable.foco_encendido)
        }else if(texto.equals("apaga las luces")){
            foco_sala!!.setImageResource(R.drawable.foco_apagado)
            foco_cocina!!.setImageResource(R.drawable.foco_apagado)
        }else if(texto.equals("enciende la luz de la sala")){
            foco_sala!!.setImageResource(R.drawable.foco_encendido)
        }else if(texto.equals("apaga la luz de la sala")){
            foco_sala!!.setImageResource(R.drawable.foco_apagado)
        }else if(texto.equals("enciende la luz de la cocina")){
            foco_cocina!!.setImageResource(R.drawable.foco_encendido)
        }else if(texto.equals("apaga la luz de la cocina")){
            foco_cocina!!.setImageResource(R.drawable.foco_apagado)
        }
    }

}
