//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.shawn.dubbo.utils;

import java.io.Serializable;
import java.util.List;

public class Page<T> implements Serializable {
    private static final long serialVersionUID = -3878115558559468504L;
    private int currentPage;
    private int pageSize;
    private int startRecord;
    private int totalPage;
    private int totalRecord;
    private List<T> datas;

    public Page() {
        this.currentPage = 1;
        this.pageSize = 20;
        this.startRecord = 1;
        this.totalPage = 0;
        this.totalRecord = 0;
    }

    public Page(int currentPage, int pageSize) {
        this.currentPage = 1;
        this.pageSize = 20;
        this.startRecord = 1;
        this.totalPage = 0;
        this.totalRecord = 0;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        if(this.currentPage <= 0) {
            this.currentPage = 1;
        }

        if(this.pageSize <= 0) {
            this.pageSize = 1;
        }

    }

    public Page(int currentPage, int pageSize, int totalRecord) {
        this(currentPage, pageSize);
        this.totalRecord = totalRecord;
        if(this.totalRecord <= 0) {
            this.totalRecord = 1;
        }

    }

    public int getCurrentPage() {
        return this.currentPage <= 0?1:this.currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalRecord() {
        return this.totalRecord < 0?0:this.totalRecord;
    }

    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
    }

    public List<T> getDatas() {
        return this.datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public int getTotalPage() {
        if(this.totalRecord <= 0) {
            return 0;
        } else {
            int size = this.totalRecord / this.pageSize;
            int mod = this.totalRecord % this.pageSize;
            if(mod != 0) {
                ++size;
            }

            this.totalPage = size;
            return this.totalPage;
        }
    }

    public int getStartRecord() {
        this.startRecord = (this.getCurrentPage() - 1) * this.pageSize;
        return this.startRecord;
    }
}
