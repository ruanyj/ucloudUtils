package com.yonyouup.ruanyj;

import java.util.ArrayList;
import java.util.List;

public class ImportToolbar
{

	public static void main(String[] args)
	{
		try
		{
			String sql = "select * from bill_base where cBillNo like '%aa_%' and cName not like '%参照%' and cBillType in('TreeArchive','treelist','archivelist','archive','Compare')";
			@SuppressWarnings("unchecked")
			List<BillBase> rs = (List<BillBase>) DBHelperMySQL.ExecuteReader(sql, null, BillBase.class);
			System.out.println("导入开始");
			for(int i=0;i<rs.size();i++)
			{
				String billnumber = rs.get(i).getcBillNo();
				String cBillType = rs.get(i).getcBillType().toLowerCase();
				String fileFullName ="C:\\Users\\Administrator\\Desktop\\toolbar\\"+billnumber+".sql";
				exportSql(billnumber,cBillType,fileFullName);
			}
			System.out.println("导入结束");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private static Boolean exportSql(String billnumber,String cBillType,String fileFullName)
	{
		StringBuffer beign = new StringBuffer("##" + billnumber + "开始##");
		beign.append("\r\n");
		beign.append("start transaction;");
		beign.append("\r\n");
		beign.append("set autocommit = 0;");
		beign.append("\r\n");
		beign.append("-- delete from bill_toolbaritem where billnumber = '"+billnumber+"';");
		beign.append("\r\n");
		beign.append("-- delete from bill_command where billnumber = '"+billnumber+"';");
		beign.append("\r\n");
		beign.append("delete from bill_status_profile where billnumber = '"+billnumber+"';");
		beign.append("\r\n");
		beign.append("delete from bill_status where billnumber='"+billnumber+"';");
		beign.append("\r\n");
		beign.append("insert into bill_status(code,name,billnumber,cmdvisible,cmdenable,itemvisible,itemenable,`condition`,subid,system) values('blank','空白','"+billnumber+"',1,0,1,0,null,'AA',0);");
		beign.append("\r\n");
		beign.append("insert into bill_status(code,name,billnumber,cmdvisible,cmdenable,itemvisible,itemenable,`condition`,subid,system) values('add','新增','"+billnumber+"',1,0,1,1,null,'AA',0);");
		beign.append("\r\n");
		beign.append("insert into bill_status(code,name,billnumber,cmdvisible,cmdenable,itemvisible,itemenable,`condition`,subid,system) values('edit','编辑','"+billnumber+"',1,0,1,1,null,'AA',0);");
		beign.append("\r\n");
		beign.append("insert into bill_status(code,name,billnumber,cmdvisible,cmdenable,itemvisible,itemenable,`condition`,subid,system) values('browse','浏览','"+billnumber+"',1,1,1,0,null,'AA',0);");
		beign.append("\r\n");
		beign.append("delete from bill_status_config where billnumber='"+billnumber+"';");
		beign.append("\r\n");
		beign.append("insert into bill_status_config(`billnumber`,`basedon`,`subid`,`system`) values('"+billnumber+"','CommonVoucher','AA',1);");
		beign.append("\r\n");
		if(cBillType.equals("treearchive"))//左树右卡
		{
			List<String> buttons = new ArrayList<String>();
			buttons.add("Add,新建,/bill/add.do,GET");
			buttons.add("Edit,编辑,null,null");
			buttons.add("Copy,复制,/bill/copy.do,GET");
			buttons.add("Save,保存,/bill/save.do,POST");
			buttons.add("Abandon,放弃,/bill/abandon.do,GET");
			buttons.add("Delete,删除,/bill/delete.do,POST");
			buttons.add("Position,定位,/bill/position.do,POST");
			buttons.add("BatchPrint,打印,/bill/print/printBill.do,POST");
			buttons.add("Help,帮助,/bill/help.do,GET");
			buttons.add("Refresh,刷新,/bill/refresh.do,POST");
			beign.append(generateSql(buttons,billnumber));
		}
		else if(cBillType.equals("treelist")||cBillType.equals("archivelist")||cBillType.equals("compare"))//左树右表、纯列表、 左树右表头表体
		{
			List<String> buttons = new ArrayList<String>();
			buttons.add("Add,新建,/bill/add.do,GET");
			buttons.add("BatchDelete,删除,/bill/batchdelete.do,POST");
			buttons.add("Position,定位,/bill/position.do,POST");
			buttons.add("BatchPrint,打印,/bill/print/printBill.do,POST");
			buttons.add("Help,帮助,/bill/help.do,GET");
			buttons.add("Refresh,刷新,/bill/refresh.do,POST");
			beign.append(generateSql(buttons,billnumber));
		}
		else if(cBillType.equals("archive"))//纯卡片
		{
			List<String> buttons = new ArrayList<String>();
			buttons.add("Edit,编辑,null,null");
			buttons.add("Copy,复制,/bill/copy.do,GET");
			buttons.add("Save,保存,/bill/save.do,POST");
			buttons.add("Abandon,放弃,/bill/abandon.do,GET");
			buttons.add("Delete,删除,/bill/delete.do,POST");
			beign.append(generateSql(buttons,billnumber));
		}
		FileOperate.WriteFile(fileFullName, beign.toString(), false);
		FileOperate.WriteFile(fileFullName, "commit;", true);
		FileOperate.WriteFile(fileFullName, "##" + billnumber + "结束##", true);
		return true;
	}
	
	/**
	 * @param buttons(Add,新建,/bill/add.do,GET)
	 * @param billnumber
	 * @return
	 */
	private static String generateSql(List<String> buttons,String billnumber)
	{
		StringBuffer res = new StringBuffer("");
		for(int i=0;i<buttons.size();i++)
		{
			String[] bt = buttons.get(i).split(",");
			res.append("delete from bill_toolbaritem where billnumber = '"+billnumber+"' and name = 'btn"+bt[0]+"';");
			res.append("\r\n");
			res.append("INSERT INTO `bill_toolbaritem` (`billnumber`, `toolbar`, `name`, `command`, `authid`, `type`, `style`, `text`, `imgsrc`, `parent`, `parameter`, `order`, `subid`, `system`, `authname`) VALUES ('"+billnumber+"', '"+billnumber+"', 'btn"+bt[0]+"', 'cmd"+bt[0]+"', '"+billnumber+"."+billnumber+".btn"+bt[0]+"', 'button', '0', '"+bt[1]+"', NULL, NULL, NULL, '"+(i+1)+"', 'AA', '1', NULL);");
			res.append("\r\n");
			res.append("delete from bill_command where billnumber = '"+billnumber+"' and name = 'cmd"+bt[0]+"';");
			res.append("\r\n");
			res.append("INSERT INTO `bill_command` (`name`, `action`, `billnumber`, `target`, `ruleid`, `parameter`, `svcurl`, `httpmethod`, `subid`, `system`) VALUES ('cmd"+bt[0]+"', '"+bt[0].toLowerCase()+"', '"+billnumber+"', '"+billnumber+"', NULL, NULL, '"+bt[2]+"', '"+bt[3]+"', 'AA', '1');");
			res.append("\r\n");
		}
		return res.toString();
	}

}
