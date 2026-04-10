package com.curiosityengine.app.sync

import com.curiosityengine.app.data.model.Streak

object ConflictResolver {

    /**
     * Last-write-wins: return the streak with the more recent updatedAt.
     * Falls back to remote if both are null or equal.
     *
     * [updatedAt] is a Unix epoch millis timestamp (Long). Zero is treated as absent.
     */
    fun resolveStreak(local: Streak, remote: Streak): Streak {
        val localTime = local.updatedAt.takeIf { it > 0L }
        val remoteTime = remote.updatedAt.takeIf { it > 0L }
        return when {
            localTime == null -> remote
            remoteTime == null -> local
            localTime > remoteTime -> local
            else -> remote
        }
    }
}
