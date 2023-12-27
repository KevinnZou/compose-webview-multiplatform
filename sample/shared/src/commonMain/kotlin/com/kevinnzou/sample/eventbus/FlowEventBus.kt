package com.kevinnzou.sample.eventbus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Created By Kevin Zou On 2023/12/16
 */
object FlowEventBus {
    private val mEvents = MutableSharedFlow<IEvent>()
    val events = mEvents.asSharedFlow()

    suspend fun publishEvent(event: IEvent) {
        mEvents.emit(event)
    }
}
