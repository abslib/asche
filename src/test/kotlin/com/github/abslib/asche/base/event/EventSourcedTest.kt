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

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Test
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class EventSourcedTest {
    @Test
    fun testEventSourced() {
        class StringEvent(val name: String) : BaseEvent

        val es = object : EventSourcedTarget<StringEvent>(BlockingQueueMailBox(LinkedBlockingQueue())) {
            override fun onReceive(event: StringEvent) {
                when (event.name) {
                    "1" -> {
                        log.info { "receive event 1" }
                    }
                    "2" -> {
                        log.info { "receive event 2" }
                    }
                }
            }
        }
        runBlocking {
            launch {
                while (true) {
                    yield()
                    if (!es.receive()) {
                        delay(10)
                    }
                }
            }
            (1..100).forEach { _ -> es.send(StringEvent("2"), 100, TimeUnit.MILLISECONDS) }
        }
    }
}