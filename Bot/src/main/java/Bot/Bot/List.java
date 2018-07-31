package Bot.Bot;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class List 
{
	private String id[];
	private int[] balance;

	public List(){
	}
	
	public List(String id[], int[] balance)
	{
		this.id = id;
		this.balance = balance;
	}

	public String[] getId() {
		return id;
	}

	public void setId(String[] id) {
		this.id = id;
	}
	
	public void subPlayerBalance(String str, int bal)
	{
		for (int i = 0; i < id.length; i++)
		{
			if (str.equals(id[i]))
			{
				balance[i] -= bal;
			}
		}
	}
	public void addPlayerBalance(String str, int bal)
	{
		for (int i = 0; i < id.length; i++)
		{
			if (str.equals(id[i]))
			{
				balance[i] += bal;
			}
		}
	}

	public int getPlayerBalance(String str) 
	{
		int bal = 0;
		for (int i = 0; i < id.length; i++)
		{
			if (str.equals(id[i]))
			{
				bal = balance[i];
			}
		}
		return bal;
	}

	public void setBalance(int[] balance) {
		this.balance = balance;
	}
	
	public int[] getBalance() {
		return balance;
	}

	public void add(String user)
	{
		if (id == null)
		{
			id = new String[20];
			balance = new int[20];
		}
		
		checkIfFull();
		
		for (int i = 0; i < id.length; i++)
		{
			if (user.equals(id[i]))
			{
				return;
			}
			if (id[i] == null)
			{
				id[i] = user;
				balance[i] = 100;
				return;
			}
		}
	}
	
	private void checkIfFull()
	{
		if (id[id.length/2] == null){
			return;
		}
		else
		{
			int newLength = id.length*2;
			String str[] = new String[newLength];
			int bal[] = new int[newLength];
			for (int i = 0; i < id.length; i++)
			{
				str[i] = id[i];
				bal[i] = balance[i];
			}		
			id = str;
			balance = bal;
		}
	}
	
}
