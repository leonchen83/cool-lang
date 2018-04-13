package com.leon.cool.lang.tree.compile.impl;

import com.leon.cool.lang.ast.ClassDef;
import com.leon.cool.lang.ast.MethodDef;
import com.leon.cool.lang.support.TreeSupport;
import com.leon.cool.lang.support.declaration.MethodDeclaration;
import com.leon.cool.lang.tree.compile.TreeScanner;

import java.util.stream.Collectors;

import static com.leon.cool.lang.support.ErrorSupport.error;
import static com.leon.cool.lang.support.ErrorSupport.errorPos;
import static com.leon.cool.lang.support.TypeSupport.isTypeDefined;

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
 * @author leon on 15-10-15
 */
public class MethodDefTreeScanner extends TreeScanner {
    private String className;

    public MethodDefTreeScanner(TreeSupport treeSupport) {
        super(treeSupport);
    }

    @Override
    public void applyClassDef(ClassDef classDef) {
        className = classDef.type.name;
        treeSupport.createMethodGraph(className);
        super.applyClassDef(classDef);
    }

    @Override
    public void applyMethodDef(MethodDef methodDef) {
        if (isTypeDefined(treeSupport.classGraph, methodDef.type)) {
            error("type.error.undefined", methodDef.type.name, errorPos(methodDef.type));
        }
        var methodDeclaration = new MethodDeclaration();
        methodDeclaration.methodName = methodDef.id.name;
        methodDeclaration.returnType = methodDef.type.name;
        methodDeclaration.paramTypes = methodDef.formals.stream().map(e -> {
            if (isTypeDefined(treeSupport.classGraph, e.type)) {
                error("type.error.undefined", e.type.name, errorPos(e.type));
            }
            return e.type.name;
        }).collect(Collectors.toList());
        methodDeclaration.declaration = methodDef;
        methodDeclaration.owner = className;
        treeSupport.putToMethodGraph(className, methodDeclaration);
        super.applyMethodDef(methodDef);
    }
}
