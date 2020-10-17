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

package com.github.abslib.asche.base.process

import com.github.abslib.asche.base.fsm.SimpleStateMachine
import com.github.abslib.asche.base.fsm.StateBehavior
import com.github.abslib.asche.base.fsm.StateMachine

abstract class BaseStateEventProcess(private val initState: ProcessState) : EventSourcedProcess {
    protected open val fsm: StateMachine<ProcessState, ProcessEvent> by lazy {
        SimpleStateMachine(behavior(initState))
            .defTransition(
                ProcessState.WAITING,
                ProcessState.RUNNING, behavior(ProcessState.RUNNING),
                ProcessCtlAction.START
            )
            .defTransition(
                ProcessState.SUSPENDED,
                ProcessState.RUNNING, behavior(ProcessState.RUNNING),
                ProcessCtlAction.RESUME
            )
            .defTransition(
                ProcessState.RUNNING,
                ProcessState.STOPPING, behavior(ProcessState.STOPPING),
                ProcessCtlAction.STOP
            )
            .defTransition(
                ProcessState.RUNNING,
                ProcessState.SUSPENDING, behavior(ProcessState.SUSPENDING),
                ProcessCtlAction.SUSPEND
            )
            .defTransition(
                ProcessState.RUNNING,
                ProcessState.COMPLETED, behavior(ProcessState.COMPLETED),
                ProcessCtlAction.FINISH
            )
            .defTransition(
                ProcessState.STOPPING,
                ProcessState.STOPPED, behavior(ProcessState.STOPPED),
                ProcessCtlAction.FINISH
            )
            .defTransition(
                ProcessState.SUSPENDING,
                ProcessState.SUSPENDED, behavior(ProcessState.SUSPENDED),
                ProcessCtlAction.FINISH
            )
    }

    val state: ProcessState
        get() = fsm.state

    protected abstract fun behavior(state: ProcessState): StateBehavior<ProcessState, ProcessEvent>

    override suspend fun send(event: ProcessEvent) {
        fsm.act(event)
    }

    override suspend fun resume() {
        send(ProcessCtlAction.RESUME)
    }

    override suspend fun start() {
        send(ProcessCtlAction.START)
    }

    override suspend fun stop() {
        send(ProcessCtlAction.STOP)
    }

    override suspend fun suspend() {
        send(ProcessCtlAction.SUSPEND)
    }

    override suspend fun finish() {
        send(ProcessCtlAction.FINISH)
    }
}