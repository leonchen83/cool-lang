package com.leon.cool.lang.object;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.leon.cool.lang.factory.ObjectFactory.coolInt;
import static com.leon.cool.lang.factory.ObjectFactory.coolIntDefault;
import static com.leon.cool.lang.factory.ObjectFactory.coolString;
import static com.leon.cool.lang.factory.ObjectFactory.coolStringDefault;
import static com.leon.cool.lang.factory.TypeFactory.objectType;
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
 * @author leon on 15-10-21
 */
public class CoolIO extends CoolObject {
    public CoolIO() {
        this.type = objectType("IO");
    }

    public CoolObject out_string(CoolString x) {
        System.out.println(x);
        return this;
    }

    public CoolObject out_int(CoolInt x) {
        System.out.println(x.val);
        return this;
    }

    public CoolString in_string() {
        try (var reader = new BufferedReader(new InputStreamReader(System.in))) {
            var str = reader.readLine();
            return coolString(str);
        } catch (Exception e) {
            error("unexpected.error");
        }
        return coolStringDefault();
    }

    public CoolInt in_int() {
        try (var reader = new BufferedReader(new InputStreamReader(System.in))) {
            var str = reader.readLine();
            return coolInt(Integer.parseInt(str));
        } catch (Exception e) {
            error("unexpected.error");
        }
        return coolIntDefault();
    }

    @Override
    public CoolIO copy() {
        return new CoolIO();
    }
}
