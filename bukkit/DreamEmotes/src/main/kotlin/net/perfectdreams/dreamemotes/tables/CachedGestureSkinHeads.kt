package net.perfectdreams.dreamemotes.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object CachedGestureSkinHeads : LongIdTable() {
    val skinHash = text("hash").index()
    val signature = text("signature")
    val value = text("value")

    val generatedAt = timestampWithTimeZone("generated_at")
    val lastUsedAt = timestampWithTimeZone("last_used_at")

    init {
        uniqueIndex(skinHash)
    }
}