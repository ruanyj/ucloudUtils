package com.yonyouup.ruanyj;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public final class DBHelperMySQL
{
	/**
	 * 用于执行语句（eg：insert语句，update语句，delete语句）
	 * 
	 * @param SQL语句
	 * @param 参数集合
	 * @return 影响的行数
	 */
	public static int ExecuteNonQuery(String cmdtext, Object[] params) throws Exception
	{
		PreparedStatement pstmt = null;
		Connection conn = null;
		try
		{
			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement(cmdtext);
			PrepareCommand(pstmt, params);
			return pstmt.executeUpdate();
		} catch (SQLException sqlE)
		{
			throw new Exception("ExecuteNonQuery方法出错:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * 批量执行语句（eg：insert语句，update语句，delete语句）
	 * 
	 * @param cmdtext
	 * @param params
	 * @return
	 */
	public static int MultiExecuteNonQuery(String cmdtext, List<String> params)
	{
		PreparedStatement pstmt = null;
		Connection conn = null;
		int count = 0;
		try
		{
			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement(cmdtext);
			conn.setAutoCommit(false); // 设置手动提交
			PreparedStatement psts = conn.prepareStatement(cmdtext);
			String line = null;
			for (int i = 0; i < params.size(); i++)
			{
				String[] aa = params.get(i).split(",");
				for(int j=0;j<aa.length;j++)
				{
					psts.setString(j+1, aa[j].toString().trim());
				}
				psts.addBatch(); // 加入批量处理
				count++;
			}
			psts.executeBatch(); // 执行批量处理
			conn.commit(); // 提交
		} catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		} finally
		{
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
		return count;
	}

	/**
	 * 用于查找单条数据（Select * from where id =0）
	 * 
	 * @param 查询语句
	 * @param 查询参数
	 * @param 返回类型
	 * @return 查询实体对象
	 * @throws 异常
	 */
	public static Object getModel(String cmdtext, Object[] params, Class<?> classObj) throws Exception
	{
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try
		{
			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement(cmdtext);
			PrepareCommand(pstmt, params);
			rs = pstmt.executeQuery();
			Object obj = classObj.newInstance();
			Field[] fields = classObj.getDeclaredFields();
			if (rs.next())
			{
				for (Field item : fields)
				{
					if (!item.getName().equals("serialVersionUID") && rs.getString(item.getName()) != null)
					{
						item.setAccessible(true);
						Class<?> type = item.getType();
						if (type.isPrimitive() == true)
						{
							item.set(obj, convert(item.getType().toString(), rs.getString(item.getName())));
						} else
						{
							if (item.getType().getName().equals("java.lang.String"))
							{
								item.set(obj, rs.getString(item.getName()));
							} else if (item.getType().getName().equals("java.util.Date"))
							{
								Date date = Timestamp.valueOf(rs.getString(item.getName()));
								item.set(obj, date);
							} else
							{
								Method m = type.getMethod("valueOf", String.class);
								item.set(obj, m.invoke(null, rs.getString(item.getName())));
							}
						}
						item.setAccessible(false);
					}
				}
				return obj;
			}
			return null;
		} catch (SQLException sqlE)
		{
			System.out.println(sqlE.getMessage());
			throw new Exception("ExecuteNonQuery方法出错:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * 导出sql语句
	 * 
	 * @param cmdtext
	 * @param params
	 * @param classObj
	 * @return
	 * @throws Exception
	 */
	public static String ExportSql(String cmdtext, Object[] params, Class<?> classObj, String tableName,
			HashMap<String, String> replaceList) throws Exception
	{
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try
		{
			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement(cmdtext);
			PrepareCommand(pstmt, params);
			rs = pstmt.executeQuery();
			Field[] fields = classObj.getDeclaredFields();
			StringBuffer res = new StringBuffer("");
			// 收集需要替换的ID
			while (rs.next())
			{
				for (Field item : fields)
				{
					if (item.getName().equals("id"))
					{
						if (tableName.equals("billentity_base"))
						{
							if (rs.getRow() == 1)
							{
								ExportArchiveSqlFile.EntityMap.put(rs.getString("id"), replaceList.get(item.getName()));
							}
							if (rs.getRow() > 1)
							{
								ExportArchiveSqlFile.EntityMap.put(rs.getString("id"),
										replaceList.get(item.getName()) + "+" + (rs.getRow() - 1));
							}
						}
						if (tableName.equals("billtplgroup_base"))
						{
							if (rs.getRow() == 1)
							{
								ExportArchiveSqlFile.GroupMap.put(rs.getString("id"), replaceList.get(item.getName()));
							}
							if (rs.getRow() > 1)
							{
								ExportArchiveSqlFile.GroupMap.put(rs.getString("id"),
										replaceList.get(item.getName()) + "+" + (rs.getRow() - 1));
							}
						}
					}
				}
			}
			//回到开始
			rs.beforeFirst();
			while (rs.next())
			{
				StringBuffer front = new StringBuffer("");
				front.append("insert into " + tableName + " (");
				StringBuffer beheind = new StringBuffer("");
				beheind.append(" values (");
				for (Field item : fields)
				{
					front.append("`" + item.getName() + "`,");
					// 替换billitem或billtplgroup_base中的iBillEntityId
					if (item.getName().equals("iBillEntityId")
							&& (tableName.equals("billitem_base") || tableName.equals("billtplgroup_base")))
					{
						beheind.append(ExportArchiveSqlFile.EntityMap.get(rs.getString(item.getName())) + ",");
						continue;
					}
					// 替换billitem中的iBillTplGroupId
					if (item.getName().equals("iBillTplGroupId") && tableName.equals("billitem_base"))
					{
						beheind.append(ExportArchiveSqlFile.GroupMap.get(rs.getString(item.getName())) + ",");
						continue;
					}
					// 替换billtplgroup_base中的iParentId
					if (item.getName().equals("iParentId") && (tableName.equals("billtplgroup_base")))
					{
						beheind.append(ExportArchiveSqlFile.GroupMap.get(rs.getString(item.getName())) + ",");
						continue;
					}
					// id递增
					if (replaceList.containsKey(item.getName()))
					{
						if (item.getName().equals("id") && rs.getRow() > 1)
						{
							beheind.append(replaceList.get(item.getName()) + "+" + (rs.getRow() - 1) + ",");
						} else
						{
							beheind.append(replaceList.get(item.getName()) + ",");
						}
						continue;
					}
					if (item.getType().getName().equals("java.lang.String")
							|| item.getType().getName().equals("java.util.Date"))
					{
						if (rs.getString(item.getName()) == null)
						{
							beheind.append(rs.getString(item.getName()) + ",");
						} else
						{
							beheind.append("'" + rs.getString(item.getName()) + "',");
						}
					} else
					{
						beheind.append(rs.getString(item.getName()) + ",");
					}
				}
				front = front.deleteCharAt(front.length() - 1);
				front.append(")");
				beheind = beheind.deleteCharAt(beheind.length() - 1);
				beheind.append(");");
				front.append(beheind.toString());
				res.append(front.toString());
				res.append("\r\n");
			}
			return res.toString();
		} catch (SQLException sqlE)
		{
			System.out.println(sqlE.getMessage());
			throw new Exception("ExportSql方法出错:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * 用于获取结果集语句（eg：selete * from table）
	 * 
	 * @param 查询语句
	 * @param 查询参数
	 * @param 返回类型
	 * @return 查询实体对象
	 * @throws 异常
	 */
	public static List<?> ExecuteReader(String cmdtext, Object[] params, Class<?> classObj) throws Exception
	{
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try
		{
			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement(cmdtext);
			PrepareCommand(pstmt, params);
			rs = pstmt.executeQuery();
			@SuppressWarnings(
			{ "rawtypes", "unchecked" })
			List<Object> list = new ArrayList();
			Field[] fields = classObj.getDeclaredFields();
			while (rs.next())
			{
				Object obj = classObj.newInstance();
				for (Field item : fields)
				{
					if (!item.getName().equals("serialVersionUID"))
					{
						item.setAccessible(true);
						Class<?> type = item.getType();
						try
						{
							if (type.isPrimitive() == true)
							{
								item.set(obj, convert(item.getType().toString(), rs.getString(item.getName())));
							} else
							{
								if (item.getType().getName().equals("java.lang.String"))
								{
									item.set(obj, rs.getString(item.getName()));
								} else if (item.getType().getName().equals("java.util.Date"))
								{
									Date date = Timestamp.valueOf(rs.getString(item.getName()));
									item.set(obj, date);
								} else
								{
									Method m = type.getMethod("valueOf", String.class);
									item.set(obj, m.invoke(null, rs.getString(item.getName())));
								}
							}
						} catch (Exception e)
						{
							continue;
						}
						item.setAccessible(false);
					}
				}
				//System.out.println(obj.toString());
				list.add(obj);
			}
			return list;
		} catch (SQLException sqlE)
		{
			System.out.println(sqlE.getMessage());
			throw new Exception("ExecuteNonQuery方法出错:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * 用于获取单字段值语句（用名字指定字段）
	 * 
	 * @param cmdtext
	 *            SQL语句
	 * @param name
	 *            列名
	 * @param params
	 *            OracleParameter[]
	 * @return Object
	 * @throws Exception
	 */
	public static List<Object> ExecuteScalar(String cmdtext, String name, Object[] params) throws Exception
	{
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		List<Object> result = new ArrayList<Object>();
		try
		{
			conn = ConnectionManager.getConnection();

			pstmt = conn.prepareStatement(cmdtext);
			PrepareCommand(pstmt, params);

			rs = pstmt.executeQuery();
			while (rs.next())
			{
				result.add(rs.getObject(name));
			}
			return result;
		} catch (SQLException sqlE)
		{
			throw new Exception("ExecuteSqlObject方法出错:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * 用于获取单字段值语句（用序号指定字段）
	 * 
	 * @param cmdtext
	 *            SQL语句
	 * @param index
	 *            列名索引
	 * @param params
	 *            OracleParameter[]
	 * @return Object
	 * @throws Exception
	 */
	public static Object ExecuteScalar(String cmdtext, int index, Object[] params) throws Exception
	{
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try
		{
			conn = ConnectionManager.getConnection();
			pstmt = conn.prepareStatement(cmdtext);
			PrepareCommand(pstmt, params);
			rs = pstmt.executeQuery();
			if (rs.next())
			{
				return rs.getObject(index);
			} else
			{
				return null;
			}
		} catch (SQLException sqlE)
		{
			throw new Exception("ExecuteSqlObject方法出错:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * 准备SQL参数
	 * 
	 * @param pstm
	 * @param params
	 */
	public static void PrepareCommand(PreparedStatement pstm, Object[] params)
	{
		if (params == null || params.length == 0)
		{
			// System.out.println(pstm.toString());
			return;
		}
		try
		{
			for (int i = 0; i < params.length; i++)
			{
				int parameterIndex = i + 1;
				pstm.setString(parameterIndex, params[i].toString());
			}
			// System.out.println(pstm.toString());
		} catch (Exception e)
		{
		}
	}

	/**
	 * 转换基础类型数据
	 * 
	 * @param 基础类型
	 * @param 要设置的值
	 * @return 值对象
	 */
	private static Object convert(String type, String value)
	{
		if (type.equals("int"))
			return new Integer(value);
		if (type.equals("double"))
			return new Double(value);
		if (type.equals("float"))
			return new Float(value);
		return null;
	}
}
