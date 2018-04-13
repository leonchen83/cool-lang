package com.leon.cool.lang.support.infrastructure;

import com.leon.cool.lang.ast.ClassDef;
import com.leon.cool.lang.ast.Feature;
import com.leon.cool.lang.factory.TreeFactory;
import com.leon.cool.lang.tokenizer.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.leon.cool.lang.glossary.TokenKind.ID;
import static com.leon.cool.lang.glossary.TokenKind.TYPE;

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
 * @author leon on 15-10-28
 */
public class ClassTable {
    private final TreeFactory f = new TreeFactory();

    /**
     * @return
     * @see com.leon.cool.lang.ast.StaticDispatch
     * @see com.leon.cool.lang.ast.Dispatch
     * <p>
     * 初始化build-in 类
     * Object:
     * abort():Object
     * type_name():String
     * copy():SELF_TYPE
     * IO:
     * out_string(String):SELF_TYPE
     * out_int(Int):SELF_TYPE
     * in_string():String
     * in_int():Int
     * Int:
     * val:Int
     * Bool:
     * val:Bool
     * String：
     * val:String
     * str_field:String
     * length():Int
     * concat(String):String
     * substr(Int,Int):String
     */
    @SuppressWarnings("unchecked")
    public List<ClassDef> builtInClasses() {
        var classDefs = new ArrayList<ClassDef>();
        var features = new ArrayList<Feature>();
        features.add(f.methodDef(new Token("abort", ID), Collections.EMPTY_LIST, new Token("Object", TYPE), f.noExpression()));
        features.add(f.methodDef(new Token("type_name", ID), Collections.EMPTY_LIST, new Token("String", TYPE), f.noExpression()));
        features.add(f.methodDef(new Token("copy", ID), Collections.EMPTY_LIST, new Token("SELF_TYPE", TYPE), f.noExpression()));
        ClassDef objectDef = f.classDef(new Token("Object", TYPE), Optional.empty(), features);
        classDefs.add(objectDef);

        features = new ArrayList<>();
        features.add(f.methodDef(new Token("out_string", ID), Collections.singletonList(f.formal(new Token("arg", ID), new Token("String", TYPE))), new Token("SELF_TYPE", TYPE), f.noExpression()));
        features.add(f.methodDef(new Token("out_int", ID), Collections.singletonList(f.formal(new Token("arg", ID), new Token("Int", TYPE))), new Token("SELF_TYPE", TYPE), f.noExpression()));
        features.add(f.methodDef(new Token("in_string", ID), Collections.EMPTY_LIST, new Token("String", TYPE), f.noExpression()));
        features.add(f.methodDef(new Token("in_int", ID), Collections.EMPTY_LIST, new Token("Int", TYPE), f.noExpression()));
        ClassDef ioDef = f.classDef(new Token("IO", TYPE), Optional.of(new Token("Object", TYPE)), features);
        classDefs.add(ioDef);

        features = new ArrayList<>();
        features.add(f.attrDef(new Token("val", ID), new Token("Int", TYPE), Optional.empty()));
        ClassDef intDef = f.classDef(new Token("Int", TYPE), Optional.of(new Token("Object", TYPE)), features);
        classDefs.add(intDef);

        features = new ArrayList<>();
        features.add(f.attrDef(new Token("val", ID), new Token("Bool", TYPE), Optional.empty()));
        ClassDef boolDef = f.classDef(new Token("Bool", TYPE), Optional.of(new Token("Object", TYPE)), features);
        classDefs.add(boolDef);

        features = new ArrayList<>();
        features.add(f.attrDef(new Token("val", ID), new Token("Int", TYPE), Optional.empty()));
        features.add(f.attrDef(new Token("str_field", ID), new Token("String", TYPE), Optional.empty()));
        features.add(f.methodDef(new Token("length", ID), Collections.EMPTY_LIST, new Token("Int", TYPE), f.noExpression()));
        features.add(f.methodDef(new Token("concat", ID), Collections.singletonList(f.formal(new Token("arg", ID), new Token("String", TYPE))), new Token("String", TYPE), f.noExpression()));
        features.add(f.methodDef(new Token("substr", ID), Arrays.asList(f.formal(new Token("arg", ID), new Token("Int", TYPE)), f.formal(new Token("arg2", ID), new Token("Int", TYPE))), new Token("String", TYPE), f.noExpression()));
        ClassDef stringDef = f.classDef(new Token("String", TYPE), Optional.of(new Token("Object", TYPE)), features);
        classDefs.add(stringDef);
        return classDefs;
    }
}
