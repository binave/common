/*
 * Copyright (c) 2017 bin jin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.binave.common.api;

/**
 * 同步代理
 * 用于一致性更替
 *
 * 实现时，需要同时实现、继承 {@link E}
 *
 * @author bin jin
 * @since 1.8
 */
public interface SyncProxy<E> {

    /**
     * 替换被代理对象
     */
    void syncUpdate(E e);

    /**
     * 测试被代理对象是否为 null
     */
    boolean isNull();

    /**
     * 获得实现类本身
     */
    default E getProxy() {
        return (E) this;
    }

}
