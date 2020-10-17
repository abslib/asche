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
import org.junit.Test

private val log = KotlinLogging.logger {}

class FsmTest {
    @Test
    fun testFsmTransition() {
        val sm = SimpleStateMachine(object : StateBehavior<String, String> {
            override val state: String
                get() = "init"

            override fun onEnter(from: String, action: String) {
                log.info("transition from {} to {} by act: {}", from, state, action)
            }

            override fun onAction(action: String) {
                log.info("on action: {}", action)
            }

            override fun onExit(action: String) {
            }

        })
        sm.defTransition("init", "run", object : StateBehavior<String, String> {
            override val state: String
                get() = "run"

            override fun onEnter(from: String, action: String) {
                log.info("transition from {} to {} by act: {}", from, state, action)
            }

            override fun onAction(action: String) {
                log.info("on action: {}", action)
            }

            override fun onExit(action: String) {
            }
        }, "run")
        sm.act("run")
        log.info("current state: {}", sm.state)
        sm.act("doSomething")
    }
}