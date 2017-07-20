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

package org.binave.common.util;

/**
 * 处理一些异常
 *
 * @author by bin jin on 2017/7/20.
 * @since 1.8
 */
public class ExceptionUtil {

    /**
     * 异常脱壳
     * todo 可能需要将中途的栈信息打印出来
     */
    public static RuntimeException unpackRuntimeException(Throwable e) {
        Throwable throwable = e.getCause();
        if (throwable == null) throwable = e;
        if (throwable instanceof RuntimeException) {
            if (throwable.getCause() == null) {
                return (RuntimeException) throwable;
            } else return unpackRuntimeException(throwable);
        } else return new RuntimeException(throwable);
    }

}
