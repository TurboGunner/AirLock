package AirLock.AirLock;

import java.util.*;
import java.util.concurrent.*;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.server.invite.InviteBuilder;
import org.javacord.api.entity.server.invite.RichInvite;

public class App {
	
	public static ArrayList<Server> servers = new ArrayList<Server>();
	
    public static void main(String[] args) throws ExecutionException {
		String token = ""; //Enter in your token
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        System.out.println("You can invite this glorious bot by using the following URL: " + api.createBotInvite());
        
        api.addMessageCreateListener(event -> {
        	if(event.getMessage().getContent().indexOf("!createInvite") != -1) {
        		ServerChannel channel = event.getServerTextChannel().get();
                InviteBuilder invite = new InviteBuilder(channel);
                boolean isAdded = false;
                if(servers.size() == 0) {
                	servers.add(event.getServer().get());
                }
                for(int i = 0; i < servers.size(); i++) {
                	if(event.getServer().get().getId() == (servers.get(i).getId())) { 
                		isAdded = true;
            		}
                	if(isAdded) { 
                		System.out.println("Server already added! "); 
                		break; 
                	}
                	if(!isAdded && i == (servers.size() - 1)) {
                		servers.add(event.getServer().get());
                	}
                }
                
                invite.setMaxAgeInSeconds(5 * 60);
                invite.setMaxUses(1);
                Invite link = null;
                
                try {link = invite.create().get(); } 
                catch (Exception e) {
					e.printStackTrace();
				}
                
                event.getChannel().sendMessage(link.getUrl() + "");
        	}
        });
        
        timerLogic(api);
    }
    
    public static void timerLogic(DiscordApi api) {
    	Runnable runnable = () -> {
	    	System.out.println("Running invite check " + servers);
	        for(int i = 0; i < servers.size(); i++) {
	        	servers.set(i, servers.get(i).getCurrentCachedInstance().get());
	        	
	        	ArrayList<RichInvite> invites = null;
    	        try { 
    	        	invites = new ArrayList(Arrays.asList(servers.get(i).getInvites().get().toArray())); 
	        	} 
    	        catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
    	        
    	        for(int j = 0; j < invites.size(); j++) {
    	        	if(invites.get(j).getInviter() != api.getYourself()) {
    	        		invites.get(j).delete();
    	        	}
    	        }
    	    } 
    	};
    	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    	executor.scheduleAtFixedRate(runnable, 0, 7, TimeUnit.SECONDS);
    }
}