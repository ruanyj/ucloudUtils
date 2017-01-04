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
	 * ����ִ����䣨eg��insert��䣬update��䣬delete��䣩
	 * 
	 * @param SQL���
	 * @param ��������
	 * @return Ӱ�������
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
			throw new Exception("ExecuteNonQuery��������:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * ����ִ����䣨eg��insert��䣬update��䣬delete��䣩
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
			conn.setAutoCommit(false); // �����ֶ��ύ
			PreparedStatement psts = conn.prepareStatement(cmdtext);
			String line = null;
			for (int i = 0; i < params.size(); i++)
			{
				String[] aa = params.get(i).split(",");
				for(int j=0;j<aa.length;j++)
				{
					psts.setString(j+1, aa[j].toString().trim());
				}
				psts.addBatch(); // ������������
				count++;
			}
			psts.executeBatch(); // ִ����������
			conn.commit(); // �ύ
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
	 * ���ڲ��ҵ������ݣ�Select * from where id =0��
	 * 
	 * @param ��ѯ���
	 * @param ��ѯ����
	 * @param ��������
	 * @return ��ѯʵ�����
	 * @throws �쳣
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
			throw new Exception("ExecuteNonQuery��������:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * ����sql���
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
			// �ռ���Ҫ�滻��ID
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
			//�ص���ʼ
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
					// �滻billitem��billtplgroup_base�е�iBillEntityId
					if (item.getName().equals("iBillEntityId")
							&& (tableName.equals("billitem_base") || tableName.equals("billtplgroup_base")))
					{
						beheind.append(ExportArchiveSqlFile.EntityMap.get(rs.getString(item.getName())) + ",");
						continue;
					}
					// �滻billitem�е�iBillTplGroupId
					if (item.getName().equals("iBillTplGroupId") && tableName.equals("billitem_base"))
					{
						beheind.append(ExportArchiveSqlFile.GroupMap.get(rs.getString(item.getName())) + ",");
						continue;
					}
					// �滻billtplgroup_base�е�iParentId
					if (item.getName().equals("iParentId") && (tableName.equals("billtplgroup_base")))
					{
						beheind.append(ExportArchiveSqlFile.GroupMap.get(rs.getString(item.getName())) + ",");
						continue;
					}
					// id����
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
			throw new Exception("ExportSql��������:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * ���ڻ�ȡ�������䣨eg��selete * from table��
	 * 
	 * @param ��ѯ���
	 * @param ��ѯ����
	 * @param ��������
	 * @return ��ѯʵ�����
	 * @throws �쳣
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
			throw new Exception("ExecuteNonQuery��������:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * ���ڻ�ȡ���ֶ�ֵ��䣨������ָ���ֶΣ�
	 * 
	 * @param cmdtext
	 *            SQL���
	 * @param name
	 *            ����
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
			throw new Exception("ExecuteSqlObject��������:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * ���ڻ�ȡ���ֶ�ֵ��䣨�����ָ���ֶΣ�
	 * 
	 * @param cmdtext
	 *            SQL���
	 * @param index
	 *            ��������
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
			throw new Exception("ExecuteSqlObject��������:" + sqlE.getMessage());
		} finally
		{
			ConnectionManager.closeResultSet(rs);
			ConnectionManager.closeStatement(pstmt);
			ConnectionManager.closeConnection(conn);
		}
	}

	/**
	 * ׼��SQL����
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
	 * ת��������������
	 * 
	 * @param ��������
	 * @param Ҫ���õ�ֵ
	 * @return ֵ����
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
