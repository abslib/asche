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

package com.github.abslib.asche.domain.job

import com.github.abslib.asche.base.process.EventSourcedProcess
import com.github.abslib.asche.domain.*
import mu.KotlinLogging
import java.sql.Timestamp
import kotlin.properties.Delegates

internal val log = KotlinLogging.logger {}

data class Job(override val id: Long) : DataModel {
    var groupId by Delegates.notNull<Long>()
    var taskId by Delegates.notNull<Long>()

    var state by Delegates.notNull<String>()

    var type by Delegates.notNull<String>()
    val context: String? = null

    var startTime: Timestamp? = null
    var pauseTime: Timestamp? = null
    var stopTime: Timestamp? = null
    var finishTime: Timestamp? = null

    var createTime: Timestamp? = null
    var updateTime: Timestamp? = null
}

interface JobProcess : DomainEntity<Job>, DomainAggregateRoot, EventSourcedProcess

interface JobRepository : DomainRepository<Job>

interface JobProcessFactory : DomainEntityFactory<Job, JobProcess>

interface JobService : DomainService<Job, JobProcess> {
    companion object {
        val ins: JobService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            object : JobService {
                override fun retrieveTarget(id: Long): JobProcess {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}

