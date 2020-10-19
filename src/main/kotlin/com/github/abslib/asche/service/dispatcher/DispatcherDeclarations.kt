/*
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.abslib.asche.service.dispatcher

import com.github.abslib.asche.base.concurrent.CoroutinePool
import com.github.abslib.asche.base.event.*
import com.github.abslib.asche.base.process.ProcessEvent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.lang.reflect.Type

interface MailBoxFactory<E : BaseEvent> {
    fun create(id: Long): MailBox<E>
}

internal val processMailBoxFactory = ProcessMailBoxFactory()

data class TargetEvent<E : BaseEvent>(val targetId: Long, val event: E) : BaseEvent

@ObsoleteCoroutinesApi
internal val eventBus: EventBus<BaseEvent> = object : EventBus<BaseEvent> by AsyncEventBus(CoroutinePool(1024)) {
    override fun compareType(event: BaseEvent, eventType: Type): Boolean {
        return when (event) {
            is TargetEvent<*> -> event.event::class.java == eventType
            else -> event::class.java == eventType
        }
    }
}.also {
    it.register(object : EventListener<TargetEvent<ProcessEvent>>() {
        override suspend fun handle(event: TargetEvent<ProcessEvent>) {
            retrieveProcessMailBox(event.targetId).send(event.event)
        }
    })
}

@ObsoleteCoroutinesApi
suspend fun sendEvent(targetId: Long, event: BaseEvent) {
    sendEvent(TargetEvent(targetId, event))
}

@ObsoleteCoroutinesApi
suspend fun sendEvent(event: BaseEvent) {
    eventBus.send(event)
}

@ObsoleteCoroutinesApi
fun register(listener: EventListener<BaseEvent>) {
    eventBus.register(listener)
}

@ObsoleteCoroutinesApi
fun unregister(listener: EventListener<BaseEvent>) {
    eventBus.unregister(listener)
}

fun retrieveProcessMailBox(id: Long): MailBox<ProcessEvent> {
    return processMailBoxFactory.create(id)
}

