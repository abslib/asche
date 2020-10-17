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

import com.github.abslib.asche.base.process.ProcessEvent

sealed class TaskEvent : ProcessEvent
data class JobFinish(val jobId: Long) : TaskEvent()
data class JobUpdate(val jobId: Long, val event: ProcessEvent) : TaskEvent()