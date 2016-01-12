package com.shawn.dubbo.utils;

public interface SystemConstants {
	
	/** json返回时的正确代码 */
	String RESPONSE_STATUS_SUCCESS = "000";
	/** json返回时的错误代码 */
	String RESPONSE_STATUS_FAILURE = "001";

	/**
	 * 操作成功时Json统一返回信息
	 */
	String RESPONSE_MESSAGE_SUCCESS = "操作成功!";

	/**
	 * 操作失败时Json统一返回信息
	 */
	String RESPONSE_MESSAGE_FAILURE = "操作失败!";

	String RESPONSE_MESSAGE_ROLE_NOEXIST = "用户不存在!";

	String BIZ_SERVICEPRIVILEGE_HAVE_NO_ERROR="用户没有增加服务的权限！";

	String PARAMETER_HAS_NULLPOINTER = "参数为空指针，或者空值！";

	String CAN_NOT_DELETE_DYNAMICDATA_ERROR = "不能删除静态数据";

	String  PARAMETER_TOO_LONG = "参数过长！";

	String PARAMETER_ILLEGAL_IP = "非法的IP地址！";

	String PARAMETER_ILLEGAL_LOCALIP_ANYHOSTIP = "非法的本地IP或主机IP地址！";
}
