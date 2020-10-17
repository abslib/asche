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

package com.github.abslib.asche.base.concurrent

import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

suspend fun tryInterval(interval: Long, timeout: Long, func: suspend (Long) -> Boolean): Boolean {
    var remain = timeout
    var ret = false
    while (remain > 0) {
        val st = System.currentTimeMillis()
        val ts = if (remain < interval) remain else interval
        try {
            ret = func(ts)
        } finally {
            val rt = ts - (System.currentTimeMillis() - st)
            if (!ret) {
                if (rt > 0) delay(rt) else yield()
            }
            remain -= ts
        }
    }
    return ret
}