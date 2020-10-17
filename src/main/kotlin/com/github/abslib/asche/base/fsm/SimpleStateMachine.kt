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

package com.github.abslib.asche.base.fsm

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SimpleStateMachine<S, A>(private var behavior: StateBehavior<S, A>) : StateMachine<S, A> {
    private val transitions: MutableMap<A, MutableList<StateTransition<S, A>>> = mutableMapOf()
    private val mutex = Mutex()
    override val state: S
        get() {
            return behavior.state
        }

    override fun defTransition(from: S, to: S, behavior: StateBehavior<S, A>, action: A): StateMachine<S, A> {
        if (!transitions.containsKey(action)) {
            transitions[action] = mutableListOf()
        }
        transitions[action]?.add(StateTransition(from, to, behavior, action))
        return this
    }

    override suspend fun act(action: A) {
        mutex.withLock {
            transitions[action]?.find { it.from?.equals(state) ?: false }?.also {
                behavior.onExit(action)
                behavior = it.behavior
                it.behavior.onEnter(state, action)
            } ?: behavior.onAction(action)
        }
    }
}