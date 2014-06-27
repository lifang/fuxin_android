package com.fuwu.mobileim.pojo;

/**
 * @作者 丁作强
 * @时间 2014-5-28 上午10:07:55
 */
public class VersionPojo {

	private int version;
	private String sql_str;

	public VersionPojo() {
	}

	public VersionPojo(int version, String sql_str) {
		super();
		this.version = version;
		this.sql_str = sql_str;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getSql_str() {
		return sql_str;
	}

	public void setSql_str(String sql_str) {
		this.sql_str = sql_str;
	}

}