package com.leon.cool.lang.parser;

import com.leon.cool.lang.Constant;
import com.leon.cool.lang.ast.Branch;
import com.leon.cool.lang.ast.ClassDef;
import com.leon.cool.lang.ast.Expression;
import com.leon.cool.lang.ast.Feature;
import com.leon.cool.lang.ast.Formal;
import com.leon.cool.lang.ast.IdConst;
import com.leon.cool.lang.ast.LetAttrDef;
import com.leon.cool.lang.ast.Program;
import com.leon.cool.lang.ast.StaticDispatchBody;
import com.leon.cool.lang.factory.TreeFactory;
import com.leon.cool.lang.glossary.Assoc;
import com.leon.cool.lang.glossary.Pos;
import com.leon.cool.lang.glossary.TokenKind;
import com.leon.cool.lang.support.infrastructure.ClassTable;
import com.leon.cool.lang.tokenizer.CoolScanner;
import com.leon.cool.lang.tokenizer.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.leon.cool.lang.glossary.TokenKind.ARROW;
import static com.leon.cool.lang.glossary.TokenKind.ASSIGN;
import static com.leon.cool.lang.glossary.TokenKind.CASE;
import static com.leon.cool.lang.glossary.TokenKind.CLASS;
import static com.leon.cool.lang.glossary.TokenKind.COLON;
import static com.leon.cool.lang.glossary.TokenKind.COMMA;
import static com.leon.cool.lang.glossary.TokenKind.DOT;
import static com.leon.cool.lang.glossary.TokenKind.ELSE;
import static com.leon.cool.lang.glossary.TokenKind.EOF;
import static com.leon.cool.lang.glossary.TokenKind.EQ;
import static com.leon.cool.lang.glossary.TokenKind.ESAC;
import static com.leon.cool.lang.glossary.TokenKind.FALSE;
import static com.leon.cool.lang.glossary.TokenKind.FI;
import static com.leon.cool.lang.glossary.TokenKind.ID;
import static com.leon.cool.lang.glossary.TokenKind.IF;
import static com.leon.cool.lang.glossary.TokenKind.IN;
import static com.leon.cool.lang.glossary.TokenKind.INHERITS;
import static com.leon.cool.lang.glossary.TokenKind.INTEGER;
import static com.leon.cool.lang.glossary.TokenKind.ISVOID;
import static com.leon.cool.lang.glossary.TokenKind.LBRACE;
import static com.leon.cool.lang.glossary.TokenKind.LET;
import static com.leon.cool.lang.glossary.TokenKind.LOOP;
import static com.leon.cool.lang.glossary.TokenKind.LPAREN;
import static com.leon.cool.lang.glossary.TokenKind.LT;
import static com.leon.cool.lang.glossary.TokenKind.LTEQ;
import static com.leon.cool.lang.glossary.TokenKind.MONKEYS_AT;
import static com.leon.cool.lang.glossary.TokenKind.NEW;
import static com.leon.cool.lang.glossary.TokenKind.NOT;
import static com.leon.cool.lang.glossary.TokenKind.OF;
import static com.leon.cool.lang.glossary.TokenKind.PLUS;
import static com.leon.cool.lang.glossary.TokenKind.POOL;
import static com.leon.cool.lang.glossary.TokenKind.RBRACE;
import static com.leon.cool.lang.glossary.TokenKind.RPAREN;
import static com.leon.cool.lang.glossary.TokenKind.SEMI;
import static com.leon.cool.lang.glossary.TokenKind.SLASH;
import static com.leon.cool.lang.glossary.TokenKind.STAR;
import static com.leon.cool.lang.glossary.TokenKind.STRING;
import static com.leon.cool.lang.glossary.TokenKind.SUB;
import static com.leon.cool.lang.glossary.TokenKind.THEN;
import static com.leon.cool.lang.glossary.TokenKind.TILDE;
import static com.leon.cool.lang.glossary.TokenKind.TRUE;
import static com.leon.cool.lang.glossary.TokenKind.TYPE;
import static com.leon.cool.lang.glossary.TokenKind.WHILE;
import static com.leon.cool.lang.support.ErrorSupport.error;
import static com.leon.cool.lang.support.ErrorSupport.errorMsg;
import static com.leon.cool.lang.support.ErrorSupport.errorPos;
import static com.leon.cool.lang.support.TypeSupport.isSelf;
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
 * @author leon on 15-10-8
 */
public class CoolParser {
    private final CoolScanner scanner;
    private final TreeFactory f;
    public final List<String> errMsgs = new ArrayList<>();

    public CoolParser(CoolScanner scanner, TreeFactory f) {
        this.scanner = scanner;
        this.f = f;
        scanner.nextToken();
        token = scanner.token();
    }

    /*
     * program ::= [[class; ]] +
     */
    public Program parseProgram() {
        var classDefs = new ArrayList<ClassDef>();
        if (token.kind == EOF) {
            syntaxError(token.kind, token.startPos);
        } else {
            var startPos = token.startPos;
            classDefs.addAll(new ClassTable().builtInClasses());
            while (true) {
                if (token.kind == EOF) {
                    accept(EOF);
                    return f.at(startPos, classDefs.get(classDefs.size() - 1).endPos).program(classDefs);
                }
                try {
                    var classDef = parseClass();
                    classDefs.add(classDef);
                    accept(SEMI);
                } catch (RuntimeException e) {
                    reportSyntaxError(e.getMessage());
                    if (!errRecovery(true, false, false, EOF)) {
                        break;
                    }
                }
            }
        }
        return null;
    }

    /*
     * class ::= class TYPE [inherits TYPE] { [[feature; ]] ∗ }
     */
    private ClassDef parseClass() {
        accept(CLASS);
        var startPos = scanner.prevToken().startPos;
        if (token.kind == TYPE) {
            var type = token;
            nextToken();
            var inheritsType = Optional.of(new Token(Constant.OBJECT, TYPE));
            var features = new ArrayList<Feature>();
            if (token.kind == INHERITS) {
                accept(INHERITS);
                if (token.kind == TYPE) {
                    inheritsType = Optional.of(token);
                    nextToken();
                } else {
                    syntaxError(token.kind, token.startPos, TYPE);
                }
            }
            if (token.kind == LBRACE) {
                accept(LBRACE);
                if (token.kind == RBRACE) {
                    accept(RBRACE);
                } else {
                    while (true) {
                        if (token.kind == RBRACE) {
                            accept(RBRACE);
                            return f.at(startPos, scanner.prevToken().endPos).classDef(type, inheritsType, features);
                        }
                        try {
                            var feature = parseFeature();
                            features.add(feature);
                            accept(SEMI);
                        } catch (RuntimeException e) {
                            reportSyntaxError(e.getMessage());
                            if (!errRecovery(false, false, true, ID, RBRACE)) {
                                break;
                            }
                        }
                    }

                }
                return f.at(startPos, scanner.prevToken().endPos).classDef(type, inheritsType, features);
            }

        } else {
            syntaxError(token.kind, token.startPos, TYPE);
        }
        return null;
    }

    /*
     * feature ::= ID( [ formal [[, formal]] ∗ ] ) : TYPE { expr }
     *            |ID : TYPE [ <- expr ]
     */
    private Feature parseFeature() {
        if (token.kind == ID) {
            var id = token;
            nextToken();
            switch (token.kind) {
                case LPAREN:
                    nextToken();
                    var formals = new ArrayList<Formal>();
                    if (token.kind == RPAREN) {
                        accept(RPAREN);
                    } else {
                        do {
                            var formal = parseFormal();
                            formals.add(formal);
                        } while (isToken(COMMA));
                        accept(RPAREN);
                    }
                    accept(COLON);
                    if (token.kind == TYPE) {
                        var returnType = token;
                        nextToken();
                        accept(LBRACE);
                        var expr = parseExpr();
                        accept(RBRACE);
                        return f.at(id.startPos, scanner.prevToken().endPos).methodDef(id, formals, returnType, expr);
                    } else {
                        syntaxError(token.kind, token.startPos, TYPE);
                    }
                case COLON:
                    accept(COLON);
                    if (token.kind == TYPE) {
                        var type = token;
                        var exprOpt = Optional.<Expression>empty();
                        nextToken();
                        if (token.kind == ASSIGN) {
                            accept(ASSIGN);
                            exprOpt = Optional.of(parseExpr());
                        }
                        return f.at(id.startPos, exprOpt.isPresent() ? exprOpt.get().endPos : type.endPos).attrDef(id, type, exprOpt);
                    } else {
                        syntaxError(token.kind, token.startPos, TYPE);
                    }
                default:
                    syntaxError(token.kind, token.startPos, COLON, ID);
            }
        } else {
            syntaxError(token.kind, token.startPos, ID);
        }
        return null;
    }

    /*
     * formal ::= ID : TYPE
     */
    private Formal parseFormal() {
        if (token.kind == ID) {
            var id = token;
            nextToken();
            accept(COLON);
            if (token.kind == TYPE) {
                var formal = f.at(id.startPos, token.endPos).formal(id, token);
                nextToken();
                return formal;
            }
        } else {
            syntaxError(token.kind, token.startPos, ID);
        }
        return null;
    }

    /*
     * expr ::= ID <- expr
     *     | ID( [ expr [[, expr]] ∗ ] ) //dispatch
     *     | ID
     *     | if expr then expr else expr fi
     *     | while expr loop expr pool
     *     | { [[expr; ]] + }
     *     | let ID : TYPE [ <- expr ] [[, ID : TYPE [ <- expr ]]] ∗ in expr
     *     | case expr of [[ID : TYPE => expr; ]] + esac
     *     | new TYPE
     *     | isvoid expr
     *     | ~expr
     *     | not expr
     *     | (expr)
     *     | integer
     *     | string
     *     | true
     *     | false
     *
     *     -- left recursive
     *     | expr[@TYPE].ID( [ expr [[, expr]] ∗ ] ) //static dispatch
     *     | expr + expr
     *     | expr − expr
     *     | expr ∗ expr
     *     | expr / expr
     *     | expr < expr
     *     | expr <= expr
     *     | expr = expr
     *
     * expr = false... exprRest
     *
     * exprRest = + - / * expr exprRest
     */
    public Expression parseExpr() {
        newSuffixExprList();
        newOpStack();
        newErrStartToken();
        var expr = parseExpr(0);
        suffixExprListSupply.remove(suffixExprListSupply.size() - 1);
        opStackSupply.remove(opStackSupply.size() - 1);
        errStartTokenSupply.poll();
        return expr;
    }

    /*
     * Same as SymbolTable
     */
    private final List<List<Object>> suffixExprListSupply = new ArrayList<>();
    private final List<Deque<TokenKind>> opStackSupply = new ArrayList<>();
    private final Deque<Token> errStartTokenSupply = new LinkedList<>();
    private Token errEndToken;

    private void newErrStartToken() {
        errStartTokenSupply.push(token);
    }

    private List<Object> newSuffixExprList() {
        suffixExprListSupply.add(new ArrayList<>());
        return suffixExprListSupply.get(suffixExprListSupply.size() - 1);
    }

    private Deque<TokenKind> newOpStack() {
        opStackSupply.add(new LinkedList<>());
        return opStackSupply.get(opStackSupply.size() - 1);
    }

    private Expression add(Expression expr) {
        suffixExprListSupply.get(suffixExprListSupply.size() - 1).add(expr);
        return expr;
    }

    private TokenKind add(TokenKind op) {
        suffixExprListSupply.get(suffixExprListSupply.size() - 1).add(op);
        return op;
    }

    private Token add(Token tok) {
        suffixExprListSupply.get(suffixExprListSupply.size() - 1).add(tok);
        return tok;
    }

    private void pushOp(TokenKind o1) {
        var opStack = opStackSupply.get(opStackSupply.size() - 1);

        switch (o1) {
            case MONKEYS_AT:
            case DOT:
            case ASSIGN:
            case NOT:
            case ISVOID:
            case TILDE:
            case PLUS:
            case SUB:
            case STAR:
            case SLASH:
            case LT:
            case LTEQ:
            case EQ:
                if (opStack.isEmpty()) {
                    opStack.push(o1);
                } else {
                    while (!opStack.isEmpty() && ((o1.assoc != Assoc.RIGHT && o1.prec <= opStack.peek().prec) || (o1.assoc == Assoc.RIGHT && o1.prec < opStack.peek().prec))) {
                        add(opStack.poll());
                    }
                    opStack.push(o1);
                }
                break;
            default:
                while (!opStack.isEmpty()) {
                    add(opStack.poll());
                }
        }

    }

    private Expression parseExpr(int prec) {
        Expression returnExpr = null;
        var startPos = token.startPos;
        switch (token.kind) {
            case INTEGER:
                Expression expr = f.at(startPos, token.endPos).intConst(token);
                nextToken();
                add(expr);
                returnExpr = parseExprRest(expr);
                break;
            case STRING:
                expr = f.at(startPos, token.endPos).stringConst(token);
                nextToken();
                add(expr);
                returnExpr = parseExprRest(expr);
                break;
            case TRUE:
                expr = f.at(startPos, token.endPos).boolConst(true);
                nextToken();
                add(expr);
                returnExpr = parseExprRest(expr);
                break;
            case FALSE:
                expr = f.at(startPos, token.endPos).boolConst(false);
                nextToken();
                add(expr);
                returnExpr = parseExprRest(expr);
                break;
            case LPAREN:
                accept(LPAREN);
                expr = f.paren(parseExpr());
                accept(RPAREN);
                add(expr);
                returnExpr = parseExprRest(expr);
                break;
            case NOT:
                accept(NOT);
                pushOp(NOT);
                returnExpr = parseExpr(0);
                break;
            case TILDE:
                accept(TILDE);
                pushOp(TILDE);
                returnExpr = parseExpr(0);
                break;
            case ISVOID:
                accept(ISVOID);
                pushOp(ISVOID);
                returnExpr = parseExpr(0);
                break;
            case NEW:
                accept(NEW);
                if (token.kind == TYPE) {
                    expr = f.at(startPos, token.endPos).newDef(token);
                    nextToken();
                    add(expr);
                    returnExpr = parseExprRest(expr);
                } else {
                    syntaxError(token.kind, token.startPos, TYPE);
                }
                break;
            case CASE:
                accept(CASE);
                expr = parseExpr();
                accept(OF);
                if (peekToken(0, ESAC)) {
                    syntaxError(token.kind, token.startPos);
                } else {
                    var branches = new ArrayList<Branch>();
                    do {
                        if (token.kind == ESAC) {
                            accept(ESAC);
                            var caseExpr = f.at(startPos, scanner.prevToken().endPos).caseDef(expr, branches);
                            add(caseExpr);
                            returnExpr = parseExprRest(caseExpr);
                            break;
                        }
                        if (token.kind == ID) {
                            var id = token;
                            nextToken();
                            accept(COLON);
                            if (token.kind == TYPE) {
                                var type = token;
                                nextToken();
                                accept(ARROW);
                                var branchExpr = parseExpr();
                                var branch = f.at(id.startPos, branchExpr.endPos).branch(id, type, branchExpr);
                                branches.add(branch);
                            } else {
                                syntaxError(token.kind, token.startPos, TYPE);
                            }
                        } else {
                            syntaxError(token.kind, token.startPos, ID);
                        }
                    } while (isToken(SEMI));
                }
                break;
            case LET:
                accept(LET);
                if (token.kind == IN) {
                    syntaxError(token.kind, token.startPos);
                } else {
                    var attrDefs = new ArrayList<LetAttrDef>();
                    do {
                        try {
                            if (token.kind == ID) {
                                var id = token;
                                nextToken();
                                accept(COLON);
                                if (token.kind == TYPE) {
                                    var type = token;
                                    var exprOpt = Optional.<Expression>empty();
                                    nextToken();
                                    if (token.kind == ASSIGN) {
                                        accept(ASSIGN);
                                        exprOpt = Optional.of(parseExpr());
                                    }
                                    attrDefs.add(f.at(id.startPos, exprOpt.isPresent() ? exprOpt.get().endPos : type.endPos).letAttrDef(id, type, exprOpt));
                                } else {
                                    syntaxError(token.kind, token.startPos, TYPE);
                                }
                            } else {
                                syntaxError(token.kind, token.startPos, ID);
                            }
                        } catch (RuntimeException e) {
                            reportSyntaxError(e.getMessage());
                            errRecovery(false, true, false, IN);
                        }
                    } while (isToken(COMMA));
                    accept(IN);
                    expr = parseExpr();
                    returnExpr = f.at(startPos, expr.endPos).let(attrDefs, expr);
                }
                break;
            case LBRACE:
                accept(LBRACE);
                var exprs = new ArrayList<Expression>();
                if (peekToken(0, RBRACE)) {
                    syntaxError(token.kind, token.startPos);
                } else {
                    while (true) {
                        if (token.kind == RBRACE) {
                            accept(RBRACE);
                            var blocksExpr = f.at(startPos, scanner.prevToken().endPos).blocks(exprs);
                            add(blocksExpr);
                            returnExpr = parseExprRest(blocksExpr);
                            break;
                        }
                        try {
                            expr = parseExpr();
                            exprs.add(expr);
                            accept(SEMI);
                        } catch (RuntimeException e) {
                            reportSyntaxError(e.getMessage());
                            if (!errRecovery(false, false, true, RBRACE)) {
                                break;
                            }
                        }

                    }
                }
                break;
            case WHILE:
                accept(WHILE);
                expr = parseExpr();
                accept(LOOP);
                var loopExpr = parseExpr();
                accept(POOL);
                var whileExpr = f.at(startPos, scanner.prevToken().endPos).loop(expr, loopExpr);
                add(whileExpr);
                returnExpr = parseExprRest(whileExpr);
                break;
            case IF:
                accept(IF);
                expr = parseExpr();
                accept(THEN);
                var thenExpr = parseExpr();
                accept(ELSE);
                var elseExpr = parseExpr();
                accept(FI);
                var ifExpr = f.at(startPos, scanner.prevToken().endPos).cond(expr, thenExpr, elseExpr);
                add(ifExpr);
                returnExpr = parseExprRest(ifExpr);
                break;
            case ID:
                var id = token;
                if (peekToken(0, LPAREN)) {
                    nextToken();
                    accept(LPAREN);
                    var params = new ArrayList<Expression>();
                    if (token.kind != RPAREN) {
                        do {
                            var param = parseExpr();
                            params.add(param);
                        } while (isToken(COMMA));
                    }
                    accept(RPAREN);
                    var dispatchExpr = f.at(id.startPos, scanner.prevToken().endPos).dispatch(id, params);
                    add(dispatchExpr);
                    returnExpr = parseExprRest(dispatchExpr);
                } else {
                    expr = f.at(id.startPos, id.endPos).idConst(id);
                    nextToken();
                    add(expr);
                    returnExpr = parseExprRest(expr);
                }
                break;
            default:
                syntaxError(token.kind, token.startPos, INTEGER, ID, TRUE, FALSE, STRING, LPAREN, NOT, ISVOID, NEW, TILDE, LBRACE, IF, WHILE, LET, CASE);
        }
        assert returnExpr != null;
        return returnExpr;
    }

    private Expression parseExprRest(Expression left) {
        switch (token.kind) {
            case PLUS:
                accept(PLUS);
                pushOp(PLUS);
                left = parseExpr(0);
                return left;
            case SUB:
                accept(SUB);
                pushOp(SUB);
                left = parseExpr(0);
                return left;
            case STAR:
                accept(STAR);
                pushOp(STAR);
                left = parseExpr(0);
                return left;
            case SLASH:
                accept(SLASH);
                pushOp(SLASH);
                left = parseExpr(0);
                return left;
            case LT:
                accept(LT);
                pushOp(LT);
                left = parseExpr(0);
                return parseExprRest(left);
            case LTEQ:
                accept(LTEQ);
                pushOp(LTEQ);
                left = parseExpr(0);
                return left;
            case EQ:
                accept(EQ);
                pushOp(EQ);
                left = parseExpr(0);
                return left;
            case ASSIGN:
                accept(ASSIGN);
                pushOp(ASSIGN);
                left = parseExpr(0);
                return left;
            case MONKEYS_AT:
            case DOT:
                if (token.kind == MONKEYS_AT) {
                    accept(MONKEYS_AT);
                    pushOp(MONKEYS_AT);
                    if (token.kind == TYPE) {
                        add(token);
                        nextToken();
                    }
                }
                if (token.kind == DOT) {
                    accept(DOT);
                    if (token.kind == ID) {
                        var id = token;
                        nextToken();
                        accept(LPAREN);
                        var params = new ArrayList<Expression>();
                        if (token.kind != RPAREN) {
                            do {
                                var param = parseExpr();
                                params.add(param);
                            } while (isToken(COMMA));
                        }
                        accept(RPAREN);
                        pushOp(DOT);
                        left = f.at(id.startPos, scanner.prevToken().endPos).staticDispatchBody(id, params);
                        add(left);
                        return parseExprRest(left);
                    } else {
                        syntaxError(token.kind, token.startPos, TokenKind.ID);
                    }
                }
            case TILDE:
            case NOT:
            case ISVOID:
            default:
                pushOp(token.kind);
                var suffixExpr = suffixExprListSupply.get(opStackSupply.size() - 1);
                errEndToken = scanner.prevToken();
                return expr(suffixExpr);
        }
    }

    /*
     * Shunting yard algorithm
     */
    private Expression expr(List<Object> suffixExpr) {
        var startPos = errStartTokenSupply.peek().startPos;
        var endPos = errEndToken.endPos;
        var stack = new LinkedList<Object>();
        boolean nonAssoc = false;
        for (var i = 0; i < suffixExpr.size(); i++) {
            if (suffixExpr.get(i) instanceof TokenKind) {
                var op = (TokenKind) suffixExpr.get(i);
                switch (op) {
                    case MONKEYS_AT:
                        error("parser.error.unexpected.at", errorPos(errStartTokenSupply.peek().startPos, errEndToken.endPos));
                    case DOT:
                        if (nextIsAt(i, suffixExpr)) {
                            StaticDispatchBody dispatch = this.getGenericElement(stack);
                            Token type = this.getGenericElement(stack);
                            Expression expr = this.getGenericElement(stack);
                            assert type != null;
                            stack.push(f.at(startPos, endPos).staticDispatch(expr, Optional.of(type), dispatch));
                            i++;
                        } else {
                            StaticDispatchBody dispatch = this.getGenericElement(stack);
                            Expression expr = this.getGenericElement(stack);
                            // self.doSomething() equals to doSomething(). this is totally for simple tail-recursive optimization.
                            if (expr instanceof IdConst && isSelf(((IdConst) expr).tok)) {
                                assert dispatch != null;
                                stack.push(f.at(startPos, endPos).dispatch(dispatch.id, dispatch.params));
                            } else {
                                stack.push(f.at(startPos, endPos).staticDispatch(expr, Optional.empty(), dispatch));
                            }
                        }
                        break;
                    case ASSIGN:
                        Expression expr = this.getGenericElement(stack);
                        IdConst id = this.getGenericElement(stack);
                        stack.push(f.at(startPos, endPos).assign(id, expr));
                        break;
                    case NOT:
                        expr = this.getGenericElement(stack);
                        stack.push(f.at(startPos, endPos).not(expr));
                        break;
                    case ISVOID:
                        expr = this.getGenericElement(stack);
                        stack.push(f.at(startPos, endPos).isVoid(expr));
                        break;
                    case TILDE:
                        expr = this.getGenericElement(stack);
                        stack.push(f.at(startPos, endPos).neg(expr));
                        break;
                    case PLUS:
                        Expression right = this.getGenericElement(stack);
                        Expression left = this.getGenericElement(stack);
                        stack.push(f.at(startPos, endPos).plus(left, right));
                        break;
                    case SUB:
                        right = this.getGenericElement(stack);
                        left = this.getGenericElement(stack);
                        stack.push(f.at(startPos, endPos).sub(left, right));
                        break;
                    case STAR:
                        right = this.getGenericElement(stack);
                        left = this.getGenericElement(stack);
                        stack.push(f.at(startPos, endPos).mul(left, right));
                        break;
                    case SLASH:
                        right = this.getGenericElement(stack);
                        left = this.getGenericElement(stack);
                        stack.push(f.at(startPos, endPos).divide(left, right));
                        break;
                    case LT:
                        if (nonAssoc) {
                            error("parser.error.non.assoc", errorPos(errStartTokenSupply.peek().startPos, errEndToken.endPos));
                        } else {
                            right = this.getGenericElement(stack);
                            left = this.getGenericElement(stack);
                            stack.push(f.at(startPos, endPos).lt(left, right));
                            nonAssoc = true;
                        }
                        break;
                    case LTEQ:
                        if (nonAssoc) {
                            error("parser.error.non.assoc", errorPos(errStartTokenSupply.peek().startPos, errEndToken.endPos));
                        } else {
                            right = this.getGenericElement(stack);
                            left = this.getGenericElement(stack);
                            stack.push(f.at(startPos, endPos).ltEq(left, right));
                            nonAssoc = true;
                        }
                        break;
                    case EQ:
                        if (nonAssoc) {
                            error("parser.error.non.assoc", errorPos(errStartTokenSupply.peek().startPos, errEndToken.endPos));
                        } else {
                            right = this.getGenericElement(stack);
                            left = this.getGenericElement(stack);
                            stack.push(f.at(startPos, endPos).comp(left, right));
                            nonAssoc = true;
                        }
                        break;
                    default:
                        error("parser.error.expr", errorPos(errStartTokenSupply.peek().startPos, errEndToken.endPos));
                }
            } else {
                stack.push(suffixExpr.get(i));
            }
        }
        assert stack.size() == 1;
        return (Expression) stack.poll();
    }

    @SuppressWarnings("unchecked")
    private <T> T getGenericElement(Deque<Object> stack) {
        try {
            return (T) stack.poll();
        } catch (ClassCastException e) {
            error("parser.error.expr", errorPos(errStartTokenSupply.peek().startPos, errEndToken.endPos));
        }
        return null;
    }

    private boolean errRecovery(boolean stopAtClass, boolean stopAtComma, boolean stopAtSemi, TokenKind... tks) {
        while (true) {
            if (List.of(tks).stream().filter(e -> token.kind == e).findFirst().isPresent()) {
                return true;
            }
            switch (token.kind) {
                case CLASS:
                    if (stopAtClass) {
                        return true;
                    }
                    break;
                case SEMI:
                    if (stopAtSemi) {
                        nextToken();
                        return true;
                    }
                    break;
                case COMMA:
                    if (stopAtComma) {
                        return true;
                    }
                    break;
                case EOF:
                    return false;
                default:
                    break;
            }
            nextToken();
        }
    }

    private boolean nextIsAt(int i, List<Object> suffixExpr) {
        if ((i + 1) < suffixExpr.size() && suffixExpr.get(i + 1) instanceof TokenKind) {
            var op = (TokenKind) suffixExpr.get(i + 1);
            if (op == MONKEYS_AT) {
                return true;
            }
        }
        return false;
    }

    private Token token;

    @SuppressWarnings("unused")
    private Token token() {
        return token;
    }

    private void nextToken() {
        scanner.nextToken();
        token = scanner.token();
    }

    @SuppressWarnings("unused")
    private boolean peekToken(Predicate<TokenKind> tk) {
        return peekToken(0, tk);
    }

    private boolean peekToken(int lookAhead, Predicate<TokenKind> tk) {
        return tk.test(scanner.token(lookAhead + 1).kind);
    }

    @SuppressWarnings("unused")
    private boolean peekToken(Predicate<TokenKind> tk1, Predicate<TokenKind> tk2) {
        return peekToken(0, tk1, tk2);
    }

    private boolean peekToken(int lookAhead, Predicate<TokenKind> tk1, Predicate<TokenKind> tk2) {
        return tk1.test(scanner.token(lookAhead + 1).kind) &&
                tk2.test(scanner.token(lookAhead + 2).kind);
    }

    @SuppressWarnings("unused")
    private boolean peekToken(Predicate<TokenKind> tk1, Predicate<TokenKind> tk2, Predicate<TokenKind> tk3) {
        return peekToken(0, tk1, tk2, tk3);
    }

    private boolean peekToken(int lookAhead, Predicate<TokenKind> tk1, Predicate<TokenKind> tk2, Predicate<TokenKind> tk3) {
        return tk1.test(scanner.token(lookAhead + 1).kind) &&
                tk2.test(scanner.token(lookAhead + 2).kind) &&
                tk3.test(scanner.token(lookAhead + 3).kind);
    }

    @SafeVarargs
    @SuppressWarnings("unused")
    private final boolean peekToken(Predicate<TokenKind>... kinds) {
        return peekToken(0, kinds);
    }

    @SafeVarargs
    private final boolean peekToken(int lookAhead, Predicate<TokenKind>... kinds) {
        for (; lookAhead < kinds.length; lookAhead++) {
            if (!kinds[lookAhead].test(scanner.token(lookAhead + 1).kind)) {
                return false;
            }
        }
        return true;
    }

    private void accept(TokenKind tk) {
        if (token.kind == tk) {
            nextToken();
        } else {
            reportSyntaxError(token.kind, token.startPos, tk);
        }
    }

    private boolean isToken(TokenKind tk) {
        if (token.kind == tk) {
            nextToken();
            return true;
        } else {
            return false;
        }
    }

    private void syntaxError(TokenKind actual, Pos pos, TokenKind... tks) {
        error("parser.error.expected", mkString(Arrays.asList(tks), ","), actual.toString(), pos.toString());
    }

    private void reportSyntaxError(TokenKind actual, Pos pos, TokenKind... tks) {
        reportSyntaxError(errorMsg("parser.error.expected", mkString(Arrays.asList(tks), ","), actual.toString(), pos.toString()));
    }

    private void reportSyntaxError(String message) {
        errMsgs.add(message);
    }

    private void syntaxError(TokenKind actual, Pos pos) {
        error("parser.error.unexpected", actual.toString(), pos.toString());
    }

}
