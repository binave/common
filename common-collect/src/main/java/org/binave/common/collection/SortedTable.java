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

package org.binave.common.collection;

import com.google.common.collect.Table;

/**
 * {@link java.util.SortedMap} {@link com.google.common.collect.RowSortedTable}
 *
 * @author by bin jin on 2017/5/13.
 * @since 1.8
 */
public interface SortedTable<R, C, V> extends Table<R, C, V> {

    SortedTable<R, C, V> subRowTable(R fromKey, R toKey);

    SortedTable<R, C, V> headRowTable(R toKey);

    SortedTable<R, C, V> tailRowTable(R fromKey);

    R firstRowKey();

    R lastRowKey();

//    SortedTable<R, C, V> subColumnTable(R fromKey, R toKey);
//
//    SortedTable<R, C, V> headColumnTable(R toKey);
//
//    SortedTable<R, C, V> tailColumnTable(R fromKey);
//
//    R firstColumnKey();
//
//    R lastColumnKey();
}
