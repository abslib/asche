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

package com.github.abslib.asche.base.event

import java.util.concurrent.BlockingQueue

enum class Priority { HIGH, NORMAL, LOW }

interface PriorityEvent : BaseEvent {
    val priority: Priority
}

class PriorityBlockingQueueMailBox<E : PriorityEvent>(
    highPriorityQueue: BlockingQueue<E>,
    normalPriorityQueue: BlockingQueue<E>,
    lowPriorityQueue: BlockingQueue<E>
) : MultiMailBoxAdapter<E>(
    listOf(
        BlockingQueueMailBox(highPriorityQueue),
        BlockingQueueMailBox(normalPriorityQueue),
        BlockingQueueMailBox(lowPriorityQueue)
    )
) {
    override fun route(event: E): MailBox<E> {
        return when (event.priority) {
            Priority.HIGH -> mailBoxes[0]
            Priority.NORMAL -> mailBoxes[1]
            Priority.LOW -> mailBoxes[2]
        }
    }
}
