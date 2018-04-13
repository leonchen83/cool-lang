package com.leon.cool.lang.support.infrastructure;

import com.leon.cool.lang.object.CoolObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Copyright leon
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author leon on 16-3-13
 */
public class ObjectHeap {
    private static final Logger LOGGER = Logger.getLogger(ObjectHeap.class.getName());

    private Map<CoolObject, Boolean> heap = new HashMap<>();

    public void add(CoolObject obj) {
        heap.put(obj, false);
    }

    public void canReach(CoolObject obj) {
        if (heap.containsKey(obj)) {
            heap.put(obj, true);
        }
    }

    public boolean isReach(CoolObject obj) {
        if (heap.containsKey(obj)) {
            return heap.get(obj);
        }
        /**
         * 这里找不到必须返回true，如果返回false的话会有循环引用产生的死循环
         */
        return true;
    }

    /**
     * 删除不可达对象，并将可达对象重新设置成false
     */
    public void clearUnreachable() {
        LOGGER.info("*Mark-Sweep GC* total object size:" + heap.size());
        var sets = heap.entrySet().stream().filter(Map.Entry::getValue).collect(Collectors.toSet());
        LOGGER.info("*Mark-Sweep GC* reachable object size:" + sets.size());
        heap = new HashMap<>();
        sets.stream().forEach(e -> heap.put(e.getKey(), false));
    }

    public int size() {
        return heap.size();
    }
}
