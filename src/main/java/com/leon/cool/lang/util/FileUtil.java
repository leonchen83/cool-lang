package com.leon.cool.lang.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
 * @author leon on 15-10-31
 */
public class FileUtil {

    private FileUtil() {
    }

    public static String readJarFile(String fileName) {
        var sb = new StringBuilder();
        try (var br = new BufferedReader(new InputStreamReader(FileUtil.class.getClassLoader().getResourceAsStream(fileName)))) {
            String line; while ((line = br.readLine()) != null) sb.append(line).append("\n");
        } catch (IOException ignore) { }
        return sb.toString();
    }

    public static String readFile(String fileName) {
        var sb = new StringBuilder();
        try (var br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))))) {
            String line; while ((line = br.readLine()) != null) sb.append(line).append("\n");
        } catch (IOException ignore) { }
        return sb.toString();
    }
}
