package com.leon.cool.lang.ast;

import com.leon.cool.lang.glossary.TokenKind;
import com.leon.cool.lang.object.CoolObject;
import com.leon.cool.lang.support.infrastructure.Context;
import com.leon.cool.lang.tokenizer.Token;
import com.leon.cool.lang.tree.compile.TreeVisitor;
import com.leon.cool.lang.tree.runtime.EvalTreeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.leon.cool.lang.Constant.MAIN_CLASS;
import static com.leon.cool.lang.Constant.MAIN_METHOD;

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
 * @author leon on 15-10-31
 */
public class Program extends TreeNode {
    public final List<ClassDef> classDef;

    public Program(List<ClassDef> classDef) {
        this.classDef = classDef;
    }

    @Override
    public String toString() {
        return "Program{" +
                "classDef=" + classDef +
                '}';
    }

    @Override
    public void accept(TreeVisitor visitor) {
        visitor.applyProgram(this);
    }

    @Override
    public CoolObject accept(EvalTreeVisitor visitor, Context context) {
        var staticDispatch = new StaticDispatch(new NewDef(new Token(MAIN_CLASS, TokenKind.TYPE)), Optional.empty(), new StaticDispatchBody(new Token(MAIN_METHOD, TokenKind.ID), new ArrayList<>()));
        return staticDispatch.accept(visitor, context);
    }
}
