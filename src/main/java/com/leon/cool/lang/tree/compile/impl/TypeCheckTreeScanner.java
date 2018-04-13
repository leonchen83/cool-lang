package com.leon.cool.lang.tree.compile.impl;

import com.leon.cool.lang.Constant;
import com.leon.cool.lang.ast.Assign;
import com.leon.cool.lang.ast.AttrDef;
import com.leon.cool.lang.ast.Blocks;
import com.leon.cool.lang.ast.BoolConst;
import com.leon.cool.lang.ast.Branch;
import com.leon.cool.lang.ast.CaseDef;
import com.leon.cool.lang.ast.ClassDef;
import com.leon.cool.lang.ast.Comp;
import com.leon.cool.lang.ast.Cond;
import com.leon.cool.lang.ast.Dispatch;
import com.leon.cool.lang.ast.Divide;
import com.leon.cool.lang.ast.Formal;
import com.leon.cool.lang.ast.IdConst;
import com.leon.cool.lang.ast.IntConst;
import com.leon.cool.lang.ast.IsVoid;
import com.leon.cool.lang.ast.Let;
import com.leon.cool.lang.ast.LetAttrDef;
import com.leon.cool.lang.ast.Loop;
import com.leon.cool.lang.ast.Lt;
import com.leon.cool.lang.ast.LtEq;
import com.leon.cool.lang.ast.MethodDef;
import com.leon.cool.lang.ast.Mul;
import com.leon.cool.lang.ast.Neg;
import com.leon.cool.lang.ast.NewDef;
import com.leon.cool.lang.ast.NoExpression;
import com.leon.cool.lang.ast.Not;
import com.leon.cool.lang.ast.Paren;
import com.leon.cool.lang.ast.Plus;
import com.leon.cool.lang.ast.Program;
import com.leon.cool.lang.ast.StaticDispatch;
import com.leon.cool.lang.ast.StaticDispatchBody;
import com.leon.cool.lang.ast.StringConst;
import com.leon.cool.lang.ast.Sub;
import com.leon.cool.lang.support.TreeSupport;
import com.leon.cool.lang.tokenizer.Token;
import com.leon.cool.lang.tree.compile.TreeScanner;
import com.leon.cool.lang.type.TypeEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.leon.cool.lang.factory.TypeFactory.booleanType;
import static com.leon.cool.lang.factory.TypeFactory.integerType;
import static com.leon.cool.lang.factory.TypeFactory.noType;
import static com.leon.cool.lang.factory.TypeFactory.objectType;
import static com.leon.cool.lang.factory.TypeFactory.stringType;
import static com.leon.cool.lang.support.ErrorSupport.errorMsg;
import static com.leon.cool.lang.support.ErrorSupport.errorPos;
import static com.leon.cool.lang.support.TypeSupport.isBasicType;
import static com.leon.cool.lang.support.TypeSupport.isParent;
import static com.leon.cool.lang.support.TypeSupport.isSelf;
import static com.leon.cool.lang.support.TypeSupport.isSelfType;
import static com.leon.cool.lang.support.TypeSupport.isTypeDefined;
import static com.leon.cool.lang.util.StringUtil.constructMethod;

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
public class TypeCheckTreeScanner extends TreeScanner {

    private String className = null;

    public final List<String> errMsgs = new ArrayList<>();

    public TypeCheckTreeScanner(TreeSupport treeSupport) {
        super(treeSupport);
    }

    @Override
    public void applyProgram(Program program) {
        super.applyProgram(program);
    }

    @Override
    public void applyClassDef(ClassDef classDef) {
        className = classDef.type.name;
        super.applyClassDef(classDef);
        treeSupport.lookupSymbolTable(className).exitScope();
    }

    @Override
    public void applyFormal(Formal formal) {
        super.applyFormal(formal);
    }

    @Override
    public void applyStaticDispatch(StaticDispatch staticDispatch) {
        super.applyStaticDispatch(staticDispatch);
        var paramsTypes = staticDispatch.dispatch.params.stream().map(e -> e.typeInfo).collect(Collectors.toList());
        if (!staticDispatch.type.isPresent()) {
            var methodDeclaration = treeSupport.lookupMethodDeclaration(staticDispatch.expr.typeInfo.replace().className(), staticDispatch.dispatch.id.name, paramsTypes);
            if (!methodDeclaration.isPresent()) {
                var method = constructMethod(staticDispatch.dispatch.id.name, paramsTypes.stream().map(Object::toString).collect(Collectors.toList()));
                reportTypeCheckError("type.error.method.undefined", staticDispatch.expr.typeInfo.replace().className(), method, errorPos(staticDispatch.dispatch.id));
                staticDispatch.typeInfo = noType();
            } else {
                if (isSelfType(methodDeclaration.get().returnType)) {
                    staticDispatch.typeInfo = staticDispatch.expr.typeInfo;
                } else {
                    staticDispatch.typeInfo = objectType(methodDeclaration.get().returnType, className);
                }
            }
        } else {
            var type = objectType(staticDispatch.type.get().name, className);
            if (!isParent(treeSupport.classGraph, staticDispatch.expr.typeInfo, type)) {
                reportTypeCheckError("type.error.subclass", staticDispatch.expr.typeInfo.toString(), type.toString(), errorPos(staticDispatch.type.get()));
                staticDispatch.typeInfo = noType();
            } else {
                var methodDeclaration = treeSupport.lookupMethodDeclaration(type.className(), staticDispatch.dispatch.id.name, paramsTypes);
                if (!methodDeclaration.isPresent()) {
                    var method = constructMethod(staticDispatch.dispatch.id.name, paramsTypes.stream().map(Object::toString).collect(Collectors.toList()));
                    reportTypeCheckError("type.error.method.undefined", type.className(), method, errorPos(staticDispatch.dispatch.id));
                    staticDispatch.typeInfo = noType();
                } else {
                    if (isSelfType(methodDeclaration.get().returnType)) {
                        staticDispatch.typeInfo = staticDispatch.expr.typeInfo;
                    } else {
                        staticDispatch.typeInfo = objectType(methodDeclaration.get().returnType, className);
                    }
                }
            }
        }
    }

    @Override
    public void applyStaticDispatchBody(StaticDispatchBody staticDispatchBody) {
        super.applyStaticDispatchBody(staticDispatchBody);
    }

    @Override
    public void applyDispatch(Dispatch dispatch) {
        super.applyDispatch(dispatch);
        var paramsTypes = dispatch.params.stream().map(e -> e.typeInfo).collect(Collectors.toList());
        var methodDeclaration = treeSupport.lookupMethodDeclaration(className, dispatch.id.name, paramsTypes);
        if (!methodDeclaration.isPresent()) {
            var method = constructMethod(dispatch.id.name, paramsTypes.stream().map(Object::toString).collect(Collectors.toList()));
            reportTypeCheckError("type.error.method.undefined", className, method, errorPos(dispatch.id));
            dispatch.typeInfo = noType();
        } else {
            dispatch.typeInfo = objectType(methodDeclaration.get().returnType, className);
        }
    }

    @Override
    public void applyCaseDef(CaseDef caseDef) {
        var size = caseDef.branchList.stream().map(e -> {
            if (isSelf(e.id)) {
                reportTypeCheckError("type.error.bind.self", errorPos(e.id));
            }
            return e.type.name;
        }).collect(Collectors.toSet()).size();
        if (caseDef.branchList.size() != size) {
            reportTypeCheckError("type.error.case.distinct", errorPos(caseDef));
            caseDef.typeInfo = noType();
            super.applyCaseDef(caseDef);
        } else {
            super.applyCaseDef(caseDef);
            caseDef.typeInfo = treeSupport.lub(caseDef.branchList.stream().map(e -> e.typeInfo).collect(Collectors.toList()));
        }
    }

    @Override
    public void applyBranch(Branch branch) {
        treeSupport.lookupSymbolTable(className).enterScope();
        treeSupport.lookupSymbolTable(className).addId(branch.id.name, branch.type.name);
        super.applyBranch(branch);
        branch.typeInfo = branch.expr.typeInfo;
        treeSupport.lookupSymbolTable(className).exitScope();
    }

    @Override
    public void applyMethodDef(MethodDef methodDef) {
        treeSupport.lookupSymbolTable(className).enterScope();
        methodDef.formals.forEach(e -> {
            if (isSelf(e.id)) {
                reportTypeCheckError("type.error.assign.self", errorPos(e.id));
            } else {
                treeSupport.lookupSymbolTable(className).addId(e.id.name, e.type.name);
            }
        });
        super.applyMethodDef(methodDef);
        if (!isParent(treeSupport.classGraph, methodDef.expr.typeInfo, objectType(methodDef.type.name, className))) {
            reportTypeCheckError("type.error.subclass", methodDef.expr.typeInfo.toString(), objectType(methodDef.type.name, className).toString(), errorPos(methodDef.type));
        }
        treeSupport.lookupSymbolTable(className).exitScope();
    }

    @Override
    public void applyAttrDef(AttrDef attrDef) {
        super.applyAttrDef(attrDef);
        if (attrDef.expr.isPresent()) {
            var t0 = objectType(treeSupport.lookupSymbolTable(className).lookup(attrDef.id.name).get(), className);
            var t1 = attrDef.expr.get().typeInfo;
            if (!isParent(treeSupport.classGraph, t1, t0)) {
                reportTypeCheckError("type.error.subclass", t1.toString(), t0.toString(), errorPos(attrDef));
            }
        }
    }

    @Override
    public void applyLet(Let let) {
        treeSupport.lookupSymbolTable(className).enterScope();
        super.applyLet(let);
        let.typeInfo = let.expr.typeInfo;
        treeSupport.lookupSymbolTable(className).exitScope();
    }

    @Override
    public void applyLetAttrDef(LetAttrDef letAttrDef) {
        super.applyLetAttrDef(letAttrDef);
        if (isSelf(letAttrDef.id)) {
            reportTypeCheckError("type.error.bind.self", errorPos(letAttrDef.id));
        }
        if (letAttrDef.expr.isPresent()) {
            var t0 = objectType(letAttrDef.type.name, className);
            if (!isParent(treeSupport.classGraph, letAttrDef.expr.get().typeInfo, t0)) {
                reportTypeCheckError("type.error.subclass", letAttrDef.expr.get().typeInfo.toString(), t0.toString(), errorPos(letAttrDef));
            }
        }
        treeSupport.lookupSymbolTable(className).addId(letAttrDef.id.name, letAttrDef.type.name);
    }

    @Override
    public void applyAssign(Assign assign) {
        if (isSelf(assign.id.tok)) {
            reportTypeCheckError("type.error.assign.self", errorPos(assign.id));
        }
        super.applyAssign(assign);
        if (isParent(treeSupport.classGraph, assign.expr.typeInfo, assign.id.typeInfo)) {
            assign.typeInfo = assign.expr.typeInfo;
        } else {
            reportTypeCheckError("type.error.subclass", assign.expr.typeInfo.toString(), assign.id.typeInfo.toString(), errorPos(assign));
            assign.typeInfo = noType();
        }
    }

    @Override
    public void applyCond(Cond cond) {
        super.applyCond(cond);
        if (cond.condExpr.typeInfo.type() != TypeEnum.BOOL) {
            reportTypeCheckError("type.error.expected", Constant.BOOL, cond.condExpr.typeInfo.toString(), errorPos(cond.condExpr));
        }
        cond.typeInfo = treeSupport.lub(Arrays.asList(cond.thenExpr.typeInfo, cond.elseExpr.typeInfo));
    }

    @Override
    public void applyLoop(Loop loop) {
        super.applyLoop(loop);
        if (loop.condExpr.typeInfo.type() != TypeEnum.BOOL) {
            reportTypeCheckError("type.error.expected", Constant.BOOL, loop.condExpr.typeInfo.toString(), errorPos(loop.condExpr));
        }
        loop.typeInfo = objectType(Constant.OBJECT);
    }

    @Override
    public void applyBlocks(Blocks blocks) {
        super.applyBlocks(blocks);
        blocks.typeInfo = blocks.exprs.get(blocks.exprs.size() - 1).typeInfo;
    }

    @Override
    public void applyNewDef(NewDef newDef) {
        super.applyNewDef(newDef);
        newDef.typeInfo = objectType(newDef.type.name, className);
    }

    @Override
    public void applyIsVoid(IsVoid isVoid) {
        super.applyIsVoid(isVoid);
        isVoid.typeInfo = booleanType();
    }

    @Override
    public void applyPlus(Plus plus) {
        super.applyPlus(plus);
        if (plus.left.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, plus.left.typeInfo.toString(), errorPos(plus.left));
            plus.typeInfo = noType();
            return;
        }
        if (plus.right.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, plus.right.typeInfo.toString(), errorPos(plus.right));
            plus.typeInfo = noType();
            return;
        }
        plus.typeInfo = integerType();
    }

    @Override
    public void applySub(Sub sub) {
        super.applySub(sub);
        if (sub.left.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, sub.left.typeInfo.toString(), errorPos(sub.left));
            sub.typeInfo = noType();
            return;
        }
        if (sub.right.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, sub.right.typeInfo.toString(), errorPos(sub.right));
            sub.typeInfo = noType();
            return;
        }
        sub.typeInfo = integerType();
    }

    @Override
    public void applyMul(Mul mul) {
        super.applyMul(mul);
        if (mul.left.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, mul.left.typeInfo.toString(), errorPos(mul.left));
            mul.typeInfo = noType();
            return;
        }
        if (mul.right.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, mul.right.typeInfo.toString(), errorPos(mul.right));
            mul.typeInfo = noType();
            return;
        }
        mul.typeInfo = integerType();
    }

    @Override
    public void applyDivide(Divide divide) {
        super.applyDivide(divide);
        if (divide.left.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, divide.left.typeInfo.toString(), errorPos(divide.left));
            divide.typeInfo = noType();
            return;
        }
        if (divide.right.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, divide.right.typeInfo.toString(), errorPos(divide.right));
            divide.typeInfo = noType();
            return;
        }
        divide.typeInfo = integerType();
    }

    @Override
    public void applyNeg(Neg neg) {
        super.applyNeg(neg);
        if (neg.expr.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, neg.expr.typeInfo.toString(), errorPos(neg.expr));
            neg.typeInfo = noType();
        } else {
            neg.typeInfo = neg.expr.typeInfo;
        }
    }

    @Override
    public void applyLt(Lt lt) {
        super.applyLt(lt);
        if (lt.left.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, lt.left.typeInfo.toString(), errorPos(lt.left));
            lt.typeInfo = noType();
            return;
        }
        if (lt.right.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, lt.right.typeInfo.toString(), errorPos(lt.right));
            lt.typeInfo = noType();
            return;
        }
        lt.typeInfo = booleanType();
    }

    @Override
    public void applyLtEq(LtEq ltEq) {
        super.applyLtEq(ltEq);
        if (ltEq.left.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, ltEq.left.typeInfo.toString(), errorPos(ltEq.left));
            ltEq.typeInfo = noType();
            return;
        }
        if (ltEq.right.typeInfo.type() != TypeEnum.INT) {
            reportTypeCheckError("type.error.expected", Constant.INT, ltEq.right.typeInfo.toString(), errorPos(ltEq.right));
            ltEq.typeInfo = noType();
            return;
        }
        ltEq.typeInfo = booleanType();
    }

    @Override
    public void applyComp(Comp comp) {
        super.applyComp(comp);
        if (isBasicType(comp.left.typeInfo) && isBasicType(comp.right.typeInfo) && comp.left.typeInfo.type() == comp.right.typeInfo.type()) {
            comp.typeInfo = booleanType();
        } else if (!isBasicType(comp.left.typeInfo) && !isBasicType(comp.right.typeInfo)) {
            comp.typeInfo = booleanType();
        } else {
            reportTypeCheckError("type.error.same", comp.left.typeInfo.toString(), comp.right.typeInfo.toString(), errorPos(comp));
            comp.typeInfo = noType();
        }
    }

    @Override
    public void applyNot(Not not) {
        super.applyNot(not);
        if (not.expr.typeInfo.type() != TypeEnum.BOOL) {
            reportTypeCheckError("type.error.expected", Constant.BOOL, not.expr.typeInfo.toString(), errorPos(not.expr));
            not.typeInfo = noType();
        } else {
            not.typeInfo = booleanType();
        }

    }

    @Override
    public void applyIdConst(IdConst idConst) {
        var type = treeSupport.lookupSymbolTable(className).lookup(idConst.tok.name);
        if (type.isPresent()) {
            var typeStr = type.get();
            if (!isTypeDefined(treeSupport.classGraph, typeStr)) {
                reportTypeCheckError("type.error.type.undefined", className, typeStr, errorPos(idConst));
                idConst.typeInfo = noType();
            } else {
                idConst.typeInfo = objectType(typeStr, className);
            }
        } else {
            reportTypeCheckError("type.error.id.undefined", className, idConst.tok.name, errorPos(idConst));
            idConst.typeInfo = noType();
        }
    }

    @Override
    public void applyStringConst(StringConst stringConst) {
        super.applyStringConst(stringConst);
        stringConst.typeInfo = stringType();
    }

    @Override
    public void applyBoolConst(BoolConst boolConst) {
        super.applyBoolConst(boolConst);
        boolConst.typeInfo = booleanType();
    }

    @Override
    public void applyIntConst(IntConst intConst) {
        super.applyIntConst(intConst);
        intConst.typeInfo = integerType();
    }

    @Override
    public void applyToken(Token token) {
        super.applyToken(token);
    }

    @Override
    public void applyParen(Paren paren) {
        super.applyParen(paren);
        paren.typeInfo = paren.expr.typeInfo;
    }

    @Override
    public void applyNoExpression(NoExpression expr) {
        super.applyNoExpression(expr);
        expr.typeInfo = noType();
    }

    private void reportTypeCheckError(String key, Object... params) {
        errMsgs.add(errorMsg(key, params));
    }
}
