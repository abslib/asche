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

package com.github.abslib.asche.domain.task

import com.github.abslib.asche.base.process.EventSourcedProcess
import com.github.abslib.asche.base.process.TriggeredProcess
import com.github.abslib.asche.domain.*
import com.github.abslib.asche.domain.flow.FlowController
import mu.KotlinLogging
import java.sql.Timestamp
import kotlin.properties.Delegates

internal val log = KotlinLogging.logger {}

data class Task(override val id: Long) : DataModel {
    var state by Delegates.notNull<String>()

    var tenant by Delegates.notNull<String>()
    var creator by Delegates.notNull<String>()

    var startTime: Timestamp? = null
    var pauseTime: Timestamp? = null
    var stopTime: Timestamp? = null
    var finishTime: Timestamp? = null

    var createTime: Timestamp? = null
    var updateTime: Timestamp? = null
}

interface TaskProcess : DomainEntity<Task>, DomainAggregateRoot, EventSourcedProcess, TriggeredProcess {
    val flow: FlowController
}

interface TaskRepository : DomainRepository<Task>

interface TaskProcessFactory : DomainEntityFactory<Task, TaskProcess>

interface TaskService : DomainService<DomainAggregateRoot> {
    fun listLocalTaskIds(offset: Int, limit: Int): List<Long>
    fun pollActiveTaskProcess(): TaskProcess?
    fun activeTaskProcess(id: Long)

    companion object {
        val ins: TaskService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            object : TaskService {
                override fun listLocalTaskIds(offset: Int, limit: Int): List<Long> {
                    TODO("Not yet implemented")
                }

                override fun pollActiveTaskProcess(): TaskProcess {
                    TODO("Not yet implemented")
                }

                override fun activeTaskProcess(id: Long) {
                    TODO("Not yet implemented")
                }

                override fun retrieveAggregateRoot(id: Long): TaskProcess {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}