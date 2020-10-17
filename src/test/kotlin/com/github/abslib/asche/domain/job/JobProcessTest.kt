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

import org.junit.Test

class JobProcessTest {
    @Test
    fun testJobJson() {
        val job = Job(12L)
        job.taskId = 2
        job.groupId = 2
        job.type = "sa"
        job.state = "init"
        log.info { "job: ${job.serialize().decodeToString()}" }
    }
}