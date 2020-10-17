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

import mu.KotlinLogging

internal val log = KotlinLogging.logger {}

interface StateBehavior<S, A> {
    val state: S
    suspend fun onEnter(from: S, action: A)
    suspend fun onExit(action: A)
    suspend fun onAction(action: A)
}

internal data class StateTransition<S, A>(val from: S, val to: S, val behavior: StateBehavior<S, A>, val action: A)

interface StateMachine<S, A> {
    val state: S
    fun defTransition(from: S, to: S, behavior: StateBehavior<S, A>, action: A): StateMachine<S, A>
    suspend fun act(action: A)
}
