package com.leon.cool.lang.tree.compile.impl;

import com.leon.cool.lang.ast.AttrDef;
import com.leon.cool.lang.ast.ClassDef;
import com.leon.cool.lang.support.TreeSupport;
import com.leon.cool.lang.support.declaration.AttrDeclaration;
import com.leon.cool.lang.tree.compile.TreeScanner;

import static com.leon.cool.lang.support.ErrorSupport.error;
import static com.leon.cool.lang.support.ErrorSupport.errorPos;
import static com.leon.cool.lang.support.TypeSupport.isSelf;

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
public class AttrDefTreeScanner extends TreeScanner {
    private String className = null;

    public AttrDefTreeScanner(TreeSupport treeSupport) {
        super(treeSupport);
    }

    @Override
    public void applyClassDef(ClassDef classDef) {
        className = classDef.type.name;
        treeSupport.createAttrGraph(className);
        super.applyClassDef(classDef);
    }

    @Override
    public void applyAttrDef(AttrDef attrDef) {
        if (isSelf(attrDef.id)) {
            error("type.error.assign.self", errorPos(attrDef.id));
        }
        var attrDeclaration = new AttrDeclaration();
        attrDeclaration.id = attrDef.id.name;
        attrDeclaration.type = attrDef.type.name;
        attrDeclaration.expr = attrDef.expr;
        treeSupport.putToAttrGraph(className, attrDef.id.name, attrDeclaration);
        super.applyAttrDef(attrDef);
    }
}
