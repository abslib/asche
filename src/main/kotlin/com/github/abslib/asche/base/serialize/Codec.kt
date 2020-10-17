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

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.msgpack.jackson.dataformat.MessagePackFactory

internal val jsonMapper = with(ObjectMapper().registerKotlinModule()) {
    enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature())
}
internal val msgpackMapper = ObjectMapper(MessagePackFactory()).registerKotlinModule()

fun toJson(value: Any): String {
    return jsonMapper.writeValueAsString(value)
}

fun <T> fromJson(value: String, cls: Class<T>): T {
    return jsonMapper.readValue(value, cls)
}

fun <T> fromJson(value: String, type: TypeReference<T>): T {
    return jsonMapper.readValue(value, type)
}

fun toMsgpack(value: Any): ByteArray {
    return msgpackMapper.writeValueAsBytes(value)
}

fun <T> fromMsgpack(bytes: ByteArray, cls: Class<T>): T {
    return msgpackMapper.readValue(bytes, cls)
}

fun <T> fromMsgpack(bytes: ByteArray, type: TypeReference<T>): T {
    return msgpackMapper.readValue(bytes, type)
}

fun str2JsonObject(value: String): JsonNode {
    return jsonMapper.readTree(value)
}