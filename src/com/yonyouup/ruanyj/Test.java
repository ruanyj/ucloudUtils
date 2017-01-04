package com.yonyouup.ruanyj;

public class Test
{

	public static void main(String[] args)
	{
		// TODO Auto-generated method 
		BillBase bb = new BillBase();
		bb.setcBillNo("aa");
		BillBase cc = bb;
		cc.setcBillNo("bbb");
		System.out.println(bb.getcBillNo());
		System.out.println(cc.getcBillNo());
	}

}
