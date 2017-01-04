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
			int exportType = 0;// 0：导入现金流量 1:导入现金流量类型
			// 生成CsvReader对象，以，为分隔符，GBK编码方式
			CsvReader r = new CsvReader("C:\\Users\\Administrator\\Desktop\\accountsubimp\\cashflow.csv", ',',
					Charset.forName("GBK"));
			// 读取表头
			r.readHeaders();
			List<String> params = new ArrayList<String>();
			// 逐条读取记录，直至读完
			while (r.readRecord())
			{
				params.add(r.getRawRecord());
			}
			if (exportType == 0)
			{
				String delSql = "delete from aa_cashflow;";
				int res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
				System.out.println("删除影响条数" + res2);
				String sql = "insert into aa_cashflow(id,cashFlowType_id,code,name,direction) values(?,?,?,?,?)";
				int res = DBHelperMySQL.MultiExecuteNonQuery(sql, params);
				System.out.println("插入影响条数" + res);
			}
			if(exportType ==1)
			{
				String delSql = "delete from aa_cashflowtype;";
				int res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
				System.out.println("删除影响条数" + res2);
				String sql = "insert into aa_cashflowtype(id,cashSystem_id,level,path,sort_num,isEnd,code,name,parent_id) values(?,?,?,?,?,?,?,?,?)";
				int res = DBHelperMySQL.MultiExecuteNonQuery(sql, params);
				System.out.println("插入影响条数" + res);
			}
			r.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
