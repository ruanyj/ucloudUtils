package com.yonyouup.ruanyj;
import java.util.ArrayList;
import java.util.List;

public class ImportArchiveList
{

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
	    try
		{
			String sql = "SELECT menu_code code,menu_name name FROM  pb_menu_lang where menu_code like '%aa%' and menu_code!='aa'  and ideleted =0 ";  
			List<Archive> rs = (List<Archive>)DBHelperMySQL.ExecuteReader(sql,null,Archive.class);
			List<String> params = new ArrayList<String>();
			for(int i=0;i<rs.size();i++)
			{
				String code = rs.get(i).getCode();
				String name = rs.get(i).getName();
				params.add((i+1)+",basic,"+code+","+name);
			}
			ConnectionManager.URL="jdbc:mysql://10.10.12.93:3306/ufdata_yy_002";
			String sql4 = "select classId code,item name from userdef_base where className='单据头';";
			@SuppressWarnings("unchecked")
			List<Archive> rs2 = (List<Archive>)DBHelperMySQL.ExecuteReader(sql4,null,Archive.class);
			for(int i=0;i<rs2.size();i++)
			{
				String code = rs2.get(i).getCode();
				String name = rs2.get(i).getName();
				params.add((rs.size()+i+1)+",custom,"+code+","+name);
			}
			String sql3 = "delete from aa_archivelist";
			int res1 = DBHelperMySQL.ExecuteNonQuery(sql3, null);
			System.out.println("删除影响条数 : "+res1);
			String sql2 = "insert into aa_archivelist(id,archiveType,code,name) values(?,?,?,?)";
			int res2 = DBHelperMySQL.MultiExecuteNonQuery(sql2,params);
			System.out.println("插入影响条数 : "+res2);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}

}
