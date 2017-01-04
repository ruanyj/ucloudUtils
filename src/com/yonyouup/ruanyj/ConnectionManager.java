package com.yonyouup.ruanyj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class ConnectionManager
{
	public static String DRIVER = "com.mysql.jdbc.Driver"; // 数据库驱动  
    public static String URL = "jdbc:mysql://10.10.12.93:3306/ufmeta"; // URL  
    public static String DBUSER = "root"; // 数据库用户名  
    public static String DBPASS = "123456"; // 数据库密码  
  
    /* 
     * 得到数据库连接 
     *  
     * @throws Exception 
     *  
     * @return 数据库连接对象 
     */  
    public static Connection getConnection() {  
        Connection dbConnection = null;  
        try {  
            // 把JDBC驱动类装载入Java虚拟机中  
            Class.forName(DRIVER);  
            // 加载驱动，并与数据库建立连接  
            URL += "?useServerPrepStmts=false&rewriteBatchedStatements=true&useUnicode=true&characterEncoding=UTF-8";  
            dbConnection = DriverManager.getConnection(URL,DBUSER, DBPASS);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return dbConnection;  
    }  
  
    public static void closeConnection(Connection dbConnection) {  
        try {  
            if (dbConnection != null && !dbConnection.isClosed()) {  
                dbConnection.close();  
            }  
        } catch (SQLException sqlEx) {  
            sqlEx.printStackTrace();  
        }  
    }  
    public static void closeResultSet(ResultSet res){  
        try{  
            if(res != null){  
                res.close();  
                res = null;  
            }  
        }catch(SQLException e){  
            e.printStackTrace();  
        }  
    }  
    public static void closeStatement(PreparedStatement pStatement){  
        try{  
            if(pStatement != null){  
                pStatement.close();  
                pStatement = null;  
            }  
        }catch(SQLException e){  
            e.printStackTrace();  
        }  
    }  
}  
