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

package com.github.abslib.asche.base.event

import com.github.abslib.asche.base.concurrent.CoroutinePool
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.lang.reflect.Type

class AsyncEventBus<E : BaseEvent>(private val coroutinePool: CoroutinePool) : EventBus<E> {
    private val listeners = mutableSetOf<EventListener<out E>>()

    override fun register(listener: EventListener<out E>) {
        listeners.add(listener)
    }

    override fun unregister(listener: EventListener<out E>) {
        listeners.remove(listener)
    }

    override fun compareType(event: E, eventType: Type): Boolean {
        return event.javaClass == eventType
    }

    @Suppress("UNCHECKED_CAST")
    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    override suspend fun send(event: E) {
        coroutinePool.submit {
            listeners.filter { compareType(event, it.eventType) }
                .forEach { (it as? EventListener<BaseEvent>)?.onEvent(event) }
        }
    }
}