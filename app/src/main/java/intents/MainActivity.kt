package intents

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.Intent.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.bes.prdm.intents.R
import br.edu.ifsp.scl.bes.prdm.intents.databinding.ActivityMainBinding
import intents.Extras.PARAMETER_EXTRA

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

        // Botão que envia o parâmetro via Intent implícita
        amb.parameterBt.setOnClickListener {
            val intent = Intent("OPEN_PARAMETER_ACTIVITY_ACTION").apply {
                putExtra(PARAMETER_EXTRA, amb.parameterTv.text.toString())
            }
            parameterArl.launch(intent)
        }

        // Recebe o resultado da ParameterActivity
        parameterArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.getStringExtra(PARAMETER_EXTRA)?.let {
                    amb.parameterTv.setText(it)  // Correção: setText para EditText
                }
            }
        }

        // Solicitação de permissão para CALL_PHONE
        cppArl = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                callPhone(true)
            } else {
                Toast.makeText(this, "Permissão necessária para realizar chamadas!", Toast.LENGTH_SHORT).show()
            }
        }

        // Seleção de imagem da galeria
        pickImageArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    startActivity(Intent(ACTION_VIEW, uri))
                }
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
                Toast.makeText(this, "Você clicou no Open", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.view_mi -> {
                val urlText = amb.parameterTv.text.toString()
                if (urlText.isNotBlank()) {
                    startActivity(browserIntent())
                } else {
                    Toast.makeText(this, "Digite uma URL válida!", Toast.LENGTH_SHORT).show()
                }
                true
            }

            R.id.call_mi -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(CALL_PHONE) == PERMISSION_GRANTED) {
                        callPhone(true)
                    } else {
                        cppArl.launch(CALL_PHONE)
                    }
                } else {
                    callPhone(true)
                }
                true
            }

            R.id.dial_mi -> {
                callPhone(false)
                true
            }

            R.id.pick_mi -> {
                val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                val pickImageIntent = Intent(ACTION_PICK).apply {
                    setDataAndType(Uri.parse(imageDir), "image/*")
                }
                pickImageArl.launch(pickImageIntent)
                true
            }

            R.id.chooser_mi -> {
                val chooserIntent = Intent(ACTION_CHOOSER).apply {
                    putExtra(EXTRA_TITLE, "Escolha seu navegador favorito")
                    putExtra(EXTRA_INTENT, browserIntent())
                }
                startActivity(chooserIntent)
                true
            }

            else -> false
        }
    }

    private fun callPhone(call: Boolean) {
        val number = amb.parameterTv.text.toString().trim()
        if (number.isBlank()) {
            Toast.makeText(this, "Número de telefone inválido!", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse("tel:$number")
        val callIntent = Intent(if (call) ACTION_CALL else ACTION_DIAL).apply {
            data = uri
        }
        startActivity(callIntent)
    }

    private fun browserIntent(): Intent {
        val urlText = amb.parameterTv.text.toString().trim()
        val uri = if (!urlText.startsWith("http")) {
            Uri.parse("https://$urlText")
        } else {
            Uri.parse(urlText)
        }
        return Intent(ACTION_VIEW, uri)
    }
}
