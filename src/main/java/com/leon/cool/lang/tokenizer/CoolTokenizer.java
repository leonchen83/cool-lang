package com.leon.cool.lang.tokenizer;

import com.leon.cool.lang.Constant;
import com.leon.cool.lang.glossary.Pos;
import com.leon.cool.lang.glossary.TokenKind;
import com.leon.cool.lang.glossary.TokenTag;

import java.util.ArrayList;
import java.util.List;

import static com.leon.cool.lang.support.ErrorSupport.error;

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
public class CoolTokenizer {
    public CoolTokenizer(char[] chs) {
        this.chs = chs;
    }

    private final char[] chs;
    private int i = 0;
    private int column = 1;
    private int row = 1;
    private Pos startPos;

    private void newLine() {
        column = 1;
        row++;
    }

    private void pos() {
        startPos = new Pos(column, row);
    }

    private TokenKind tk;

    private List<Character> name;

    private final TokenKind[] keys = new TokenKind[]{TokenKind.CLASS, TokenKind.ELSE, TokenKind.FALSE, TokenKind.FI, TokenKind.IF, TokenKind.IN, TokenKind.INHERITS, TokenKind.ISVOID, TokenKind.LET, TokenKind.LOOP, TokenKind.POOL, TokenKind.THEN, TokenKind.WHILE, TokenKind.CASE, TokenKind.ESAC, TokenKind.NEW, TokenKind.OF, TokenKind.NOT, TokenKind.TRUE};

    public Token readToken() {
        loop:
        while (true) {
            pos();
            char ch = currentChar();
            switch (ch) {
                case ' ':
                case '\t':
                case '\f':
                    nextChar();
                    break;
                case '\r':
                    nextChar();
                    if (currentChar() == '\n') {
                        nextChar();
                    }
                    break;
                case '\n':
                    nextChar();
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                    scanType();
                    tk = TokenKind.TYPE;
                    break loop;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    scanID();
                    tk = TokenKind.ID;
                    break loop;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    scanInteger();
                    tk = TokenKind.INTEGER;
                    break loop;
                case '\"':
                    scanString();
                    break loop;
                case '~':
                    nextChar();
                    tk = TokenKind.TILDE;
                    break loop;
                case '+':
                    nextChar();
                    tk = TokenKind.PLUS;
                    break loop;
                case '-':
                    nextChar();
                    if (currentChar() == '-') {
                        nextChar();
                        name = new ArrayList<>();
                        while (currentChar() != '\r' && currentChar() != '\n' && currentChar() != 0x1A) {
                            putChar(currentChar());
                            nextChar();
                        }
                        tk = TokenKind.COMMENT;
                        break loop;
                    } else {
                        tk = TokenKind.SUB;
                        break loop;
                    }
                case '*':
                    nextChar();
                    tk = TokenKind.STAR;
                    break loop;
                case '/':
                    nextChar();
                    tk = TokenKind.SLASH;
                    break loop;
                case '<':
                    nextChar();
                    if (currentChar() == '=') {
                        nextChar();
                        tk = TokenKind.LTEQ;
                        break loop;
                    } else if (currentChar() == '-') {
                        nextChar();
                        tk = TokenKind.ASSIGN;
                        break loop;
                    } else {
                        tk = TokenKind.LT;
                        break loop;
                    }
                case '.':
                    nextChar();
                    tk = TokenKind.DOT;
                    break loop;
                case '@':
                    nextChar();
                    tk = TokenKind.MONKEYS_AT;
                    break loop;
                case ';':
                    nextChar();
                    tk = TokenKind.SEMI;
                    break loop;
                case ',':
                    nextChar();
                    tk = TokenKind.COMMA;
                    break loop;
                case '{':
                    nextChar();
                    tk = TokenKind.LBRACE;
                    break loop;
                case '}':
                    nextChar();
                    tk = TokenKind.RBRACE;
                    break loop;
                case '(':
                    nextChar();
                    if (currentChar() == '*') {
                        nextChar();
                        name = new ArrayList<>();
                        while (currentChar() != 0x1A) {
                            if (currentChar() == '*') {
                                nextChar();
                                if (currentChar() == ')') {
                                    nextChar();
                                    tk = TokenKind.COMMENT;
                                    break loop;
                                } else {
                                    putChar('*');
                                    putChar(currentChar());
                                    nextChar();
                                }
                            } else {
                                putChar(currentChar());
                                nextChar();
                            }
                        }
                    } else {
                        tk = TokenKind.LPAREN;
                        break loop;
                    }
                case ')':
                    nextChar();
                    tk = TokenKind.RPAREN;
                    break loop;
                case '=':
                    nextChar();
                    if (currentChar() == '>') {
                        nextChar();
                        tk = TokenKind.ARROW;
                        break loop;
                    } else {
                        tk = TokenKind.EQ;
                        break loop;
                    }
                case ':':
                    nextChar();
                    tk = TokenKind.COLON;
                    break loop;
                case 0x1A:
                    tk = TokenKind.EOF;
                    break loop;
                default:
                    tk = TokenKind.ERROR;
                    error("tokenizer.error.unexpected.char", new String(new char[]{currentChar()}), row + "", column + "");
            }
        }
        if (tk.tag == TokenTag.DEFAULT) {
            return new Token(tk.name, tk, startPos);
        } else if (tk.tag == TokenTag.NAME || tk.tag == TokenTag.TYPE) {
            Token token = lookup(getName(name));
            if (token != null) {
                return token;
            } else {
                return new Token(getName(name), tk, startPos);
            }
        } else if (tk.tag == TokenTag.COMMENT) {
            //SKIP COMMENT
            return readToken();
        } else {
            return new Token(getName(name), tk, startPos);
        }
    }

    private void scanString() {
        name = new ArrayList<>();
        startPos = new Pos(column, row);
        while (true) {
            switch (nextChar()) {
                case '\"':
                    tk = TokenKind.STRING;
                    nextChar();
                    return;
                case '\\':
                    nextChar();
                    if (currentChar() == 'b') {
                        putChar('\b');
                    } else if (currentChar() == 't') {
                        putChar('\t');
                    } else if (currentChar() == 'n') {
                        putChar('\n');
                    } else if (currentChar() == 'f') {
                        putChar('\f');
                    } else {
                        putChar(currentChar());
                    }
                    continue;
                case 0x1A:
                    //EOI
                    tk = TokenKind.ERROR;
                    error("tokenizer.error.unclosed.string");
                    return;
                case '\b':
                case '\t':
                case '\n':
                case '\f':
                    tk = TokenKind.ERROR;
                    error("tokenizer.error.illegal.char", row + "", column + "");
                    return;
                default:
                    putChar(currentChar());
            }
        }
    }

    private void scanInteger() {
        name = new ArrayList<>();
        startPos = new Pos(column, row);
        putChar(currentChar());
        while (true) {
            switch (nextChar()) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    putChar(currentChar());
                    continue;
                default:
                    return;
            }
        }
    }

    private void scanID() {
        name = new ArrayList<>();
        startPos = new Pos(column, row);
        char ch = currentChar();
        putChar(ch);
        while (true) {
            ch = nextChar();
            switch (ch) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '_':
                    putChar(currentChar());
                    continue;
                default:
                    return;
            }
        }
    }

    private void scanType() {
        scanID();
    }


    public char nextChar() {
        if (currentChar() == '\r') {
            i++;
            if (currentChar() == '\n') {
                newLine();
            }
            i--;
        } else if (currentChar() == '\n') {
            newLine();
        } else {
            column++;
        }
        i++;
        if (i < chs.length) {
            return chs[i];
        }
        //EOI
        return 0x1A;
    }

    public char currentChar() {
        if (i < chs.length) {
            return chs[i];
        }
        //EOI
        return 0x1A;
    }

    public Token lookup(String name) {
        for (TokenKind key : keys) {
            if (key.name.equals(name.toLowerCase())) {
                if (key.name.equals(Constant.TRUE) || key.name.equals(Constant.FALSE)) {
                    if (name.charAt(0) == 't' || name.charAt(0) == 'f') {
                        return new Token(name, key, startPos);
                    } else {
                        return null;
                    }
                } else {
                    return new Token(name, key, startPos);
                }
            }
        }
        return null;
    }

    public String getName(List<Character> name) {
        char[] chs = new char[name.size()];
        for (int i = 0; i < name.size(); i++) {
            chs[i] = name.get(i);
        }
        return new String(chs);
    }

    public void putChar(char c) {
        name.add(c);
    }
}
