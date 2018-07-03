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

package org.binave.common.serialize;

/**
 * POJO 序列化（编码），反序列化（解码）
 * 需要 pojo 有无参构造
 * 属性需要有公有权限的 get set 方法
 *
 * @author bin jin on 2017/4/18.
 * @since 1.8
 */
public interface Codec {

    // 缓存后缀
    String SUFFIX = "$codec";

    /**
     * 序列化
     *
     * @param pojo 对象实例
     */
    <POJO> byte[] encode(POJO pojo);

    /**
     * 反序列化
     *
     * @param type      目标类型
     * @param generics  泛型类型
     */
    <POJO> POJO decode(byte[] bytes, Class<POJO> type, Class<?>... generics);

    /**
     * 对象复制
     */
    <POJO> POJO copy(POJO pojo);

}
