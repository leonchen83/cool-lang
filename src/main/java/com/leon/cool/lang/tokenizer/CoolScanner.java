package com.leon.cool.lang.tokenizer;

import java.util.ArrayList;
import java.util.List;

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
public class CoolScanner {

    private Token token;

    private Token prevToken;

    private final List<Token> savedTokens = new ArrayList<>();

    private final CoolTokenizer tokenizer;

    public CoolScanner(CoolTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Token token() {
        return token(0);
    }

    public Token token(int lookahead) {
        if (lookahead == 0) {
            return token;
        } else {
            ensureLookahead(lookahead);
            return savedTokens.get(lookahead - 1);
        }
    }

    //where
    private void ensureLookahead(int lookahead) {
        for (var i = savedTokens.size(); i < lookahead; i++) {
            savedTokens.add(tokenizer.readToken());
        }
    }

    public Token prevToken() {
        return prevToken;
    }

    public void nextToken() {
        prevToken = token;
        if (!savedTokens.isEmpty()) {
            token = savedTokens.remove(0);
        } else {
            token = tokenizer.readToken();
        }
    }
}
