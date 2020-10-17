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

package com.github.abslib.asche.domain.executor

import com.github.abslib.asche.base.event.EventDispatcher
import com.github.abslib.asche.domain.DataModel
import com.github.abslib.asche.domain.DomainService
import com.github.abslib.asche.domain.DomainTarget
import com.github.abslib.asche.domain.DomainTargetFactory
import mu.KotlinLogging
import java.sql.Timestamp
import kotlin.properties.Delegates

internal val log = KotlinLogging.logger {}

data class Executor(override val id: Long) : DataModel {
    val type by Delegates.notNull<String>()
    val tenant by Delegates.notNull<String>()

    var protocol: String? = null
    var address: String? = null
    var status by Delegates.notNull<String>()

    var createTime: Timestamp? = null
    var updateTime: Timestamp? = null
}

interface ExecutorClient : DomainTarget<Executor>, EventDispatcher<ExecEvent>

interface ExecutorClientFactory : DomainTargetFactory<Executor, ExecutorClient>

interface ExecutorService : DomainService<Executor, ExecutorClient> {
    companion object {
        val ins: ExecutorService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            object : ExecutorService {
                override fun retrieveTarget(id: Long): ExecutorClient {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}
