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

package com.github.abslib.asche.domain.flow

import com.github.abslib.asche.base.process.EventSourcedProcess
import com.github.abslib.asche.domain.*
import com.sun.xml.internal.fastinfoset.util.StringArray

enum class FlowType { DAG }

interface Flow : DataModel {
    val type: String
}

interface FlowController : DomainTarget<Flow>, EventSourcedProcess {
    fun addRelation(source: Long, target: Long)
    fun addRelations(relations: List<StringArray>)
    suspend fun onNodeFinish(id: Long)
}

interface FlowRepository : DomainRepository<Flow> {
    fun addRelation(flowId: Long, source: Long, target: Long)
    fun addRelations(flowId: Long, relations: List<StringArray>)

    fun getStartNode(flowId: Long): Long
    fun getEndNode(flowId: Long): Long

    fun activeSuccessors(flowId: Long, source: Long)
    fun getSuccessors(flowId: Long, source: Long): List<Long>
    fun getPredecessors(flowId: Long, target: Long): List<Long>

    fun hasRelation(flowId: Long, source: Long, target: Long)
}

interface FlowFactory : DomainTargetFactory<Flow, FlowController>

interface FlowService : DomainService<Flow, FlowController>
