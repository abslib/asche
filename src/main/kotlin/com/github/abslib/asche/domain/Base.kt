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

package com.github.abslib.asche.domain

import com.github.abslib.asche.base.serialize.JsonSerializable

interface ValueObject : JsonSerializable

interface DataModel : ValueObject {
    val id: Long
}

interface DomainEntity<T : DataModel> {
    val data: T
    fun save()
}

interface DomainAggregateRoot {
    fun save()
}

interface DomainRepository<T : DataModel> {
    fun load(id: Long): T
    fun save(data: T)
}

interface DomainEntityFactory<M : DataModel, T : DomainEntity<M>> {
    fun create(id: Long): T
    fun create(data: M): T
}

interface DomainService<M : DataModel, T : DomainEntity<M>> {
    fun retrieveTarget(id: Long): T
}