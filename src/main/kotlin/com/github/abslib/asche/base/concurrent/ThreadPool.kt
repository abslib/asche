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

import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

fun createThreadPool(coreSize: Int, maxSize: Int, capacity: Int, name: String): ThreadPoolExecutor {
    return ThreadPoolExecutor(coreSize, maxSize, 10, TimeUnit.MINUTES,
        LinkedBlockingQueue(if (capacity > 0) capacity else Int.MAX_VALUE),
        ThreadFactoryBuilder()
            .setDaemon(true)
            .setPriority(Thread.NORM_PRIORITY)
            .setNameFormat("$name-%d")
            .setUncaughtExceptionHandler { _, e ->
                log.error { "Worker pool execute error: $e" }
            }.build(),
        { r, executor ->
            if (!executor.isShutdown) {
                try {
                    executor.queue.put(r)
                } catch (e: InterruptedException) {
                }
            }
        })
}