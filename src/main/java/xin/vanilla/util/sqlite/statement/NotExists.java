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
package xin.vanilla.util.sqlite.statement;

import static java.lang.String.format;

/**
 * Appending the NOT EXISTS operator clause by a sub-query.
 *
 * @see Exists
 */
public class NotExists extends Exists {
    /**
     * Constructing the clause by a single sub-query.
     *
     * @param stmt apply a single sub-query as term.
     */
    public NotExists(Statement stmt) {
        super(stmt);
        clause = format("NOT %s", clause);
    }
}
