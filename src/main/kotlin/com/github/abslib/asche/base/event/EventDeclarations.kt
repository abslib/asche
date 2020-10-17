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

import mu.KotlinLogging
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal val log = KotlinLogging.logger {}

interface BaseEvent

interface EventDispatcher<E : BaseEvent> {
    suspend fun send(event: E)
}

abstract class EventListener<E : BaseEvent> {
    val eventType: Class<*>
        get() {
            val genType: Type = this::class.java.genericSuperclass
            if (genType !is ParameterizedType) {
                return Any::class.java
            }
            val params: Array<Type> = genType.actualTypeArguments
            if (params[0] !is ParameterizedType) {
                return Any::class.java
            }
            return (params[0] as ParameterizedType).rawType as Class<*>
        }

    abstract suspend fun handle(event: E)
}

internal suspend inline fun EventListener<BaseEvent>.onEvent(event: BaseEvent) {
    if (event::class.java == this.eventType) {
        this.handle(event)
    }
}

interface EventBus<E : BaseEvent> : EventDispatcher<E> {
    fun register(listener: EventListener<out E>)
    fun unregister(listener: EventListener<out E>)
    fun compareType(event: E, eventType: Type): Boolean
}

interface MailBox<E : BaseEvent> : EventDispatcher<E> {
    suspend fun receive(): E?
    suspend fun close()
}

abstract class MultiMailBoxAdapter<E : BaseEvent>(protected val mailBoxes: List<MailBox<E>>) : MailBox<E> {
    override suspend fun send(event: E) {
        route(event).send(event)
    }

    protected abstract fun route(event: E): MailBox<E>

    override suspend fun receive(): E? {
        var ret: E? = null
        for (q in mailBoxes) {
            ret = q.receive()
            if (ret != null) break
        }
        return ret
    }

    override suspend fun close() {
        mailBoxes.forEach { it.close() }
    }
}

abstract class EventSourcedTarget<E : BaseEvent>(private val mailBox: MailBox<E>) : EventDispatcher<E> {
    override suspend fun send(event: E) {
        mailBox.send(event)
    }

    suspend fun receive(): Boolean {
        val event = mailBox.receive()
        if (event != null) {
            try {
                onReceive(event)
                return true
            } catch (e: Exception) {
                uncaughtExceptionHandler(event, e)
            }
        }
        return false
    }

    suspend fun close() {
        mailBox.close()
    }

    protected abstract suspend fun onReceive(event: E)

    protected open fun uncaughtExceptionHandler(event: E, error: Exception) {
        log.error { "handle event: $event, exception: $error" }
    }
}