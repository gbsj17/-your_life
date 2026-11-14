package com.yourlife.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourlife.app.ui.theme.YourLifeTheme

/** Tela que o Health Connect abre para explicar como o app usa os dados de saúde. */
class RationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourLifeTheme(isSystemInDarkTheme()) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(Modifier.safeDrawingPadding().padding(24.dp)) {
                        Text("Como o Your Life usa seus dados", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "O app grava no Health Connect uma sessão de exercício (nome do treino, hora de início e fim) " +
                                "toda vez que você conclui um treino. Ele não lê nenhum dado de saúde seu e não envia nada " +
                                "para servidores: seu histórico fica salvo só neste aparelho e no Health Connect, " +
                                "onde você pode revogar a permissão quando quiser.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
