/**
 * Copyright (C) 2015 Vince Styling
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
 */
package xin.vanilla.util.statement;

import static java.lang.String.format;

/**
 * This class used to parenthesizing the specified statement.
 */
public class Parenthesize extends ClauseWrapper {
    /**
     * Constructing the parenthesize clause.
     *
     * @param object the object to be parenthesizing for.
     */
    public Parenthesize(Object object) {
        clause = format("(%s)", object);
    }
}
