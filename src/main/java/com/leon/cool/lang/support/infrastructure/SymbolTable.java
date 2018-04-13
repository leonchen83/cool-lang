package com.leon.cool.lang.support.infrastructure;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

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
 * @author leon on 15-10-11
 */
public class SymbolTable<T> {
    private final LinkedList<HashMap<String, T>> tbl;

    public SymbolTable() {
        tbl = new LinkedList<>();
    }

    public void enterScope() {
        tbl.push(new LinkedHashMap<>());
    }

    public void exitScope() {
        if (tbl.isEmpty()) {
            System.out.println("existScope: can't remove scope from an isEmpty symbol table.");
        }
        tbl.poll();
    }

    public void addId(String id, T info) {
        if (tbl.isEmpty()) {
            System.out.println("addId:" + id + ", can't add a symbol without a scope.");
        }
        tbl.peek().put(id, info);
    }

    public Optional<T> lookup(String sym) {
        if (tbl.isEmpty()) {
            System.out.println("lookup: no scope in symbol table.");
        }
        for (var i = 0; i < tbl.size(); i++) {
            T info = tbl.get(i).get(sym);
            if (info != null) return Optional.of(info);
        }
        return Optional.empty();
    }

    public void update(String sym, T obj) {
        if (tbl.isEmpty()) {
            System.out.println("lookup: no scope in symbol table.");
        }
        for (var i = 0; i < tbl.size(); i++) {
            if (tbl.get(i).containsKey(sym)) {
                tbl.get(i).put(sym, obj);
            }
        }
    }

    public HashMap<String, T> elementAt(int index) {
        return tbl.get(index);
    }

    public int size() {
        return tbl.size();
    }

    public Optional<Map<String, T>> topStack() {
        if (tbl.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(tbl.peek());
        }
    }

    public String toString() {
        String res = "";
        for (int i = tbl.size() - 1, j = 0; i >= 0; i--, j++) {
            res += "Scope " + j + ": " + tbl.get(i) + "\n";
        }
        return res;
    }
}




