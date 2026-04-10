package com.curiosityengine.app.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Wraps a Flow<T> to emit Result<T>, catching exceptions as Result.failure */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map { Result.success(it) }
        .catch { emit(Result.failure(it)) }
