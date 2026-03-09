/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.serde.xml;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.xml.annotation.XmlRootName;
import org.jspecify.annotations.Nullable;

@Serdeable
@XmlRootName(value = "custom-root2")
public class XmlPojo {

    @Nullable
    private String name;
    private int age;
    @Nullable
    private XmlNestedChild child;

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Nullable
    public XmlNestedChild getChild() {
        return child;
    }

    public void setChild(@Nullable XmlNestedChild child) {
        this.child = child;
    }
}
