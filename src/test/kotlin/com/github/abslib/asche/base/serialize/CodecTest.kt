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

package com.github.abslib.asche.base.serialize

import com.github.abslib.asche.base.event.BaseEvent
import org.junit.Test

class CodecTest {
    data class TestEvent(val name: String) : MsgpackSerializable, BaseEvent

    @Test
    fun testJsonParser() {
        val e = TestEvent("test")
        val d = e.serialize()
        val e1: TestEvent = MsgpackSerializable.deserialize(d)
        log.info { "event: ${e1}" }
    }
}