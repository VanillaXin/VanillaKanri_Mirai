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

/**
 * A statement producer that use to producing <b>DELETE</b> command of SQL language.
 *
 */
public class DeleteStatement extends Statement {
    /**
     * Producing a normal DELETE statement.
     *
     * @param table the name of the table to deleting.
     * @return the created statement.
     */
    public static Statement produce(CharSequence table) {
        DeleteStatement deleteStmt = new DeleteStatement();
        deleteStmt.statement.append("DELETE");
        return deleteStmt.from(table);
    }
}
