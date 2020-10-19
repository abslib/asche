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

package com.github.abslib.asche.service.actuator

import com.github.abslib.asche.base.concurrent.CoroutinePool
import com.github.abslib.asche.domain.task.TaskService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@ObsoleteCoroutinesApi
internal class TaskProcessActuator : SingleThreadLoopActuator("FlowActuator") {
    private val taskService = TaskService.ins

    private val fetchLimit = 128
    private var fetchOffset = 0
    private val timeLimit = 500L

    private val coroutinePool = CoroutinePool(1024)

    @ExperimentalCoroutinesApi
    override suspend fun handle() {
        val task = taskService.pollActiveTaskProcess()
        if (task == null) {
            val scanRet = taskService.listLocalTaskIds(fetchOffset, fetchLimit)
            if (scanRet.isEmpty()) delay(timeLimit) else scanRet.forEach { taskService.activeTaskProcess(it) }
            fetchOffset += scanRet.size
        } else {
            coroutinePool.submit { task.trigger(timeLimit, TimeUnit.MILLISECONDS) }
        }
    }

    override suspend fun stop() {
        super.stop()
        coroutinePool.close(1, TimeUnit.MINUTES)
    }
}