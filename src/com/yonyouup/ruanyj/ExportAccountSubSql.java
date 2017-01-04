package com.yonyouup.ruanyj;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

public class ExportAccountSubSql
{

	public static void main(String[] args)
	{
		try
		{
			// TODO Auto-generated method stub
			int exportType = 0;// 0�������ƿ�Ŀ 1:�����Ŀ��ϵ
			// ����CsvReader�����ԣ�Ϊ�ָ�����GBK���뷽ʽ
			CsvReader r = new CsvReader("C:\\Users\\Administrator\\Desktop\\accountsubimp\\Bookcount.csv", ',',
					Charset.forName("GBK"));
			// ��ȡ��ͷ
			r.readHeaders();
			List<String> params = new ArrayList<String>();
			// ������ȡ��¼��ֱ������
			while (r.readRecord())
			{
				params.add(r.getRawRecord());
				// System.out.println(r.getRawRecord());
				// //��������ȡ������¼��ֵ
				// System.out.println(r.get("accountType"));
				// System.out.println(r.get("code"));
				// System.out.println(r.get("name"));
				// System.out.println(r.get("level"));
				// System.out.println(r.get("balanceDirection"));
				// System.out.println(r.get("subjectSystem"));
			}

			//ConnectionManager.URL = "jdbc:mysql://10.10.12.93:3306/ufdata_yy_002";
			if (exportType == 0)
			{
				String delSql = "delete from aa_accountingsubjects;";
				int res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
				System.out.println("ɾ��Ӱ������" + res2);
//				delSql = "delete from aa_as_accountdimension;";
//				res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
//				System.out.println("ɾ��Ӱ������" + res2);
				String sql = "insert into aa_accountingsubjects(id,accountType_id,code,name,level,balanceDirection,subjectSystem_id,path,parent_id,isEnd) values(?,?,?,?,?,?,?,?,?,?)";
				int res = DBHelperMySQL.MultiExecuteNonQuery(sql, params);
				System.out.println("����Ӱ������" + res);
			}
			if(exportType ==1)
			{
				String delSql = "delete from aa_accountsubtype;";
				int res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
				System.out.println("ɾ��Ӱ������" + res2);
				String sql = "insert into aa_accountsubtype(id,subjectSystem_id,level,path,sort_num,isEnd,code,name,balanceDirection) values(?,?,?,?,?,?,?,?,?)";
				int res = DBHelperMySQL.MultiExecuteNonQuery(sql, params);
				System.out.println("����Ӱ������" + res);
			}
			r.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
