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

import java.util.Objects;

/**
 * 混合两个参数
 *
 * @author by bin jin on 2017/6/5.
 * @since 1.8
 */
public class Dual<Alpha, Beta> {

    private int hash;

    private Alpha alpha;
    private Beta beta;

    public Dual(Alpha alpha, Beta beta) {
        if (alpha == null || beta == null)
            throw new IllegalArgumentException();
        this.alpha = alpha;
        this.beta = beta;
        this.hash = alpha.hashCode() + beta.hashCode();
    }

    public Alpha getAlpha() {
        return this.alpha;
    }

    public Beta getBeta() {
        return this.beta;
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
        if (o instanceof Dual) {
            Dual m = (Dual) o;
            return Objects.equals(m.alpha, this.alpha) &&
                    Objects.equals(m.beta, this.beta);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Dual{" +
                ", alpha=" + alpha +
                ", beta=" + beta +
                '}';
    }
}
