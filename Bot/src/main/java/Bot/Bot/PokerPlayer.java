package Bot.Bot;

import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;

public class PokerPlayer
{
	private User user;
	private int balance;
	private Cards cards[];
	private boolean status;

	public PokerPlayer(User u) 
	{
		user = u;
		balance = 0;
		cards = new Cards[2];
		status = true;
	}
	
	public Cards getCards(int i) {
		return cards[i];
	}

	public void setCards(Cards c, int n) {
		cards[n] = c;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getBalance()
	{
		return balance;
	}
	
	public int subBalance(int b)
	{
		balance -= b;
		return b;
	}
	
	public boolean canBuyIn()
	{
		return balance - 1 > 0;
	}
	
	public String getName()
	{
		return user.getName()+user.getDiscriminator();
	}
	
	public String getID()
	{
		return Long.toString(user.getIdLong());
	}
	
	public void setStatus(boolean s)
	{
		status = s;
	}
	
	public boolean getStatus()
	{
		return status;
	}
}
