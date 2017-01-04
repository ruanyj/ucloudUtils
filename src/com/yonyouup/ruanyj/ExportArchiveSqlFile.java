package com.yonyouup.ruanyj;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExportArchiveSqlFile
{

	public static HashMap<String, String> EntityMap = new HashMap<String, String>();
	public static HashMap<String, String> GroupMap = new HashMap<String, String>();

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		try
		{
			int exportType = 0;// 0:导出档案 1:导出档案参照2:列表过滤
			List<Object> rs = new ArrayList<Object>();
			if (exportType == 0)
			{
				//String sql = "select * from pb_menu_base where menu_code like '%aa%' and menu_code!='aa' and metakey!=''";
				// and menu_code!='aa' ORDER BY metakey
				String sql = "select * from pb_menu_base  where metakey in ('aa_currencylist') ORDER BY metakey";
				rs = (List<Object>) DBHelperMySQL.ExecuteScalar(sql, "metakey", null);
			} else if (exportType == 1)
			{
				ConnectionManager.URL = "jdbc:mysql://10.10.12.93:3306/ufdata_yy_002";
				String sql = "select * from pub_ref where code in ('aa_currency','aa_bank','aa_purchasingType','aa_admindivisions','aa_transceivertype','aa_areaclass','aa_country','aa_archivelist','aa_cargospace','aa_agingperiod','aa_periodtype','aa_accountingperiod','aa_accountingsubjects','aa_cashflow','aa_subjectsystem','aa_cashflowsystem')";				
				rs = (List<Object>) DBHelperMySQL.ExecuteScalar(sql, "cbillnum", null);
			} else
			{
				String sql = "select * from pb_menu_base  where metakey like 'aa%list' ORDER BY metakey";
				rs = (List<Object>) DBHelperMySQL.ExecuteScalar(sql, "metakey", null);
			}
			ConnectionManager.URL = "jdbc:mysql://10.10.12.93:3306/ufdata_yy_002";
			System.out.println("开始导出共" + rs.size() + "个档案");
			for (int i = 0; i < rs.size(); i++)
			{
				String cBillNo = rs.get(i).toString();
				String sql2 = "select * from bill_base where cBillNo = ?";
				Object[] params = new Object[1];
				params[0] = cBillNo;
				BillBase bb = (BillBase) DBHelperMySQL.getModel(sql2, params, BillBase.class);
				if (bb == null)
				{
					System.out.println(cBillNo + "在billbase中不存在");
					continue;
				}
				//档案或参照生成sql语句
				if (exportType == 0 || exportType == 1)
				{
					int billBaseId = bb.getId();// billbaseID
					String cName = bb.getcName();// 文件名
					String dirPath =null;
					String cBillType = bb.getcBillType();
					if(exportType == 0)
					{
						dirPath = "C:\\Users\\Administrator\\Desktop\\templateSql\\";// 文件路径
					}
					else
					{
						dirPath = "C:\\Users\\Administrator\\Desktop\\templateSqlRef\\";
					}
					System.out.println("开始导出" + cName + "档案");
					exportSql(exportType,cBillNo, cName, billBaseId, dirPath,cBillType);
				}
				else//列表过滤生成sql语句
				{
					String key = "AA_"+cBillNo;
					String titile =  bb.getcName()+"过滤";
					//String dirPath = "C:\\Users\\Administrator\\Desktop\\templateSqlFilter\\";// 文件路径
					exportFilterSql(cBillNo, key,titile);
				}
				// 档案需要生成编辑画面的SQL
				if (exportType == 0)
				{
					String cBillNo2 = bb.getcCardKey();// 编辑页面cBillNo
					params[0] = cBillNo2;
					BillBase bb2 = (BillBase) DBHelperMySQL.getModel(sql2, params, BillBase.class);
					if (bb2 == null)
					{
						System.out.println(cBillNo2 + "在billbase中不存在");
						continue;
					}
					int billBaseId2 = bb2.getId();// billbaseID
					String cName2 = bb2.getcName();// 文件名
					String cBillType = bb2.getcBillType();
					String dirPath = "C:\\Users\\Administrator\\Desktop\\templateSql\\";// 文件路径
					System.out.println("开始导出" + cName2 + "档案");
					exportSql(exportType,cBillNo2, cName2, billBaseId2, dirPath,cBillType);
				}
			}
			System.out.println("导出完成");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param cBillNo
	 * @param cName
	 * @param billBaseId
	 * @param dirPath
	 */
	private static void exportSql(int exportType,String cBillNo, String cName, int billBaseId, String dirPath,String cBillType)
	{
		try
		{
			String sql2 = "select * from bill_base where cBillNo = ?";
			String sql3 = "select * from billtemplate_base where iBillId = ?";
			String sql4 = "select * from billentity_base where ibillid = ?";
			String sql5 = "select * from billtplgroup_base where iBillId = ?";
			String sql6 = "select * from billitem_base where  ibillid = ?";
			String sql7 = "select * from bill_toolbar where billnumber=?";
			String sql8 = "select * from bill_toolbaritem where billnumber=?";
			String sql9 = "select * from bill_command where billnumber=?";
			String sql10 = "select * from pub_ref where cbillnum = ?";
			Object[] paramcBillNo = new Object[1];
			paramcBillNo[0] = cBillNo;
			Object[] paramBillId = new Object[1];
			paramBillId[0] = billBaseId;
			StringBuffer beign = new StringBuffer("##" + cName + "开始##");
			beign.append("\r\n");
			beign.append("start transaction;");
			beign.append("\r\n");
			beign.append("set autocommit = 0;");
			beign.append("\r\n");
			beign.append("set @cBillNo = '" + cBillNo + "';");
			beign.append("\r\n");
			beign.append("delete from billtemplate_base where iBillId in (select id from bill_base where cBillNo='"
					+ cBillNo + "');");
			beign.append("\r\n");
			beign.append("delete from billentity_base where iBillId in (select id from bill_base where cBillNo='"
					+ cBillNo + "');");
			beign.append("\r\n");
			beign.append("delete from billtplgroup_base where iBillId in (select id from bill_base where cBillNo='"
					+ cBillNo + "');");
			beign.append("\r\n");
			beign.append("delete from billitem_base where iBillId in (select id from bill_base where cBillNo='"
					+ cBillNo + "');");
			beign.append("\r\n");
			beign.append("delete from bill_base where cBillNo='" + cBillNo + "';");
			beign.append("\r\n");
			beign.append("delete from bill_toolbar where billnumber='" + cBillNo + "';");
			beign.append("\r\n");
			beign.append("delete from bill_toolbaritem where billnumber='" + cBillNo + "';");
			beign.append("\r\n");
			beign.append("delete from bill_command where billnumber='" + cBillNo + "';");
			beign.append("\r\n");
			beign.append("select ifnull(max(id),0)+1 into @billid from bill_base;");
			beign.append("\r\n");
			beign.append("select ifnull(max(id),0)+1 into @billtemplateid from billtemplate_base;");
			beign.append("\r\n");
			beign.append("select ifnull(max(id),0)+1 into @billentityid from billentity_base;");
			beign.append("\r\n");
			beign.append("select ifnull(max(id),0)+1 into @billtplgroupid from billtplgroup_base;");
			beign.append("\r\n");
			beign.append("select ifnull(max(id),0)+1 into @billitemid from billitem_base;");
			beign.append("\r\n");
			beign.append("select ifnull(max(id),0)+1 into @billtoolbarid from bill_toolbar;");
			beign.append("\r\n");
			beign.append("select ifnull(max(id),0)+1 into @billtoolbaritemid from bill_toolbaritem;");
			beign.append("\r\n");
			beign.append("select ifnull(max(id),0)+1 into @billcommandid from bill_command;");
			beign.append("\r\n");
			if(exportType==1)
			{
				beign.append("select ifnull(max(id),0)+1 into @pubrefid from pub_ref;");
				beign.append("\r\n");
				beign.append("delete from pub_ref where cbillnum = '"+cBillNo+"';");
				beign.append("\r\n");
			}
			
			File file = new File(dirPath);
			// 如果文件夹不存在则创建
			if (!file.exists() && !file.isDirectory())
			{
				file.mkdir();
			}
			String fileFullName = dirPath + cName + ".sql";
			HashMap<String, String> replaceList = new HashMap<String, String>();
			replaceList.put("id", "@billid");
			replaceList.put("iDefTplId", "@billtemplateid");
			String billBaseSql = DBHelperMySQL.ExportSql(sql2, paramcBillNo, BillBase.class, "bill_base", replaceList);
			beign.append(billBaseSql);
			replaceList.clear();
			replaceList.put("id", "@billtemplateid");
			replaceList.put("iBillId", "@billid");
			String billTemplateSql = DBHelperMySQL.ExportSql(sql3, paramBillId, BillTemplate.class, "billtemplate_base",
					replaceList);
			beign.append(billTemplateSql);
			replaceList.clear();
			replaceList.put("id", "@billentityid");
			replaceList.put("iBillId", "@billid");
			String billEntitySql = DBHelperMySQL.ExportSql(sql4, paramBillId, BillEntityBase.class, "billentity_base",
					replaceList);
			beign.append(billEntitySql);
			replaceList.clear();
			replaceList.put("id", "@billtplgroupid");
			replaceList.put("iBillId", "@billid");
			replaceList.put("iTplId", "@billtemplateid");
			String billTplGroupSql = DBHelperMySQL.ExportSql(sql5, paramBillId, BillTplGroup.class, "billtplgroup_base",
					replaceList);
			beign.append(billTplGroupSql);
			replaceList.clear();
			replaceList.put("id", "@billitemid");
			replaceList.put("iBillId", "@billid");
			replaceList.put("iTplId", "@billtemplateid");
			String billItemBaseSql = DBHelperMySQL.ExportSql(sql6, paramBillId, BillItemBase.class, "billitem_base",
					replaceList);
			beign.append(billItemBaseSql);
			replaceList.clear();
			replaceList.put("id", "@billtoolbarid");
			String billToolbarSql = DBHelperMySQL.ExportSql(sql7, paramcBillNo, BillToolBar.class, "bill_toolbar",
					replaceList);
			beign.append(billToolbarSql);
			replaceList.clear();
			replaceList.put("id", "@billtoolbaritemid");
			String billToolbaritemSql = DBHelperMySQL.ExportSql(sql8, paramcBillNo, BillToolBarItem.class,
					"bill_toolbaritem", replaceList);
			beign.append(billToolbaritemSql);
			replaceList.clear();
			replaceList.put("id", "@billcommandid");
			replaceList.put("billnumber", "@cBillNo");
			String billCommandSql = DBHelperMySQL.ExportSql(sql9, paramcBillNo, BillCommand.class, "bill_command",
					replaceList);
			beign.append(billCommandSql);
			if(exportType==1)
			{
				replaceList.clear();
				replaceList.put("id", "@pubrefid");
				String pubRefSql = DBHelperMySQL.ExportSql(sql10, paramcBillNo, PubRef.class, "pub_ref",
						replaceList);
				beign.append(pubRefSql);
			}
			beign.append("##" + cName + "结束##");
			beign.append("\r\n");
			//左树右表和纯列表添加过滤方案
			if(cBillType.toLowerCase().equals("treelist")||cBillType.toLowerCase().equals("archivelist"))
			{
				String key = "AA_"+cBillNo;
				String titile =  cName+"过滤";
				beign.append(exportFilterSql(cBillNo, key,titile));
			}
			beign.append("\r\n");
			beign.append("commit;");
			FileOperate.WriteFile(fileFullName,beign.toString(), false);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static String exportFilterSql(String billid, String key, String titile) throws Exception
	{

			String sql = "select * from billitem_base where ibillid=(select id from bill_base where cbillno='" + billid
					+ "') and bFilter=1";
			// 导出数据开始
			StringBuffer beginout = new StringBuffer("##" + titile + "开始##");
			beginout.append("\r\n");
			String deleteSql1 = "delete from pb_filter_solution_common where solutionId in (select id from pb_filter_solution where filtersId in (select id from pb_meta_filters where filterName='"
					+ key + "'));";
			beginout.append(deleteSql1);
			beginout.append("\r\n");
			String deleteSql2 = "delete from pb_filter_solution where filtersId in (select id from pb_meta_filters where filterName='"
					+ key + "');";
			beginout.append(deleteSql2);
			beginout.append("\r\n");
			String deleteSql3 = "delete from pb_meta_filter_item where filtersId in (select id	 from pb_meta_filters where filterName='"
					+ key + "');";
			beginout.append(deleteSql3);
			beginout.append("\r\n");
			String deleteSql4 = "delete from pb_meta_filters where filterName='" + key + "';";
			beginout.append(deleteSql4);
			beginout.append("\r\n");
			String sql1 = "select ifnull(max(id),0)+1 into @filtersid from pb_meta_filters;";
			beginout.append(sql1);
			beginout.append("\r\n");
			String insertsql1 = "insert into pb_meta_filters(`id`,`filterName`,`filterDesc`,`subId`,`createTime`,`isUpGrade`,`dr`) "
					+ "values (@filtersid,'" + key + "','" + titile + "','AA',now(),0,0);";
			beginout.append(insertsql1);
			beginout.append("\r\n");

			String updatebillbase = "update bill_base set cFilterId=@filtersid where cbillno='" + billid + "';";

			beginout.append(updatebillbase);
			beginout.append("\r\n");

			List<String> lStringitem = new ArrayList<String>();
			@SuppressWarnings("unchecked")
			List<Object> rs = (List<Object>) DBHelperMySQL.ExecuteReader(sql, null, BillItemBase.class);
			String setitemsId = "select ifnull(max(id),0)+1 into @itemId from pb_meta_filter_item;";
			beginout.append(setitemsId);
			beginout.append("\r\n");
			String setitemscommon = "select ifnull(max(id),0)+1 into @commonid from pb_filter_solution;";
			beginout.append(setitemscommon);
			beginout.append("\r\n");
			String commoninsert = "insert into pb_filter_solution(`id`,`filtersId`,`solutionName`,`isDefault`,`isPublic`,`userId`,`orderId`) "
					+ "values (@commonid,@filtersid,'" + titile + "',1,0,2,null);";
			beginout.append(commoninsert);
			beginout.append("\r\n");

			String sqlcommonitemid = "select ifnull(max(id),0)+1 into @commonitemid from pb_filter_solution_common;";
			beginout.append(sqlcommonitemid);
			beginout.append("\r\n");
			List<String> lStringcommon = new ArrayList<String>();
			for (int i = 0; i < rs.size(); i++)
			{
				int order = i + 1;
				BillItemBase bill = (BillItemBase) rs.get(i);
				String itemid = bill.getcFieldName();
				String itemname = bill.getcCaption();
				String compare = "eq";
				int ranginput = 0;
				// 要添加是否添加参照的字段
				String refercode = bill.getcRefType();
				String referreturn = bill.getRefReturn();
				String itemtype = "Input";
				if (itemid.contains("date") || itemid.contains("Date") || itemid.contains("Time") || refercode != null)
				{
					compare = "between";
					ranginput = 1;
				}
				if (refercode != null || referreturn != null)
				{
					compare = "between";
					ranginput = 1;
					itemtype = "refer";
				}

				String insertitems = "insert into pb_meta_filter_item(`id`,`filtersId`,`itemName`,`itemTitle`,`itemType`,`referCode`,`refType`,`refReturn`,`compareLogic`,`iprecision`,`dataSource`,`descValue`,`isCommon`,`mustInput`,`rangeInput`,`multSelect`,`allowUpdateCompare`,`orLogic`,`defaultVal1`,`defaultVal2`,`groupName`,`isSys`,`createTime`,`dr`) "
						+ "values (@itemId,@filtersid,'" + itemid + "','" + itemname + "','" + itemtype + "','"
						+ refercode + "',null,'" + referreturn + "','" + compare + "',null,'" + itemid + "',null,0,0,"
						+ ranginput + ",0,0,0,null,null,null,1,now(),0);";
				beginout.append(insertitems);
				beginout.append("\r\n");
				lStringitem.add(insertitems);

				String insertcommonitems = "insert into pb_filter_solution_common(`id`,`solutionId`,`itemId`,`itemName`,`itemTitle`,`refType`,`isCommon`,`rangeInput`,`multSelect`,`compareLogic`,`defaultVal1`,`defaultVal2`,`orderId`,`saveHistory`,`checkRefer`) "
						+ "values (@commonitemid,@commonid,@itemId,'" + itemid + "','" + itemname + "',null,0,"
						+ ranginput + ",0,'" + compare + "',null,null,null,null,null);";
				beginout.append(insertcommonitems);
				beginout.append("\r\n");
				lStringcommon.add(insertcommonitems);

				String SetIdadd = "set @itemId=@itemId+" + order + ";";
				beginout.append(SetIdadd);
				beginout.append("\r\n");
				lStringitem.add(SetIdadd);

				String SetItemadd = "set @commonitemid=@commonitemid+" + order + ";";
				beginout.append(SetItemadd);
				beginout.append("\r\n");
				lStringcommon.add(SetItemadd);
			}
			beginout.append("select ifnull(max(id),0)+1 into @billtplgroupid from billtplgroup_base;");
			beginout.append("\r\n");
			beginout.append("INSERT INTO `billtplgroup_base` ( `id`,`cCode`, `cSubId`, `cName`, `iOrder`, `isDeleted`, `cPrimaryKey` , `iBillId`, `iBillEntityId`, `iSystem`, `bMain`, `cForeignKey`, `iTplId`, `cImage`, `cType`, `iParentId`, `cAlign`, `iCols`, `cStyle`) VALUES (@billtplgroupid,'"+billid+"_abstract', 'AA', '"+titile+"摘要','1', '0', 'id',  @billid, @billentityid, '1', '1', NULL, @billtemplateid, NULL, 'ListHeader', NULL, 'Top', NULL, NULL);");
			beginout.append("\r\n");
			beginout.append("INSERT INTO `billtplgroup_base` ( `id`,`cCode`, `cSubId`, `cName`, `iOrder`, `isDeleted`, `cPrimaryKey` , `iBillId`, `iBillEntityId`, `iSystem`, `bMain`, `cForeignKey`, `iTplId`, `cImage`, `cType`, `iParentId`, `cAlign`, `iCols`, `cStyle`) VALUES (@billtplgroupid+1,'"+billid+"_filter', 'AA', '"+titile+"过滤', '2', '0', 'id', @billid, @billentityid, '1', '1', NULL, @billtemplateid, NULL, 'ConvenientQuery', @billtplgroupid, NULL, NULL, NULL);");
			beginout.append("\r\n");
			beginout.append("##" + titile + "结束##");
			return beginout.toString();
			//String fileFullName = dirPath + titile + ".sql";
			//FileOperate.WriteFile(fileFullName, beginout.toString(), false);
	}

}
