package Bot.Bot;

import javax.swing.*;
import javax.swing.Timer;

import net.dv8tion.jda.client.entities.UserSettings;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.awt.event.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.awt.*;

public class Poker
{   
    private List saveFile;  
    private DM dm = new DM();
    
    private PokerPlayer player[];
    private User user[];
    private Cards cards[];
    private MessageChannel channel;
    private Stack<Integer> deck = new Stack<Integer>();
    
    Random rng;
    private House house;
    private Pot pot;
    private Timer timer = new Timer(1000, new TimerListener());
    private int counter = 0;
    private int playerCounter = 0;
    private int currentRaise = 0;
    private int playerTurn = 0;
    private boolean wasRaised = false;
    private boolean gameFinished;
    
    private String winner = "";
    
    public Poker()
    {
    	house = new House();
    	pot = new Pot();
        rng = new Random();
        cards = new Cards[52];
        saveFile = new List();
        player = new PokerPlayer[6];
        user = new User[6];
        gameFinished = false;
    }
    
    public void init()
    { 
        loadPlayers();
        loadCards();
        shuffleDeck();
        play();
    }
    
    private void play()
    {
    	house.add(deal());
    	house.add(deal());
    	house.add(deal());
    	showHouseCards();   
        
        for (int i = 0; i < counter; i++)
        {
        	int buyCost = 1;
        	if (player[i].canBuyIn())
        	{
                player[i].setCards(deal(), 0);
                player[i].setCards(deal(), 1);       
                dm.sendDm(player[i].getCards(0).getImage(), i);
                dm.sendDm(player[i].getCards(1).getImage(), i);
                pot.add(player[i].subBalance(buyCost));
                saveFile.subPlayerBalance(player[i].getID(), buyCost);
        	}
        	else
        	{
        		player[i].setStatus(false);
        	}
        }
    	channel.sendMessage(player[playerCounter].getName()+"'s turn!").queue();
    	channel.sendMessage("Current Pot: "+pot.getBalance()).queue();
    }
    
    public void turn(User u, String str)
    {
    	turn(u, str, 0);
    }
    public void turn(User u, String str, int num) 
    {	
    	if (player[playerCounter].getID().equals(u.getId()))
    	{
    		if (str.equalsIgnoreCase("!poker raise "+num) && num > 0)
    		{
    			if (player[playerCounter].getBalance() >= num)
    			{
        			pot.add(player[playerCounter].subBalance(num));
        			currentRaise = num;
        			saveFile.subPlayerBalance(player[playerCounter].getID(), num);
        			playerTurn = playerCounter;
        			wasRaised = true;	
    			}
    			else
    			{
    				channel.sendMessage("You have insufficient funds to raise!").queue();
    				return;
    			}
    		}
    		else if (str.equalsIgnoreCase("!poker check") && !wasRaised)
    		{
    			
    		}
    		else if (str.equalsIgnoreCase("!poker call") && wasRaised)
    		{
    			if (player[playerCounter].getBalance() >= currentRaise)
    			{
    				pot.add(player[playerCounter].subBalance(currentRaise));
    				saveFile.subPlayerBalance(player[playerCounter].getID(), currentRaise);
    			}
    			else
    			{
    				channel.sendMessage("You have insufficient funds to call! All in!").queue();
    				allIn();
    				return;
    			}
    		}
    		else if (str.equalsIgnoreCase("!poker fold"))
    		{
    			player[playerCounter].setStatus(false);
    		}
    		playerCounter++;
    		
    		if (player[playerCounter] == null || !player[playerCounter].getStatus())
    			playerCounter++;
    		
    		if (player[playerCounter] == null)
    		{
    			playerCounter = 0;
    			if (house.isFull() && !str.equalsIgnoreCase("!poker fold") && playerCounter == playerTurn)	//end game
    			{
    				showCards();
    				return;
    			}  			
    			else if (str.equalsIgnoreCase("!poker fold"))	//everyone folds
    			{			
    				boolean flag = false;
    				for (int i = 0; i < playerCounter; i++)
    				{
    					if(player[i].getStatus())
    						flag = true; 
    				}
    				if (!flag)
    				{
    					gameFinished = true;
    					channel.sendMessage("Everyone folded!").queue();
    					return;
    				}	
    			}
    			else if (playerCounter == playerTurn)	//when raising
    			{
        			house.add(deal());
        			showHouseCards();	
    			}
    		}
    		if (player[playerCounter].getStatus())
    		{
            	channel.sendMessage(player[playerCounter].getName()+"'s turn!").queue();
            	channel.sendMessage("Current Pot: "+pot.getBalance()).queue();  			
    		}
    	}
    }
    
    public String getWinner()
    {
    	return winner;
    }
    
    private void showHouseCards()
    {
        for (int i = 0; i < house.getCardCount(); i++)
        {
        	 channel.sendFile(house.getCard(i).getImage()).queue();
        }
        System.out.println(house.printCards());
    }
    public boolean gameDone()
    {
    	return gameFinished;
    }
    
    private void allIn()
    {
    	pot.add(player[playerCounter].subBalance(player[playerCounter].getBalance()));
    }
    private void showCards()
    {
    	gameFinished = true;
    	winner = player[0].getID();
    	saveFile.addPlayerBalance(winner, pot.getBalance());
    }
    private void shuffleDeck()
    {
        for(int i = 0; i < 52; i++)
            deck.add(i,i);
        Collections.shuffle(deck);
    }
    private Cards deal()
    {
        int n = deck.pop();
        Cards c = cards[n];
        return c;
    }
    
    private void loadCards()
    {
        int n = 1;
        String color = "";
        String symbol = "";
            
        for (int i = 0; i < 52; i++)
        {
            if (i % 4 == 0) {
                symbol = "diamond";
                color = "Red";
                n++;
            }   
            else if (i % 4 == 1) {
                symbol = "club";
                color = "black";
            }
            else if (i % 4 == 2) {
                symbol = "heart";
                color = "Red";
            }           
            else if (i % 4 == 3) {
                symbol = "spade";
                color = "black";
            }
            cards[i] = new Cards(n, symbol, color);          
        }
        cards[0].setImage(new File("2diamond.png"));
        cards[1].setImage(new File("2club.png"));
        cards[2].setImage(new File("2heart.png"));
        cards[3].setImage(new File("2spade.png"));
        
        cards[4].setImage(new File("3diamond.png"));
        cards[5].setImage(new File("3club.png"));
        cards[6].setImage(new File("3heart.png"));
        cards[7].setImage(new File("3spade.png"));
        
        cards[8].setImage(new File("4diamond.png"));
        cards[9].setImage(new File("4club.png"));
        cards[10].setImage(new File("4heart.png"));
        cards[11].setImage(new File("4spade.png"));
        
        cards[12].setImage(new File("5diamond.png"));
        cards[13].setImage(new File("5club.png"));
        cards[14].setImage(new File("5heart.png"));
        cards[15].setImage(new File("5spade.png"));
        
        cards[16].setImage(new File("6diamond.png"));
        cards[17].setImage(new File("6club.png"));
        cards[18].setImage(new File("6heart.png"));
        cards[19].setImage(new File("6spade.png"));
        
        cards[20].setImage(new File("7diamond.png"));
        cards[21].setImage(new File("7club.png"));
        cards[22].setImage(new File("7heart.png"));
        cards[23].setImage(new File("7spade.png"));
        
        cards[24].setImage(new File("8diamond.png"));
        cards[25].setImage(new File("8club.png"));
        cards[26].setImage(new File("8heart.png"));
        cards[27].setImage(new File("8spade.png"));
        
        cards[28].setImage(new File("9diamond.png"));
        cards[29].setImage(new File("9club.png"));
        cards[30].setImage(new File("9heart.png"));
        cards[31].setImage(new File("9spade.png"));
        
        cards[32].setImage(new File("10diamond.png"));
        cards[33].setImage(new File("10club.png"));
        cards[34].setImage(new File("10heart.png"));
        cards[35].setImage(new File("10spade.png"));
        
        cards[36].setImage(new File("Jdiamond.png"));
        cards[37].setImage(new File("Jclub.png"));
        cards[38].setImage(new File("Jheart.png"));
        cards[39].setImage(new File("Jspade.png"));
        
        cards[40].setImage(new File("Qdiamond.png"));
        cards[41].setImage(new File("Qclub.png"));
        cards[42].setImage(new File("Qheart.png"));
        cards[43].setImage(new File("Qspade.png"));
        
        cards[44].setImage(new File("Kdiamond.png"));
        cards[45].setImage(new File("Kclub.png"));
        cards[46].setImage(new File("Kheart.png"));
        cards[47].setImage(new File("Kspade.png"));
        
        cards[48].setImage(new File("Adiamond.png"));
        cards[49].setImage(new File("Aclub.png"));
        cards[50].setImage(new File("Aheart.png"));
        cards[51].setImage(new File("Aspade.png"));
    }
    
    public void savePlayers()
    {
        try {
            FileOutputStream fos = new FileOutputStream(new File("./list.xml"));
            XMLEncoder encoder = new XMLEncoder(fos);
            encoder.writeObject(saveFile);
            encoder.close();
            fos.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadPlayers()
    {
        try {
            File file = new File("./list.xml");
            if (!file.exists())
            {
                FileOutputStream fos = new FileOutputStream(new File("./list.xml"));
                XMLEncoder encoder = new XMLEncoder(fos);
                encoder.writeObject(new List());
                encoder.close();
                fos.close();
                System.out.println("Creating list.xml file");
            }
            FileInputStream fis = new FileInputStream(file);
            XMLDecoder decoder = new XMLDecoder(fis);
            saveFile = (List)decoder.readObject();
            decoder.close();
            fis.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }           
        for (int i = 0; i < counter; i++)
        {
            player[i] = new PokerPlayer(user[i]);
            dm.setUser(user[i], i);
            saveFile.add(player[i].getID());
            player[i].setBalance(saveFile.getPlayerBalance(player[i].getID()));
        }
    }
    
    public boolean isInRoom(User u)
    {
        for (int i = 0; i < counter; i++)
        {
            if (u.getIdLong() == user[i].getIdLong())
                return true;
        }
        return false;
    }
    
    private class TimerListener implements ActionListener
    {
        public TimerListener() {}
        public void actionPerformed(ActionEvent e)
        {
            System.out.println("hi");
            counter++;
            if (counter == 5)
            {
                counter = 0;
                timer.stop();
            }
        }       
    }
    public int getCounter(){return counter;}
    public void addCounter(){counter++;}
    public void subCounter(){counter--;}
    public void resetCounter(){counter = 0;}
    public void setChannel(MessageChannel ch){channel = ch;}
    public MessageChannel getChannel(int n){return channel;}
    public void setPlayer(User u, int n){user[n] = u;}
    public User getPlayer(int n){return user[n];}
    
}

