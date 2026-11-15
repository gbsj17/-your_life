package com.yourlife.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.yourlife.app.AppViewModel
import com.yourlife.app.UpdateState
import com.yourlife.app.ui.components.BarProgress
import com.yourlife.app.ui.components.AppCard
import com.yourlife.app.ui.components.AppIcon
import com.yourlife.app.ui.components.IconBubble
import com.yourlife.app.ui.components.InfoChip
import com.yourlife.app.ui.components.PrimaryButton
import com.yourlife.app.ui.components.SecondaryButton
import com.yourlife.app.ui.components.SectionTitle
import com.yourlife.app.ui.icons.AppIcons

private const val HC_PACKAGE = "com.google.android.apps.healthdata"

@Composable
fun SettingsScreen(vm: AppViewModel, bottomPadding: PaddingValues) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted -> vm.onPermissionResult(granted) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp,
            bottom = bottomPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Ajustes", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 8.dp)) }

        item { SectionTitle("Aparência") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeOption("Sistema", AppIcons.Phone, vm.settings.theme == 0, Modifier.weight(1f)) { vm.setTheme(0) }
                ThemeOption("Claro", AppIcons.Sun, vm.settings.theme == 1, Modifier.weight(1f)) { vm.setTheme(1) }
                ThemeOption("Escuro", AppIcons.Moon, vm.settings.theme == 2, Modifier.weight(1f)) { vm.setTheme(2) }
            }
        }

        item { SectionTitle("Saúde do Google", Modifier.padding(top = 8.dp)) }
        item {
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconBubble(AppIcons.Heart)
                        Column(Modifier.weight(1f)) {
                            Text("Health Connect", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Envie seus treinos concluídos para a Saúde do Google.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    when {
                        vm.hcStatus == HealthConnectClient.SDK_AVAILABLE && vm.hcGranted -> {
                            InfoChip("Conectado", icon = AppIcons.Check, highlight = true)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Enviar automaticamente ao concluir um treino",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(checked = vm.settings.autoSync, onCheckedChange = { vm.setAutoSync(it) })
                            }
                            val pending = vm.pendingSync
                            SecondaryButton(
                                text = if (vm.syncing) "Enviando..." else if (pending > 0) "Sincronizar agora ($pending)" else "Sincronizar agora",
                                onClick = { vm.syncPending() },
                                modifier = Modifier.fillMaxWidth(),
                                icon = AppIcons.Sync
                            )
                            SecondaryButton(
                                text = "Gerenciar no Health Connect",
                                onClick = { openHealthSettings(context) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        vm.hcStatus == HealthConnectClient.SDK_AVAILABLE -> {
                            PrimaryButton(
                                "Conectar ao Health Connect",
                                onClick = { permissionLauncher.launch(vm.health.permissions) },
                                icon = AppIcons.Heart
                            )
                            Text(
                                "O app só pede permissão para gravar sessões de exercício. Ele não lê nenhum outro dado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        vm.hcStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                            Text(
                                "É preciso atualizar o Health Connect para continuar.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            PrimaryButton("Atualizar Health Connect", onClick = { openStore(context) })
                        }

                        else -> {
                            Text(
                                "O Health Connect não foi encontrado neste aparelho. Instale-o pela Play Store.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            PrimaryButton("Instalar Health Connect", onClick = { openStore(context) })
                        }
                    }

                    vm.syncMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item { SectionTitle("Sobre", Modifier.padding(top = 8.dp)) }
        item {
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Your Life", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Versão ${versionName(context)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Seu histórico fica salvo só neste aparelho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { SectionTitle("Atualização", Modifier.padding(top = 8.dp)) }
        item { UpdateCard(vm) }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) scheme.primaryContainer else scheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant)
    ) {
        Column(
            Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AppIcon(icon, tint = if (selected) scheme.primary else scheme.onSurfaceVariant)
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) scheme.onPrimaryContainer else scheme.onSurface
            )
        }
    }
}

private fun versionName(context: Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
} catch (e: Exception) {
    "1.0"
}

private fun openHealthSettings(context: Context) {
    try {
        context.startActivity(Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        openStore(context)
    }
}

private fun openStore(context: Context) {
    val market = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$HC_PACKAGE&url=healthconnect%3A%2F%2Fonboarding")
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(market)
    } catch (e: Exception) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$HC_PACKAGE"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
        }
    }
}

@Composable
private fun UpdateCard(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (val u = vm.update) {
                UpdateState.Idle, UpdateState.UpToDate -> {
                    Text(
                        if (u == UpdateState.UpToDate) "Você está na versão mais recente." else "Procure por novas versões do app.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    SecondaryButton("Verificar agora", { vm.checkUpdate() }, Modifier.fillMaxWidth())
                }
                UpdateState.Checking -> Text("Verificando…", style = MaterialTheme.typography.bodyMedium)
                is UpdateState.Available -> {
                    Text("Nova versão disponível: ${u.info.versionName}", style = MaterialTheme.typography.titleMedium)
                    if (u.info.sizeBytes > 0) {
                        Text(
                            "Tamanho: %.1f MB".format(u.info.sizeBytes / 1048576f),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    PrimaryButton("Baixar atualização", { vm.downloadUpdate() })
                }
                is UpdateState.Downloading -> {
                    Text("Baixando ${(u.progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                    BarProgress(u.progress)
                }
                is UpdateState.Ready -> {
                    Text("Download concluído (${u.info.versionName}).", style = MaterialTheme.typography.titleMedium)
                    if (!vm.installAllowed) {
                        Text(
                            "Para instalar, o Android pede que você permita este app a instalar atualizações. Autorize e volte aqui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                        PrimaryButton("Autorizar instalação", { vm.requestInstallPermission() })
                    } else {
                        PrimaryButton("Instalar agora", { vm.installUpdate() })
                    }
                }
                is UpdateState.Failed -> {
                    Text(u.message, style = MaterialTheme.typography.bodyMedium, color = scheme.error)
                    SecondaryButton("Tentar de novo", { vm.checkUpdate() }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}
