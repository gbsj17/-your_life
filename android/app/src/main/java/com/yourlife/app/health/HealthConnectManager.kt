package com.yourlife.app.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.yourlife.app.data.Session
import java.time.Instant
import java.time.ZoneId

/** Conexão com o Health Connect (a "Saúde" do Google): grava os treinos concluídos. */
class HealthConnectManager(private val context: Context) {

    /** Permissões pedidas ao usuário: só gravar sessões de exercício. */
    val permissions: Set<String> = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    val isAvailable: Boolean get() = status() == HealthConnectClient.SDK_AVAILABLE

    fun status(): Int = HealthConnectClient.getSdkStatus(context)

    suspend fun hasPermissions(): Boolean = try {
        HealthConnectClient.getOrCreate(context)
            .permissionController
            .getGrantedPermissions()
            .containsAll(permissions)
    } catch (e: Exception) {
        false
    }

    suspend fun write(session: Session): Boolean = try {
        val start = Instant.ofEpochMilli(session.startTs)
        var end = Instant.ofEpochMilli(session.ts)
        if (!end.isAfter(start)) end = start.plusSeconds(1)
        val zone = ZoneId.systemDefault().rules
        HealthConnectClient.getOrCreate(context).insertRecords(
            listOf(
                ExerciseSessionRecord(
                    startTime = start,
                    startZoneOffset = zone.getOffset(start),
                    endTime = end,
                    endZoneOffset = zone.getOffset(end),
                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS,
                    title = session.name
                )
            )
        )
        true
    } catch (e: Exception) {
        false
    }
}
