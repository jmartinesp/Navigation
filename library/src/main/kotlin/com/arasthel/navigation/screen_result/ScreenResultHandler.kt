package com.arasthel.navigation.screen_result

import com.arasthel.navigation.screen.ScreenResult

class ScreenResultHandler {

    private val resultChannels = mutableMapOf<ResultChannelId, ResultChannel<*>>()
    private val pendingResults = mutableMapOf<ResultChannelId, PendingResult>()

    fun <T: ScreenResult> registerResultListener(id: String, resultClass: Class<T>, listener: (T) -> Unit): ResultChannelId {
        val channel = ResultChannel(id, resultClass, listener)
        resultChannels[channel.id] = channel

        val pendingResult = pendingResults[channel.id]
        if (pendingResult != null) {
            channel.consumeResult(pendingResult.result as T)
            pendingResults.remove(channel.id)
        }

        return channel.id
    }

    fun unregisterResultListener(id: ResultChannelId) {
        resultChannels.remove(id)
    }

    fun <T: ScreenResult> publishResultToChannels(result: T, screenId: String) {
        val resultChannelId = ResultChannelId(ownerId = screenId, resultId = result::class.java.name)
        val registeredChannel = (resultChannels[resultChannelId] as? ResultChannel<T>)
        if (registeredChannel != null) {
            registeredChannel.consumeResult(result)
            unregisterResultListener(resultChannelId)
        } else {
            pendingResults[resultChannelId] = PendingResult(resultChannelId, result::class, result)
        }
    }

}