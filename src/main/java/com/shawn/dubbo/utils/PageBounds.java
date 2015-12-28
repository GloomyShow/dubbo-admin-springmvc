package com.shawn.dubbo.utils;

abstract public class PageBounds {

	private int currentPage = 1; // 当前页
	private int pageSize = 20; //每页显示记录数
	private int startRecord = 1; //起始查询记录
	
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	
	public int getStartRecord() {
		startRecord = (getCurrentPage() - 1) * pageSize;
		return startRecord;
	}
	
}
