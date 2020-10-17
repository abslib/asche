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

import com.github.abslib.asche.base.event.MailBox
import com.github.abslib.asche.base.process.ProcessEvent
import com.github.abslib.asche.base.process.ProcessState
import com.github.abslib.asche.base.process.TriggeredEventProcess

internal abstract class AbstractJobProcess(
    override val data: Job,
    mailBox: MailBox<ProcessEvent>
) : JobProcess, TriggeredEventProcess(ProcessState.valueOf(data.state), mailBox) {
    lateinit var jobRepository: JobRepository

    override fun save() {
        jobRepository.save(this.data)
    }
}