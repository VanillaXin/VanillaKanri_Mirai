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
package xin.vanilla.util.sqlite;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link ArrayList} with four pagination information, used to
 * return the SQL's resultset and pagination info as additional.
 *
 * @param <T> The generic type for datasource.
 */
@SuppressWarnings("unused")
@Setter
@Getter
public class PaginationList<T> extends ArrayList<T> {
    /**
     * 数据总页数
     */
    private int totalPageCount;
    /**
     * 当前数据数量
     */
    private int totalItemCount;
    /**
     * 每页显示数量
     */
    private int pageItemCount;
    /**
     * 当前页数
     */
    private int curPageNo;

    public PaginationList() {
        super();
    }

    public PaginationList(int capacity) {
        super(capacity);
    }

    public PaginationList(int pageNo, int pageItemCount, int totalItemCount) {
        this(0);
        setPagination(pageNo, pageItemCount, totalItemCount);
    }

    public PaginationList(List<T> ts, int pageNo, int pageItemCount, int totalItemCount) {
        setPagination(pageNo, pageItemCount, totalItemCount);
        addAll(ts);
    }

    public PaginationList<T> setPagination(int pageNo, int pageItemCount, int totalItemCount) {
        this.curPageNo = pageNo;
        this.pageItemCount = pageItemCount;
        this.totalItemCount = totalItemCount;
        // calculate the number of pages
        this.totalPageCount = (totalItemCount - 1) / pageItemCount + 1;
        return this;
    }

    public void addTotalPageCount(int count) {
        this.totalPageCount += count;
    }

    public boolean hasNextPage() {
        return curPageNo < totalPageCount;
    }
}
