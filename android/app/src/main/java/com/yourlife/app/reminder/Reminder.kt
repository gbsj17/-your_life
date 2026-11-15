package com.yourlife.app.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yourlife.app.MainActivity
import com.yourlife.app.R
import com.yourlife.app.data.Programs
import com.yourlife.app.data.Store
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Lembrete diário de treino: um alarme por vez, reagendado depois de cada disparo. */
object Reminder {
    private const val CHANNEL = "lembrete"

    private fun pending(c: Context) = PendingIntent.getBroadcast(
        c, 1, Intent(c, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    /** Agenda (ou cancela) o próximo lembrete conforme os ajustes salvos. */
    fun schedule(context: Context) {
        val s = Store(context).settings()
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = pending(context)
        am.cancel(pi)
        if (!s.reminderOn) return
        val now = LocalDateTime.now()
        var at = now.toLocalDate().atTime(s.reminderMinutes / 60, s.reminderMinutes % 60)
        if (!at.isAfter(now)) at = at.plusDays(1)
        // alarme aproximado: o Android pode atrasar alguns minutos para poupar bateria
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pi)
    }

    fun notifyNow(context: Context) {
        val store = Store(context)
        if (!store.settings().reminderOn) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        // já treinou hoje: não incomoda
        if (store.history().any { it.date == LocalDate.now().toString() }) return

        val plan = store.enrollment()?.let { Programs.status(it) }?.takeIf { !it.finished }?.todayPlan
        val text = when {
            plan != null -> "Hoje: ${plan.name}. Bora treinar?"
            store.enrollment() != null -> "Hoje é dia de descanso no seu plano, mas um treino leve também vale."
            else -> "Hora do seu treino."
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Lembrete de treino", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context, 2, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_dumbbell)
            .setContentTitle("Your Life")
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1, n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminder.notifyNow(context)
        Reminder.schedule(context)
    }
}

/** Reagenda o lembrete depois de reiniciar o aparelho ou atualizar o app (os alarmes são apagados). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminder.schedule(context)
    }
}
