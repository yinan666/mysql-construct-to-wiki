package com.wangyn.dev.mysql2wiki;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/***
 * 
 * 此程序用于将数据库中所有的表结构导出成为dokuwiki的表格字符串
 * 用法：
 * 1、修改数据库名称、数据库ip和端口、数据库用户名密码信息
 * 2、执行程序
 * 3、将输出内容复制到dokuwiki文本编辑框中即可
 * 
 * @author wangyn
 *
 */
public class ExportMysqlMeta {
	//要导出的数据库名称
	private static String database = "wangyn_order_payment";
	//数据库的ip和端口号
	private static String ipport = "127.0.0.1:3306";
	//数据库用户名
	private static String username = "wangyn";
	//数据库密码
	private static String password = "123456";
	private static String mysqlurl = "jdbc:mysql://"+ipport+"/"+database;
	
	
	
	public static void main(String arg[]){
		
		bulidTablemsg();
		
	}
	
	/**
	 * 查询数据库中的表
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static List<String> getTables(Connection conn)throws Exception{
		List<String> list = new ArrayList<String>();
		//获得数据库的meta信息
		DatabaseMetaData data = conn.getMetaData();
		ResultSet rs= data.getTables("","","",null);
		//查询数据库中的表信息
		while(rs.next()){
			list.add(rs.getString(3));
		}
		return list;
	}
	
	/**
	 * 获得查询表结构的语句
	 * @param tables
	 * @return
	 */
	private static String buildQuerySql(List<String> tables){
		StringBuilder sb2 = new StringBuilder(" select COLUMN_NAME,COLUMN_TYPE,COLUMN_COMMENT,TABLE_NAME from information_schema.COLUMNS where TABLE_NAME in ");
		sb2.append("(");
		for(int i =0;i<tables.size();i++){
			sb2.append("'").append(tables.get(i)).append("'");
			if(i!=tables.size()-1){
				sb2.append(",");
			}
		}
		sb2.append(") order by TABLE_NAME");
		return sb2.toString();
	}
	
	
	public static void bulidTablemsg(){
		StringBuilder sb = new StringBuilder();
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			//获得数据库连接
			conn = DriverManager.getConnection(mysqlurl,username,password);
			//查询数据库中表信息
			List<String> tables = getTables(conn);
			if(tables==null||tables.size()<1){
				System.out.println("未找到表。。。。。");
				return;
			}
			String sql = buildQuerySql(tables);
			stmt = conn.prepareStatement(sql);
			ResultSet rs =  stmt.executeQuery();
			String old_talbename = "";
			while(rs.next()){
				//字段名
				String columnName = rs.getString("COLUMN_NAME");
				//类型
				String columnType = rs.getString("COLUMN_TYPE");
				//字段备注
				String columnComment = rs.getString("COLUMN_COMMENT");
				//当前表
				String tableName = rs.getString("TABLE_NAME");
				if(!tableName.equals(old_talbename)){
					old_talbename = tableName;
					sb.append("表："+tableName.replaceAll("\\_", "\\\\_")+"\n");
					String talbeheader = "^ 字段名  ^ 类型       ^ 备注  ^ \n";
					sb.append(talbeheader);
				}
				if(columnComment==null||"".equals(columnComment)){
					columnComment = columnName;
				}
				sb.append("| ");
				// 获得指定列的列名，将_替换为\_，防止wiki转义
				sb.append(columnName.replaceAll("\\_", "\\\\_")).append(" | ").append(columnType).append(" | ").append(columnComment.replaceAll("\\_", "\\\\_")).append(" | \n");
				
			}
			String str = sb.toString();
			System.out.println(str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(stmt!=null){
					stmt.close();
				}
				if(conn!=null){
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
