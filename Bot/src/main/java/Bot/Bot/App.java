package Bot.Bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.GenericPrivateMessageEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.*;

public class App extends ListenerAdapter
{
	static JDA jda;
	
	Random rng = new Random();
	RPS rps = new RPS();
	Poker poker = new Poker();
	boolean pokerStarted = false;
	private AudioPlayerManager playerManager;
	
    public static void main( String[] args ) throws Exception
    {
        jda = new JDABuilder(AccountType.BOT).setToken(Ref.token).buildBlocking();
        jda.addEventListener(new App());
    }
    
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) 
    {
    	if (event.getAuthor().isBot())
    		return;
    	
    	String word = event.getMessage().getContentRaw();
    	
    	if (word.equalsIgnoreCase("Rock") || word.equalsIgnoreCase("Paper") || word.equalsIgnoreCase("Scissor"))
    	{
    		if (rps.getCounter() == 0)
    		{
    			rps = new RPS();
    			rps.setFirstWord(word);
    			rps.setPlayer(event.getAuthor(), 0);
    			rps.setChannel(event.getChannel(), 0);
    			rps.addCounter();
    			event.getAuthor().getName();
    		}
    		else
    		{
    			rps.resetCounter();
    			rps.setPlayer(event.getAuthor(), 1);
    			rps.setChannel(event.getChannel(), 1);
    			rps.setSecondWord(word);
    			int winner = rps.start();
    			rps.getChannel(0).sendMessage(rps.getPlayer(0).getName()+" rolled "+rps.getFirstWord()).queue();
    			rps.getChannel(0).sendMessage(rps.getPlayer(1).getName()+" rolled "+rps.getSecondWord()).queue();
    			rps.getChannel(1).sendMessage(rps.getPlayer(0).getName()+" rolled "+rps.getFirstWord()).queue();
    			rps.getChannel(1).sendMessage(rps.getPlayer(1).getName()+" rolled "+rps.getSecondWord()).queue();
    			if (winner == 1)
    			{
    				rps.getChannel(0).sendMessage(rps.getPlayer(0).getName()+" wins!").queue();
    				rps.getChannel(1).sendMessage(rps.getPlayer(0).getName()+" wins!").queue();
    			}				
    			else if (winner == 2)
    			{
    				rps.getChannel(0).sendMessage(rps.getPlayer(1).getName()+" wins!").queue();
    				rps.getChannel(1).sendMessage(rps.getPlayer(1).getName()+" wins!").queue();
    			}
    			else
    			{
    				rps.getChannel(0).sendMessage("Tie!").queue();
    				rps.getChannel(1).sendMessage("Tie!").queue();
    			}
    		}  
    	}
        return;
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent evt)
    {
    	//Objects
    	User objUser = evt.getAuthor();
    	MessageChannel objMsgCh= evt.getChannel();
    	Message objMsg = evt.getMessage();
    	
    	//Commands
    	if (objMsg.getContentRaw().equalsIgnoreCase(Ref.prefix+"hi"))
    	{
    		objMsgCh.sendMessage(objUser.getAsMention() + " You're gay!").queue();   		
    	}
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {	
    	if (event.getAuthor().isBot())
    		return;
    	String mess = event.getMessage().getContentRaw();
    	MessageChannel ch = event.getChannel();
    	User user = event.getAuthor();
    	int num = 0;
    	
    	if (isInteger(mess))
    	{
    		String str = mess.substring(13, mess.length());
    		num = Integer.parseInt(str);
    	}
    	
    	if (mess.startsWith("!poker"))
    	{
    		if (!pokerStarted && poker.getCounter() < 6 && mess.equalsIgnoreCase("!poker join"))
    		{
    			if (!poker.isInRoom(user))
    			{
        			poker.setPlayer(user, poker.getCounter());
        			ch.sendMessage(poker.getPlayer(poker.getCounter()).getAsMention()+" has joined the game!").queue();
        			poker.addCounter();
    			}
    			else
    			{
    				ch.sendMessage(user.getAsMention()+", you are already in the room!").queue();
    			}
    		}
    		else if (!pokerStarted && poker.getCounter() >= 6 && mess.equalsIgnoreCase("!poker join"))
    		{
    			ch.sendMessage("There is no more room!").queue();
    		}   		
    		else if (poker.getCounter() > 0 && mess.equalsIgnoreCase("!poker start"))
    		{
    			if (!pokerStarted)
    			{
        			poker.setChannel(event.getChannel());
        			event.getChannel().sendMessage("Poker game is starting!").queue();
        			pokerStarted = true;
    				poker.init();
    			}
    			else
    			{
    				ch.sendMessage("Invalid Poker Command!").queue();
    			}
    		}
    		else if (pokerStarted && mess.equalsIgnoreCase("!poker raise "+num))
    		{
    			poker.turn(user, mess, num);
    			if (poker.gameDone())
    				pokerReset(ch);
    		}
    		else if (pokerStarted && mess.equalsIgnoreCase("!poker check"))
    		{
    			poker.turn(user, mess);
    			if (poker.gameDone())
    				pokerReset(ch);
    		}
    		else if (pokerStarted && mess.equalsIgnoreCase("!poker fold"))
    		{
    			poker.turn(user, mess);
    			if (poker.gameDone())
    				pokerReset(ch);
    		}
    		
    		else if (!pokerStarted && mess.equalsIgnoreCase("!poker leave"))
    		{
    			if (!poker.isInRoom(user))
    				ch.sendMessage(user.getAsMention()+"You are not in the room!").queue();
    			else
    			{
    				poker.subCounter();
    				ch.sendMessage(poker.getPlayer(poker.getCounter()).getAsMention()+". You have left the room!").queue();
        			poker.setPlayer(null, poker.getCounter());
    			}
    			return;
    		}
    		else if (mess.equalsIgnoreCase("!poker balance"))
    		{
    			List saveFile = new List();
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
    			int bal = saveFile.getPlayerBalance(user.getId());
    			ch.sendMessage(user.getAsMention()+"'s balance: "+bal).queue();
    		}
    		else
    		{
    			ch.sendMessage("Invalid Poker Command!").queue();
    		}		  		
    	}
    	else
    		return;
    	
    }
    
    public void pokerReset(MessageChannel c)
    {   	
    	c.sendMessage(jda.getUserById(poker.getWinner()).getName()+ " won!").queue();
    	poker.savePlayers();
    	poker = new Poker();
    	pokerStarted = false;
    }

    public boolean isInteger(String str) 
    {
    	char c = ' ';
    	if (str.length() > 12)
    	{
    		if (str.charAt(12) != ' ')
    			return false;
    		for (int i = 13; i < str.length(); i++)
    		{
    			c = str.charAt(i);
    			if (c < '0' || c > '9')
    				return false;
    		}
    		return true;
    	}
    	return false;
    }
    	
    
    /*@Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
    	if (!event.getMessage().getContentRaw().startsWith("!play")) return;
    	
        Guild guild = event.getGuild();      
        VoiceChannel myChannel = event.getMember().getVoiceState().getChannel();      
    	AudioManager audioManager = guild.getAudioManager();
   
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer player = playerManager.createPlayer();
    	
        AudioSendHandler h = new AudioPlayerSendHandler(player);
        audioManager.setSendingHandler(h);
        audioManager.openAudioConnection(myChannel);
       

        final TrackScheduler trackScheduler = new TrackScheduler();
        player.addListener(trackScheduler);
 
        playerManager.loadItem("https://www.youtube.com/watch?v=U9BwWKXjVaI", new AudioLoadResultHandler() {
        	  public void trackLoaded1(AudioTrack track) {
        	    trackScheduler.queue(track);
        	  }

        	  public void playlistLoaded1(AudioPlaylist playlist) {
        	    for (AudioTrack track : playlist.getTracks()) {
        	      trackScheduler.queue(track);
        	    }
        	  }

        	  public void noMatches() {
        	    // Notify the user that we've got nothing
        	  }

        	  public void loadFailed(FriendlyException throwable) {
        	    // Notify the user that everything exploded
        	  }

			public void playlistLoaded(AudioPlaylist arg0) {
				// TODO Auto-generated method stub
				
			}

			public void trackLoaded(AudioTrack arg0) {
				// TODO Auto-generated method stub
				
			}
        	});
      
        	player.playTrack(player.getPlayingTrack());
    }*/
}
