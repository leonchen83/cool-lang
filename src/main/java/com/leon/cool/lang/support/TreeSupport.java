package com.leon.cool.lang.support;

import com.leon.cool.lang.Constant;
import com.leon.cool.lang.object.CoolInt;
import com.leon.cool.lang.object.CoolObject;
import com.leon.cool.lang.object.CoolString;
import com.leon.cool.lang.support.declaration.AttrDeclaration;
import com.leon.cool.lang.support.declaration.MethodDeclaration;
import com.leon.cool.lang.support.infrastructure.Context;
import com.leon.cool.lang.support.infrastructure.ObjectHeap;
import com.leon.cool.lang.support.infrastructure.SymbolTable;
import com.leon.cool.lang.tree.compile.impl.TypeCheckTreeScanner;
import com.leon.cool.lang.tree.runtime.EvalTreeVisitor;
import com.leon.cool.lang.type.Type;
import com.leon.cool.lang.type.TypeEnum;
import com.leon.cool.lang.util.StringUtil;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.leon.cool.lang.factory.ObjectFactory.coolBoolDefault;
import static com.leon.cool.lang.factory.ObjectFactory.coolInt;
import static com.leon.cool.lang.factory.ObjectFactory.coolIntDefault;
import static com.leon.cool.lang.factory.ObjectFactory.coolObject;
import static com.leon.cool.lang.factory.ObjectFactory.coolString;
import static com.leon.cool.lang.factory.ObjectFactory.coolStringDefault;
import static com.leon.cool.lang.factory.ObjectFactory.coolVoid;
import static com.leon.cool.lang.factory.TypeFactory.noType;
import static com.leon.cool.lang.factory.TypeFactory.objectType;
import static com.leon.cool.lang.factory.TypeFactory.selfType;
import static com.leon.cool.lang.support.ErrorSupport.error;
import static com.leon.cool.lang.support.TypeSupport.isBoolType;
import static com.leon.cool.lang.support.TypeSupport.isIntType;
import static com.leon.cool.lang.support.TypeSupport.isObjectType;
import static com.leon.cool.lang.support.TypeSupport.isParent;
import static com.leon.cool.lang.support.TypeSupport.isStringType;
import static com.leon.cool.lang.util.StringUtil.constructMethod;
import static com.leon.cool.lang.util.StringUtil.mkString;

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
public class TreeSupport implements Closeable {

    public ObjectHeap heap;
    public BufferedReader reader;
    public Map<String, String> classGraph = new HashMap<>();
    public Map<String, SymbolTable<String>> symbolTables = new HashMap<>();
    public Map<String, Set<MethodDeclaration>> methodGraph = new HashMap<>();
    public Map<String, Map<String, AttrDeclaration>> attrGraph = new HashMap<>();

    public TreeSupport() {
        this.heap = new ObjectHeap();
    }
    public void createSymbolTable(String className) {
        symbolTables.putIfAbsent(className, new SymbolTable<>());
    }

    public void createAttrGraph(String className) {
        attrGraph.putIfAbsent(className, new LinkedHashMap<>());
    }

    public void createMethodGraph(String className) {
        methodGraph.putIfAbsent(className, new LinkedHashSet<>());
    }

    public void putToClassGraph(String type, Optional<String> parentType) {
        if (classGraph.containsKey(type)) {
            error("global.error.class.duplicated", type);
        } else {
            if (parentType.isPresent()) {
                classGraph.put(type, parentType.get());
                checkCircleInherits(classGraph);
            } else {
                if (isObjectType(type)) {
                    classGraph.put(type, null);
                } else {
                    error("global.error.inherits.object", type);
                }
            }
        }
    }

    public void putToMethodGraph(String className, MethodDeclaration methodDeclaration) {
        var methodDeclarations = methodGraph.get(className);
        if (methodDeclarations.contains(methodDeclaration)) {
            error("global.error.method.duplicated", className, constructMethod(methodDeclaration));
        } else {
            methodDeclarations.add(methodDeclaration);
            methodGraph.put(className, methodDeclarations);
        }
    }

    public void putToAttrGraph(String className, String id, AttrDeclaration attrDeclaration) {
        attrGraph.get(className).put(id, attrDeclaration);
    }

    public void mergeMethodGraph(String className) {
        var parentClassName = classGraph.get(className);
        while (parentClassName != null) {
            var methodDeclarations = methodGraph.get(className);
            var parentDeclarations = methodGraph.get(parentClassName);
            for (var declaration : methodDeclarations) {
                parentDeclarations.forEach(e -> {
                    //仅返回类型不同的话override错误
                    if (declaration.equals(e) && !declaration.returnType.equals(e.returnType)) {
                        error("global.error.override", className, constructMethod(declaration));
                    }
                });
            }
            parentDeclarations.stream().forEach(e -> {
                if (!methodDeclarations.contains(e)) methodDeclarations.add(e);
            });
            methodGraph.put(className, methodDeclarations);
            parentClassName = classGraph.get(parentClassName);
        }
    }

    public void mergeAttrGraph(String className) {
        var inheritsLinks = new LinkedList<String>();
        var temp = className;
        while (temp != null) {
            inheritsLinks.push(temp);
            temp = classGraph.get(temp);
        }
        while (!inheritsLinks.isEmpty()) {
            var parentClassName = inheritsLinks.poll();
            var attrs = attrGraph.get(parentClassName);
            for (var attr : attrs.entrySet()) {
                //参数重定义
                if (lookupSymbolTable(className).lookup(attr.getKey()).isPresent()) {
                    error("global.error.attr.redefined", className, attr.getKey(), parentClassName);
                } else {
                    lookupSymbolTable(className).addId(attr.getKey(), attr.getValue().type);
                }
            }
        }
        lookupSymbolTable(className).addId(Constant.SELF, Constant.SELF_TYPE);
    }

    public Optional<MethodDeclaration> lookupMethodDeclaration(String className, String methodName, List<Type> typeInfo) {
        if (methodGraph.get(className) == null) {
            // Type check for NoType.
            return Optional.empty();
        }
        var list = methodGraph.get(className).stream().filter(e -> e.methodName.equals(methodName) && checkParamType(e.paramTypes, typeInfo, className)).collect(Collectors.toList());
        if (list.isEmpty()) {
            return Optional.empty();
        } else if (list.size() > 1) {
            //包含多个方法（重载方法），则在重载方法中进一步选择
            return minimumMethodDeclaration(list, className, typeInfo);
        } else {
            return Optional.of(list.get(0));
        }
    }

    public Optional<MethodDeclaration> lookupMethodDeclaration(String className, String methodName) {
        return methodGraph.get(className).stream().filter(e -> e.methodName.equals(methodName)).findFirst();
    }

    public SymbolTable<String> lookupSymbolTable(String className) {
        return symbolTables.get(className);
    }

    public void checkUndefinedClass() {
        var keys = classGraph.keySet();
        keys.forEach(key -> {
            if (!isObjectType(key)) {
                if (!keys.contains(classGraph.get(key))) {
                    error("global.error.class.undefined", classGraph.get(key));
                }
            }
        });
    }

    @Override
    public void close() {
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException ignore) {
        }
    }

    /**
     * @param visitor
     * @param type
     * @param context
     * @return
     */
    public CoolObject newDef(EvalTreeVisitor visitor, Type type, Context context) {
        var object = coolObject();
        object.type = type;

        object.variables.enterScope();

        var inheritsLinks = new LinkedList<String>();
        var temp = type.className();
        while (temp != null) {
            inheritsLinks.push(temp);
            temp = classGraph.get(temp);
        }
        /**
         * 遍历继承树，找到父类中的属性。
         * 如果父类中的属性是String,Bool,Int类型，则对属性赋默认值.
         * 如果不是上述类型，则赋值void
         * String  = ""
         * Bool = false
         * Int = 0
         * Object = void
         */
        while (!inheritsLinks.isEmpty()) {
            var parentClassName = inheritsLinks.poll();
            var attrs = attrGraph.getOrDefault(parentClassName, Collections.emptyMap());
            for (var attr : attrs.entrySet()) {
                if (isStringType(attr.getValue().type)) {
                    object.variables.addId(attr.getKey(), coolStringDefault());
                } else if (isBoolType(attr.getValue().type)) {
                    object.variables.addId(attr.getKey(), coolBoolDefault());
                } else if (isIntType(attr.getValue().type)) {
                    object.variables.addId(attr.getKey(), coolIntDefault());
                } else {
                    object.variables.addId(attr.getKey(), coolVoid());
                }
            }
        }
        initializer(visitor, object);
        //垃圾回收
        gc(context);
        heap.add(object);
        return object;
    }

    /**
     * build-in方法求值
     *
     * @param paramObjects
     * @param obj
     * @param methodDeclaration
     * @param pos
     * @return CoolObject
     */
    public CoolObject buildIn(List<CoolObject> paramObjects, CoolObject obj, MethodDeclaration methodDeclaration, String pos) {
        switch (methodDeclaration.owner) {
            case "Object":
                if (methodDeclaration.methodName.equals("type_name")) {
                    return coolString(obj.type.className());
                } else if (methodDeclaration.methodName.equals("copy")) {
                    return obj.copy();
                } else if (methodDeclaration.methodName.equals("abort")) {
                    return coolObject().abort();
                }
                break;
            case "IO":
                if (methodDeclaration.methodName.equals("out_string")) {
                    System.out.print(((CoolString) paramObjects.get(0)).str);
                    return obj;
                } else if (methodDeclaration.methodName.equals("out_int")) {
                    System.out.print(((CoolInt) paramObjects.get(0)).val);
                    return obj;
                } else if (methodDeclaration.methodName.equals("in_string")) {
                    try {
                        var str = reader().readLine();
                        return coolString(str);
                    } catch (Exception e) {
                        e.printStackTrace();
                        error("unexpected.error");
                    }
                    return coolStringDefault();
                } else if (methodDeclaration.methodName.equals("in_int")) {
                    try {
                        var str = reader().readLine();
                        return coolInt(Integer.parseInt(str));
                    } catch (Exception e) {
                        error("unexpected.error");
                    }
                    return coolIntDefault();
                }
                break;
            case "String":
                if (methodDeclaration.methodName.equals("length")) {
                    return ((CoolString) obj).length();
                } else if (methodDeclaration.methodName.equals("concat")) {
                    return ((CoolString) obj).concat((CoolString) paramObjects.get(0));
                } else if (methodDeclaration.methodName.equals("substr")) {
                    return ((CoolString) obj).substr((CoolInt) paramObjects.get(0), (CoolInt) paramObjects.get(1), pos);
                }
                break;
        }
        return null;
    }

    /**
     * 求多个类型的最小公共父类型
     *
     * @param types
     * @return 最小公共父类型
     * @see TypeCheckTreeScanner
     * @see com.leon.cool.lang.ast.CaseDef
     * @see com.leon.cool.lang.ast.Cond
     */
    public Type lub(List<Type> types) {
        return types.stream().reduce(noType(), (type1, type2) -> {
            if (type1.type() == TypeEnum.NO_TYPE) {
                return type2;
            } else if (type2.type() == TypeEnum.NO_TYPE) {
                return type1;
            } else if (type1.type() == TypeEnum.SELF_TYPE && type2.type() == TypeEnum.SELF_TYPE) {
                return selfType(lub(type1.replace(), type2.replace()).className());
            } else {
                return lub(type1.replace(), type2.replace());
            }
        });
    }

    private Type lub(Type type1, Type type2) {
        var list1 = new ArrayList<String>();
        list1.add(type1.className());
        var temp = type1.className();
        while (temp != null) {
            temp = classGraph.get(temp);
            list1.add(temp);
        }
        var list2 = new ArrayList<String>();
        list2.add(type2.className());
        temp = type2.className();
        while (temp != null) {
            temp = classGraph.get(temp);
            list2.add(temp);
        }
        return objectType(list1.stream().filter(list2::contains).findFirst().get());
    }

    private BufferedReader reader() {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        }
        return reader;
    }

    /**
     * Mark-Sweep GC
     *
     * @param context
     */
    private void gc(Context context) {
        if (heap.size() < Constant.GC_HEAP_SIZE) return;
        var environment = context.environment;

        //Mark
        var rootObjects = new ArrayList<CoolObject>();
        for (var i = 0; i < environment.size(); i++) {
            rootObjects.addAll(environment.elementAt(i).values().stream().filter(e -> isObjectType(e.type)).collect(Collectors.toList()));
        }

        while (!rootObjects.isEmpty()) {
            var obj = rootObjects.remove(0);
            heap.canReach(obj);
            var variables = obj.variables;
            if (variables == null) continue;
            for (int i = 0; i < variables.size(); i++) {
                var values = variables.elementAt(i).values();
                for (var variable : values) {
                    //防止循环引用
                    if (isObjectType(variable.type) && !heap.isReach(variable)) {
                        rootObjects.add(variable);
                    }
                }
            }
        }
        //Sweep
        heap.clearUnreachable();
    }

    /**
     * @param visitor
     * @param object
     * @see this.newDef(Type)
     * <p>
     * 对有表达式的属性求值，并更新对象变量表，没有表达式的属性会在newDef中赋初值。
     */
    private void initializer(EvalTreeVisitor visitor, CoolObject object) {
        var inheritsLinks = new LinkedList<String>();
        var temp = object.type.className();
        while (temp != null) {
            inheritsLinks.push(temp);
            temp = classGraph.get(temp);
        }
        var context = new Context(object, object.variables);
        while (!inheritsLinks.isEmpty()) {
            var parentClassName = inheritsLinks.poll();
            var attrs = attrGraph.getOrDefault(parentClassName, Collections.emptyMap());
            attrs.entrySet().forEach(attr -> {
                if (attr.getValue().expr.isPresent()) {
                    object.variables.addId(attr.getKey(), attr.getValue().expr.get().accept(visitor, context));
                }
            });
        }
    }

    private Optional<MethodDeclaration> minimumMethodDeclaration(List<MethodDeclaration> list, String className, List<Type> typeInfo) {
        var min = new MethodDeclaration();
        min.paramTypes = typeInfo.stream().map(e -> Constant.OBJECT).collect(Collectors.toList());
        label:
        for (var declaration : list) {
            for (var i = 0; i < declaration.paramTypes.size(); i++) {
                if (!isParent(classGraph, objectType(declaration.paramTypes.get(i), className), objectType(min.paramTypes.get(i), className))) {
                    continue label;
                }
            }
            min = declaration;
        }
        for (var declaration : list) {
            for (var i = 0; i < declaration.paramTypes.size(); i++) {
                if (!isParent(classGraph, objectType(min.paramTypes.get(i), className), objectType(declaration.paramTypes.get(i), className))) {
                    error("global.error.overload", className, mkString(list.stream().map(StringUtil::constructMethod).collect(Collectors.toList()), "[", ",", "]"));
                    return Optional.empty();
                }
            }
        }
        return Optional.of(min);
    }

    private void checkCircleInherits(Map<String, String> classGraph) {
        var keys = classGraph.keySet();
        for (var key : keys) {
            var parent = classGraph.get(key);
            while (parent != null && !parent.equals(key)) {
                parent = classGraph.get(parent);
            }
            if (parent != null && parent.equals(key)) {
                error("global.error.class.circle", key);
            }
        }
    }

    private boolean checkParamType(List<String> paramTypes, List<Type> typeInfos, String className) {
        if (paramTypes.size() != typeInfos.size()) {
            return false;
        } else {
            for (var i = 0; i < typeInfos.size(); i++) {
                if (!isParent(classGraph, typeInfos.get(i), objectType(paramTypes.get(i), className))) {
                    return false;
                }
            }
        }
        return true;
    }

}
