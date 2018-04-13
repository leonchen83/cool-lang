/**
 * @author Leon Chen
 * @since 1.0.0
 */
module com.leon.cool.lang {
    requires java.logging;
    exports com.leon.cool.lang;
    exports com.leon.cool.lang.ast;
    exports com.leon.cool.lang.factory;
    exports com.leon.cool.lang.glossary;
    exports com.leon.cool.lang.object;
    exports com.leon.cool.lang.parser;
    exports com.leon.cool.lang.support;
    exports com.leon.cool.lang.support.declaration;
    exports com.leon.cool.lang.support.infrastructure;
    exports com.leon.cool.lang.tokenizer;
    exports com.leon.cool.lang.tree;
    exports com.leon.cool.lang.tree.compile;
    exports com.leon.cool.lang.tree.compile.impl;
    exports com.leon.cool.lang.tree.runtime;
    exports com.leon.cool.lang.tree.runtime.impl;
    exports com.leon.cool.lang.type;
    exports com.leon.cool.lang.util;
}