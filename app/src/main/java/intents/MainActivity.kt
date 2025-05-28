package br.edu.ifsp.scl.bes.prdm.intents

import android.Manifest.permission.CALL_PHONE
import android.app.ComponentCaller
import android.content.Intent
import android.content.Intent.ACTION_CALL
import android.content.Intent.ACTION_CHOOSER
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_PICK
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_INTENT
import android.content.Intent.EXTRA_TITLE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.ifsp.scl.bes.prdm.intents.Extras.PARAMETER_EXTRA
import br.edu.ifsp.scl.bes.prdm.intents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var parameterArl: ActivityResultLauncher<Intent>
    private lateinit var cppArl: ActivityResultLauncher<String>
    private lateinit var pickImageArl: ActivityResultLauncher<Intent>

    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)
        setSupportActionBar(amb.toolbarIn.toolbar)
        supportActionBar?.subtitle = localClassName

        amb.parameterBt.setOnClickListener {
            // Intent implícita porque não define a classe que será executada para tratar a Intent
            // Deixa a cargo do SO escolher a Activity com base no IntentFilter
            Intent("OPEN_PARAMETER_ACTIVITY_ACTION").let {
                // Colocando o valor na Intent que será enviada para a ParameterActivity
                it.putExtra(PARAMETER_EXTRA, amb.parameterTv.text.toString())
                parameterArl.launch(it)
            }
        }

        parameterArl =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Recebendo o valor devolvido pela ParameterActivity
                    result.data?.getStringExtra(PARAMETER_EXTRA).let {
                        amb.parameterTv.text = it
                    }
                }
            }

        cppArl =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
                if (permissionGranted) {
                    // Chamar o número
                    callPhone(true)
                } else {
                    Toast.makeText(
                        this,
                        "Permission required to call a number!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        pickImageArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Recebendo URI do arquivo selecionado e abrindo em outro aplicativo para visualização (galeria)
            if (result.resultCode == RESULT_OK) {
                startActivity(Intent(ACTION_VIEW, result.data?.data))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open_activity_mi -> {
                Toast.makeText(this, "vc clicou no open", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.view_mi -> {
                startActivity(browserIntent())
                true
            }

            R.id.call_mi -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(CALL_PHONE) == PERMISSION_GRANTED) {
                        callPhone(true)
                    }
                    else {
                        // Solicitar a permissão para o usuário
                        cppArl.launch(CALL_PHONE)
                    }
                }
                else {
                    callPhone(true)
                }
                true
            }

            R.id.dial_mi -> {
                callPhone(false)
                true
            }

            R.id.pick_mi -> {
                // Chamando um aplicativo para selecionar uma imagem do diretório público de imagens
                val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                val pickImageIntent = Intent(ACTION_PICK)
                pickImageIntent.setDataAndType(Uri.parse(imageDir), "image/*")
                pickImageArl.launch(pickImageIntent)
                true
            }

            R.id.chooser_mi -> {
                val chooserIntent = Intent(ACTION_CHOOSER)
                chooserIntent.putExtra(EXTRA_TITLE, "Choose your favorite navigator")
                chooserIntent.putExtra(EXTRA_INTENT, browserIntent())
                startActivity(chooserIntent)
                true
            }

            else -> {
                false
            }
        }
    }

    private fun callPhone(call: Boolean) {
        val number = "tel: ${amb.parameterTv.text}"
        val callIntent = Intent(if (call) ACTION_CALL else ACTION_DIAL)
        callIntent.data = Uri.parse(number)
        startActivity(callIntent)
    }

    private fun browserIntent(): Intent {
        val url = Uri.parse(amb.parameterTv.text.toString())
        return Intent(ACTION_VIEW, url)
    }
}