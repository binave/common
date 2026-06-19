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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 类序列化工具工厂
 * 首先根据后缀尝试获得相应的实现 class，
 * 无果则生成并保存
 *
 * @author bin jin on 2017/4/19.
 * @since 1.8
 */
class FlatCodec implements Codec {

    /**
     * 缓存路径
     * todo 加入到 class path 中
     */
    private String cachePath = System.getProperty("java.io.tmpdir") + File.separator + ".flat_codec";

    // 对应序列化工具缓存
    private Map<Class, Codec> codecMap = new HashMap<>();

    @Override
    public <POJO> byte[] encode(POJO pojo) {
        if (pojo == null) return null;
        Class type = pojo.getClass();
        Codec codec = getCodec(type);
        return codec.encode(pojo);
    }

    @Override
    public <POJO> POJO decode(byte[] bytes, Class<POJO> type, Class<?>... generics) {
        if (bytes == null || type == null) return null;
        Codec codec = getCodec(type);
        return codec.decode(bytes, type);
    }

    @Override
    public <POJO> POJO copy(POJO pojo) {
        if (pojo == null) return null;
        return null;
    }

    /**
     * todo
     */
    private Codec getCodec(Class type) {
        Codec codec = codecMap.get(type);
        if (codec == null) {
            try {
                // 需要搞定 classloader，尝试加载已经生成过的 Codec 类
                Class<? extends Codec> codecType =
                        // todo 避免 classloader 不同造成影响
                        (Class<? extends Codec>) Class.forName(type.getName() + SUFFIX);
                codec = codecType.newInstance();
            } catch (ClassNotFoundException e) {
                // todo 没找到，进入动态生成逻辑，成功后尝试保存到对应路径，如果没有写权限，则放弃
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e); // new 对象出问题，几乎不会发生
            }
            codecMap.put(type, codec);
        }
        return codec;
    }
}
