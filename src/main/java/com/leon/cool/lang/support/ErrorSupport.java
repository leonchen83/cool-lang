package com.leon.cool.lang.support;

import com.leon.cool.lang.ast.TreeNode;
import com.leon.cool.lang.glossary.Pos;
import com.leon.cool.lang.tokenizer.Token;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

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
 * @author leon on 17-2-24
 */
public class ErrorSupport {

    private static final Properties messages = new Properties();

    static {
        try (InputStream stream = TreeSupport.class.getClassLoader().getResourceAsStream("messages.properties")) {
            messages.load(stream);
        } catch (IOException ignore) { }
    }

    public static void error(String key, Object... params) {
        throw new RuntimeException(errorMsg(key, params));
    }

    public static String errorMsg(String key, Object... params) {
        return MessageFormat.format(messages.getProperty(key), params);
    }

    public static String errorPos(TreeNode node) {
        return errorPos(node.starPos, node.endPos);
    }

    public static String errorPos(Token token) {
        return errorPos(token.startPos, token.endPos);
    }

    public static String errorPos(Pos startPos, Pos endPos) {
        return " at " + startPos + " to " + endPos;
    }

}
