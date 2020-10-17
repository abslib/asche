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

package com.github.abslib.asche.base.concurrent

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.Test
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

class CoroutinePoolTest {
    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @Test
    fun testPool() {
        val pool = CoroutinePool(1024)
        runBlocking {
            (1..1000).forEach { i ->
                val future = pool.submit(
                    suspend {
                        delay(1000)
                        if (i == 55) {
                            throw IllegalAccessException("error args")
                        }
                        log.info("ret: {}", i)
                    })
            }
            pool.close(30, TimeUnit.SECONDS)
        }
    }
}