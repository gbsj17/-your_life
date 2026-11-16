package com.yourlife.app.data

/**
 * GIFs da ExerciseDB (versão gratuita), ligados à chave `anim` de cada exercício.
 * Exercício sem entrada aqui continua com o boneco desenhado. Para trocar um GIF errado,
 * mude só o id (o código depois de /media/ no endereço do GIF).
 */
object GifMap {
    private val ids = mapOf(
        "squat" to "6YUfHPL",     // quads (bodyweight squat)
        "lunge" to "iqH55N8",     // Forward Lunge
        "jack" to "1g5bPpA",      // Jack Jump
        "climber" to "RJgzwny",   // mountain climber
        "dip" to "9RT8oQW",       // bench dip on floor
        "bridge" to "u0cNiij",    // low glute bridge on floor
        "calf" to "bJYHBIN",      // bodyweight standing calf raise
        "jumpsquat" to "LIlE5Tn", // jump squat
        "bicycle" to "1ZFqTDN",   // Bicycle Crunch
        "flutter" to "UVo2Qs2"    // flutter kicks
    )

    fun url(anim: String): String? = ids[anim]?.let { "https://static.exercisedb.dev/media/$it.gif" }
}
