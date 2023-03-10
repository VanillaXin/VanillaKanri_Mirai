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

import lombok.Getter;
import xin.vanilla.util.lambda.LambdaUtils;
import xin.vanilla.util.lambda.SerializedFunction;

/**
 * A statement producer that use to producing <b>INSERT</b> command of SQL language.
 */
public class InsertStatement extends Statement {
    @Getter
    private CharSequence table;

    /**
     * Producing an "INSERT OR ROLLBACK INTO ..." statement.
     * <p/>
     * ROLLBACK resolution algorithm aborts the current SQL statement when an applicable
     * constraint violation occurs and rolls back the current transaction if within.
     * <p/>
     * If no transaction is active, this algorithm works the same as the ABORT algorithm.
     * <p/>
     * Checking the <a href="http://sqlite.org/lang_conflict.html">SQLite documentation</a> for more details.
     *
     * @param table the name of the table to inserting.
     * @return the created statement.
     */
    public static InsertStatement orRollback(CharSequence table) {
        return produce(table, "ROLLBACK");
    }

    /**
     * Producing an "INSERT OR REPLACE INTO ..."(equivalent to "REPLACE INTO ...") statement.
     * <p/>
     * When an UNIQUE or PRIMARY KEY constraint violation occurs, the REPLACE algorithm silently deletes
     * pre-existing rows that are causing the constraint violation prior to inserting the current row.
     * <p/>
     * Checking the <a href="http://sqlite.org/lang_conflict.html">SQLite documentation</a> for more details.
     * <p/>
     * <strong>Note:</strong> For compatibility with MySQL, SQLite allows us use the single keyword REPLACE
     * as an alias for "INSERT OR REPLACE". So if you're looking for a method which building a statement
     * like "REPLACE INTO ...", then this would be equivalent.
     *
     * @param table the name of the table to inserting.
     * @return the created statement.
     */
    public static InsertStatement orReplace(CharSequence table) {
        return produce(table, "REPLACE");
    }

    /**
     * Producing an "INSERT OR IGNORE INTO ..." statement.
     * <p/>
     * When an applicable constraint violation occurs, the IGNORE resolution algorithm skips the one row that contains
     * the constraint violation and continues processing subsequent rows of the SQL statement as if nothing went wrong.
     * <p/>
     * Checking the <a href="http://sqlite.org/lang_conflict.html">SQLite documentation</a> for more details.
     *
     * @param table the name of the table to inserting.
     * @return the created statement.
     */
    public static InsertStatement orIgnore(CharSequence table) {
        return produce(table, "IGNORE");
    }

    /**
     * Producing an "INSERT OR ABORT INTO ..." statement.
     * <p/>
     * When an applicable constraint violation occurs, the ABORT resolution algorithm aborts the current SQL statement
     * with an SQLITE_CONSTRAINT error and backs out any changes made by the current SQL statement, but changes caused
     * by prior SQL statements within the same transaction are preserved and the transaction remains active.
     * <p/>
     * This is the default behavior and the behavior specified by the SQL standard.
     * That means "INSERT OR ABORT INTO ..." equivalent to "INSERT INTO ...".
     * <p/>
     * Checking the <a href="http://sqlite.org/lang_conflict.html">SQLite documentation</a> for more details.
     *
     * @param table the name of the table to inserting.
     * @return the created statement.
     */
    public static InsertStatement orAbort(CharSequence table) {
        return produce(table, "ABORT");
    }

    /**
     * Producing an "INSERT OR FAIL INTO ..." statement.
     * <p/>
     * When an applicable constraint violation occurs, the FAIL resolution algorithm aborts the current SQL statement
     * with an SQLITE_CONSTRAINT error. But the FAIL resolution does not back out prior changes of the SQL statement
     * that failed nor does it end the transaction.
     *
     * @param table the name of the table to inserting.
     * @return the created statement.
     */
    public static InsertStatement orFail(CharSequence table) {
        return produce(table, "FAIL");
    }

    /**
     * Producing an INSERT statement with a clause. This method should be
     * work with orXXX() which in this Statement, don't call it directly.
     *
     * @param table the name of the table to inserting.
     * @return the created statement.
     */
    protected static InsertStatement produce(CharSequence table, CharSequence clause) {
        InsertStatement createStmt = new InsertStatement();
        createStmt.table = table;
        createStmt.statement.append("INSERT");
        if (clause != null) createStmt.statement.append(" OR ").append(clause);
        createStmt.statement.append(" INTO ").append('`').append(table).append('`');
        return createStmt;
    }

    /**
     * Producing an "INSERT INTO ..." statement without clause.
     *
     * @param table the name of the table to inserting.
     * @return the created statement.
     */
    public static InsertStatement produce(CharSequence table) {
        return produce(table, null);
    }

    /**
     * Using a SELECT statement instead of a VALUES clause. A new entry is inserted into
     * the table for each row of data returned by executing the SELECT statement.
     * <p/>
     * A correct form of the produced SQL would like
     * "insert into first_tbl_name(column-1, column-2) (select column-1, column-2 from second_tbl_name)".
     * <p/>
     * <strong>Notice:</strong> The {@link #columns(Object...)} method which used to specifying table columns
     * must have already have been called before enable this clause.
     * <p/>
     * Interesting to know more details about "INSERT with SELECT"? check the
     * <a href="http://www.sqlite.org/lang_insert.html">SQLite documentation</a>'s
     * describe about "The second form of the INSERT statement".
     *
     * @param stmt the subquery statement.
     * @return this statement.
     */
    public InsertStatement entry(Statement stmt) {
        statement.append(' ').append(stmt);
        return this;
    }

    /**
     * Concatenates all the given column names. Used for cooperating with {@link #entry(Statement)} method.
     *
     * @param columns the column names.
     * @return this statement.
     */
    public InsertStatement columns(Object... columns) {
        statement.append('(');
        appendClauses(columns);
        statement.append(')');
        return this;
    }

    private StringBuilder columns = new StringBuilder(128);
    private StringBuilder values = new StringBuilder(128);
    private short columnCount;

    /**
     * Put a single field pair to constructing a conventional format's INSERT statement.
     *
     * @param column column name.
     * @param value  column value, will auto quoting if instance of {@link CharSequence}.
     * @return this statement.
     */
    public InsertStatement put(CharSequence column, Object value) {
        if (++columnCount > 1) {
            columns.append(", ");
            values.append(", ");
        }
        columns.append("`").append(column).append("`");
        append(values, value);
        return this;
    }

    public <T> InsertStatement put(SerializedFunction<T, ?> column, Object value) {
        if (++columnCount > 1) {
            columns.append(", ");
            values.append(", ");
        }
        columns.append("`").append(LambdaUtils.getFiledName(column)).append("`");
        append(values, value);
        return this;
    }

    @Override
    public String toString() {
        // complete the statement if any column presented.
        if (columnCount > 0) {
            statement.append('(').append(columns).append(')');
            statement.append(" VALUES(").append(values).append(')');

            // prevent completing twice and more.
            columns = values = null;
            columnCount = 0;
        }
        return statement.toString();
    }
}
