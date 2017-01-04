package com.yonyouup.ruanyj;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

public class ExportCashFlowSql
{

	public static void main(String[] args)
	{
		try
		{
			int exportType = 0;// 0�������ֽ����� 1:�����ֽ���������
			// ����CsvReader�����ԣ�Ϊ�ָ�����GBK���뷽ʽ
			CsvReader r = new CsvReader("C:\\Users\\Administrator\\Desktop\\accountsubimp\\cashflow.csv", ',',
					Charset.forName("GBK"));
			// ��ȡ��ͷ
			r.readHeaders();
			List<String> params = new ArrayList<String>();
			// ������ȡ��¼��ֱ������
			while (r.readRecord())
			{
				params.add(r.getRawRecord());
			}
			if (exportType == 0)
			{
				String delSql = "delete from aa_cashflow;";
				int res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
				System.out.println("ɾ��Ӱ������" + res2);
				String sql = "insert into aa_cashflow(id,cashFlowType_id,code,name,direction) values(?,?,?,?,?)";
				int res = DBHelperMySQL.MultiExecuteNonQuery(sql, params);
				System.out.println("����Ӱ������" + res);
			}
			if(exportType ==1)
			{
				String delSql = "delete from aa_cashflowtype;";
				int res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
				System.out.println("ɾ��Ӱ������" + res2);
				String sql = "insert into aa_cashflowtype(id,cashSystem_id,level,path,sort_num,isEnd,code,name,parent_id) values(?,?,?,?,?,?,?,?,?)";
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
