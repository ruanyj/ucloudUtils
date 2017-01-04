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
			int exportType = 0;// 0：导入会计科目 1:导入科目体系
			// 生成CsvReader对象，以，为分隔符，GBK编码方式
			CsvReader r = new CsvReader("C:\\Users\\Administrator\\Desktop\\accountsubimp\\Bookcount.csv", ',',
					Charset.forName("GBK"));
			// 读取表头
			r.readHeaders();
			List<String> params = new ArrayList<String>();
			// 逐条读取记录，直至读完
			while (r.readRecord())
			{
				params.add(r.getRawRecord());
				// System.out.println(r.getRawRecord());
				// //按列名读取这条记录的值
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
				System.out.println("删除影响条数" + res2);
//				delSql = "delete from aa_as_accountdimension;";
//				res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
//				System.out.println("删除影响条数" + res2);
				String sql = "insert into aa_accountingsubjects(id,accountType_id,code,name,level,balanceDirection,subjectSystem_id,path,parent_id,isEnd) values(?,?,?,?,?,?,?,?,?,?)";
				int res = DBHelperMySQL.MultiExecuteNonQuery(sql, params);
				System.out.println("插入影响条数" + res);
			}
			if(exportType ==1)
			{
				String delSql = "delete from aa_accountsubtype;";
				int res2 = DBHelperMySQL.ExecuteNonQuery(delSql, null);
				System.out.println("删除影响条数" + res2);
				String sql = "insert into aa_accountsubtype(id,subjectSystem_id,level,path,sort_num,isEnd,code,name,balanceDirection) values(?,?,?,?,?,?,?,?,?)";
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
