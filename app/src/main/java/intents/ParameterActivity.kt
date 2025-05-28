package intents

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.bes.prdm.intents.databinding.ActivityParameterBinding
import intents.Extras.PARAMETER_EXTRA

class ParameterActivity : AppCompatActivity() {

    private val apb: ActivityParameterBinding by lazy {
        ActivityParameterBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(apb.root)
        setSupportActionBar(apb.toolbarIn.toolbar)
        supportActionBar?.subtitle = localClassName

        // Recebe o parâmetro enviado pela MainActivity
        intent.getStringExtra(PARAMETER_EXTRA)?.let {
            apb.parameterEt.setText(it)  // Correção: setText para EditText
        }

        // Quando clicar no botão, devolve o valor e fecha a Activity
        apb.returnAndCloseBt.setOnClickListener {
            Intent().apply {
                putExtra(PARAMETER_EXTRA, apb.parameterEt.text.toString())
                setResult(RESULT_OK, this)
            }
            finish()
        }
    }
}
