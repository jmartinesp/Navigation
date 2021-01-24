package com.arasthel.navigation.screen_result

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.reflect.KClass

class ResultChannel<T>(
    ownerId: String,
    internal val resultType: Class<T>,
    private val onResult: (T) -> Unit
) {

    val id = ResultChannelId(
        ownerId = ownerId,
        resultId = resultType.name
    )

    fun consumeResult(result: T) {
        onResult.invoke(result)
    }

}

@Parcelize data class ResultChannelId(
    val ownerId: String,
    val resultId: String
): Parcelable

internal data class PendingResult(
    val resultChannelId: ResultChannelId,
    val resultType: KClass<out Any>,
    val result: Any
)