package com.yonyouup.ruanyj;

import java.util.HashMap;
import java.util.List;

public class ExportPubRefSql
{

	public static void main(String[] args)
	{
	
		try
		{
			ConnectionManager.URL = "jdbc:mysql://10.10.12.93:3306/ufdata_yy_002";
			//String sql = "select * from pub_ref where code in ('aa_currency','aa_bank','aa_purchasingType','aa_admindivisions','aa_transceivertype','aa_areaclass','aa_country','aa_archivelist','aa_cargospace','aa_agingperiod','aa_periodtype','aa_accountingperiod','aa_accountingsubjects','aa_cashflow','aa_subjectsystem','aa_cashflowsystem')";
			String sql = "select * from pub_ref where code like '%aa_%'";
			List<Object> rs = (List<Object>) DBHelperMySQL.ExecuteScalar(sql, "code", null);
			System.out.println("导出参照SQL开始");
			StringBuffer sb= new StringBuffer("##导出参照SQL开始##");
			sb.append("\r\n");
			sb.append("select ifnull(max(id),0)+1 into @pubrefid from pub_ref;");
			sb.append("\r\n");
			for(int i=0;i<rs.size();i++)
			{
				String code = rs.get(i).toString();
				sb.append("delete from pub_ref where code = '"+code+"';");
				sb.append("\r\n");
				String selectsql ="select * from pub_ref where code = ?";
				Object[] params = new Object[1];
				params[0] = code;
				HashMap<String, String> replaceList = new HashMap<String, String>();
				replaceList.put("id", "@pubrefid +"+i);
				String exportSql = DBHelperMySQL.ExportSql(selectsql, params, PubRef.class, "pub_ref",
						replaceList);
				sb.append(exportSql);
			}
			sb.append("##导出参照SQL结束##");
			FileOperate.WriteFile("C:\\Users\\Administrator\\Desktop\\pubref\\pubref.sql", sb.toString(), false);
			System.out.println("导出参照SQL结束");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
