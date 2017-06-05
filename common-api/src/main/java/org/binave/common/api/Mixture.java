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

import java.util.Arrays;
import java.util.Objects;

/**
 * 混合多个参数
 * 多个参数 {@link #<By>} 共同映射一个 #Target
 *
 * @author by bin jin on 2017/6/5.
 * @since 1.8
 */
public class Mixture<By> {

    private int hash;

    private By[] by;

    @SafeVarargs
    public Mixture(By... by) {
        if (by == null || by.length == 0)
            throw new IllegalArgumentException();
        this.by = by;
        if (by.length > 1) {
            for (By b : by) this.hash += b.hashCode();
        } else this.hash = by[0].hashCode();
    }

    public By[] get() {
        return this.by;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    /**
     * todo 暂时不考虑多个 classloader 导致的 equals 匹配问题
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Mixture) {
            try {
                Mixture m = (Mixture) o;
                if (m.by.length != this.by.length) return false;
                if (this.by.length > 1) {
                    for (int i = 0; i < this.by.length; i++)
                        if (!Objects.equals(m.by[i], this.by[i])) return false;
                    return true;
                } else return Objects.equals(m.by[0], this.by[0]);
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Mixture{" +
                "by=" + Arrays.toString(by) +
                '}';
    }
}
