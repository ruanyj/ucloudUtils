package com.yonyouup.ruanyj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileOperate
{

	public static void WriteFile(String fileFullName, String content, Boolean isAppend)
	{
		File file = new File(fileFullName);
		FileWriter fw = null;
		BufferedWriter writer = null;
		if (!ReadFile(fileFullName).equals(content))
		{
			try
			{
				fw = new FileWriter(file, isAppend);
				writer = new BufferedWriter(fw);
				writer.write(content);
				writer.newLine();// 换行
				writer.flush();

			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			} finally
			{
				try
				{
					writer.close();
					fw.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			System.out.println(fileFullName+"不需要更新");
		}
	}

	private static String ReadFile(String fileFullName)
	{

		int len = 0;

		StringBuffer str = new StringBuffer("");

		File file = new File(fileFullName);
		if (!file.exists())
		{
			return str.toString();
		}
		try
		{

			FileInputStream is = new FileInputStream(file);

			InputStreamReader isr = new InputStreamReader(is);

			BufferedReader in = new BufferedReader(isr);

			String line = null;

			while ((line = in.readLine()) != null)

			{

				if (len != 0) // 处理换行符的问题

				{

					str.append("\r\n" + line);

				}

				else

				{

					str.append(line);

				}

				len++;

			}

			in.close();

			is.close();

		} catch (IOException e)
		{

			// TODO Auto-generated catch block

			e.printStackTrace();

		}

		return str.toString();

	}
}
