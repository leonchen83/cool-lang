package com.leon.cool.lang.glossary;

import java.util.function.Predicate;

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
 * @author leon on 15-10-7
 */
public enum TokenKind implements Predicate<TokenKind> {
    //
    ID(TokenTag.NAME), TYPE(TokenTag.TYPE), STRING(TokenTag.STR), INTEGER(TokenTag.NUM), COMMENT(TokenTag.COMMENT),
    //KEY WORDS
    CLASS("class"), ELSE("else"), FALSE("false"), FI("fi"), IF("if"), IN("in"), INHERITS("inherits"), ISVOID("isvoid", 70), LET("let"), LOOP("loop"),
    POOL("pool"), THEN("then"), WHILE("while"), CASE("case"), ESAC("esac"), NEW("new"), OF("of"), NOT("not", 30), TRUE("true"),
    //OPERATORS
    TILDE("~", 80), PLUS("+", 50), SUB("-", 50), STAR("*", 60), SLASH("/", 60), LTEQ("<=", 40, Assoc.NONE), LT("<", 40, Assoc.NONE),
    //OTHERS
    DOT(".", 100), MONKEYS_AT("@", 90), SEMI(";"), LBRACE("{"), RBRACE("}"), LPAREN("("), RPAREN(")"), ARROW("=>"), COLON(":"), EQ("=", 40, Assoc.NONE), ASSIGN("<-", 20, Assoc.RIGHT), COMMA(","),
    //ERROR,EOF
    ERROR(), EOF();
    public final String name;
    public final TokenTag tag;
    public int prec = 10;
    public Assoc assoc = Assoc.LEFT;

    TokenKind(String name) {
        this(name, TokenTag.DEFAULT, 10, Assoc.LEFT);
    }

    TokenKind(String name, int prec, Assoc assoc) {
        this(name, TokenTag.DEFAULT, prec, assoc);
    }

    TokenKind(String name, int prec) {
        this(name, TokenTag.DEFAULT, prec, Assoc.LEFT);
    }

    TokenKind() {
        this(null, TokenTag.DEFAULT, 10, Assoc.LEFT);
    }

    TokenKind(TokenTag tag) {
        this(null, tag, 10, Assoc.LEFT);
    }

    TokenKind(@Nullable String name, TokenTag tag, int prec, Assoc assoc) {
        this.name = name;
        this.tag = tag;
        this.prec = prec;
        this.assoc = assoc;
    }

    @Override
    public boolean test(TokenKind that) {
        return this == that;
    }

    @Override
    public String toString() {
        switch (tag) {
            case NAME:
                return "ID";
            case TYPE:
                return "Type";
            case STR:
                return "String";
            case NUM:
                return "Int";
            case COMMENT:
                return "Comment";
            case DEFAULT:
                return "'" + name + "'";
            default:
                return "";
        }
    }
}
