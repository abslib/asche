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

package com.github.abslib.asche.base.serialize

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import mu.KotlinLogging

internal val log = KotlinLogging.logger {}

interface SerializableData {
    fun serialize(): ByteArray
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
interface JsonSerializable : SerializableData {
    @JsonIgnore
    override fun serialize(): ByteArray {
        return toJson(this).encodeToByteArray()
    }

    companion object {
        @JvmStatic
        fun <T : JsonSerializable> deserialize(data: ByteArray): T {
            return fromJson(data.decodeToString(), object : TypeReference<T>() {})
        }
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
interface MsgpackSerializable : SerializableData {
    @JsonIgnore
    override fun serialize(): ByteArray {
        return toMsgpack(this)
    }

    companion object {
        @JvmStatic
        fun <T : MsgpackSerializable> deserialize(data: ByteArray): T {
            return fromMsgpack(data, object : TypeReference<T>() {})
        }
    }
}
