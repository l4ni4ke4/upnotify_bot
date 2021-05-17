package upnotify_bot;


import java.util.ArrayList;
import java.util.Arrays;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import utils.Config;
import utils.DatabaseUtils;
//import objects.User;
import utils.MessageUtils;


/**
 * Receives the updates and handles them depending on the respective attributes of the updates.
 *
 * Instances of this class run on a thread taken from the thread pool, 
 * 
 *
 */
public class UpdateReceiver implements Runnable{
	private UpnotifyBot ub;
	private Update update;
	private Message msg;
	private String command;
	//private String[] args;
	private ArrayList<String> args = new ArrayList<String>();
	
	public UpdateReceiver(UpnotifyBot ub, Update update) {
		this.ub = ub;
		this.update = update;
	}

	
	/**
	 * Gets run on a thread from the pool, handles an update.
	 */
	public void run() {
		String threadId = Long.toString(Thread.currentThread().getId());
		
		//System.out.println(update);
		
		
		if (update.hasMessage()) {
			msg = update.getMessage();
			String chatId = msg.getChatId().toString();
			if (msg.hasText()) {
				
				User user = msg.getFrom();
				objects.User upUser;
				if (msg.getChat().getType().contentEquals("private")) {
					upUser = DatabaseUtils.getDatabaseUtils().retrieveUserFromId(user.getId(), user.getUserName());
				} else {
					upUser =  DatabaseUtils.getDatabaseUtils().retrieveUserFromId(msg.getChatId(), msg.getChat().getTitle());
				}
				
		
				
				// check if user exists, otherwise create
				
				
				
				
				String msgText = msg.getText();
		
				System.out.println("Received text: " + (msgText.length() > 20 ? msgText.subSequence(0, 19)  + "..." : msgText));
				// Now, depending on the text we have, and maybe the current state of the situation of our conversation within the group (group id) or with the person (from id), we will handle the message
	
				// Direct text handling, without any importance being given to the conversation stance
				
				// Commands
				/**
				 * Commands can come in forms such as:
				 *	/msginfo@upnotify_bot
				 *	/msginfo
				 *	/msginfo hey heyyy
				 */
				if (msgText.startsWith("/")) {
					
					boolean withArgs = msgText.contains(" ");
					
	
					command = withArgs ? msgText.substring(1, msgText.indexOf(" ")).toLowerCase() : msgText.substring(1);					
					command = command.replace("@" + ub.botUsername, "");
					System.out.println("Running command: " + command);
					args = new ArrayList<String>(Arrays.asList(withArgs ? (msgText.substring(2 + command.length()).split(" ")) : null));
					System.out.println("For args: " + args.toString());
					// TODO logging instead of printing
				
					
					switch (command) {
			
						case "msginfo":
							while (!MessageUtils.getMessageUtils().sendDebugMessage(ub, threadId, chatId, update)) {
								// Logging is to be done within the MessageUtils class, so here printing out would suffice.
								System.out.println("Error whilst sending the message, trying again...");
							}
							break;
						case "checksite":
							for (String arg : args) {
								System.out.println("Working with argument: " + arg);
								// Note that a single thread will work with all of them. If we ever want to change this, we could do these controls within OnUpdateReceived function of UpnotifyBot class, or we could have a separate class for these, and the assignment of jobs to threads could be later etc..
								MessageUtils.getMessageUtils().checkSiteHTTPResponse(ub, threadId, chatId, arg);
							}
							break;
						case "checkstatic":
							for (String arg : args) {
								System.out.println("Working with argument: " + arg);
								MessageUtils.getMessageUtils().checkIfHTMLBodyStatic(ub, chatId, arg);
							}
							break;
						case "help":
							
							MessageUtils.getMessageUtils().sendHelpMessage(ub, chatId, update, upUser);
							break;
							
						case "donothing":
							break;
						case "addrequest":
							// /addrequest snapUrl ss sch
							MessageUtils.getMessageUtils().addRequestAndSendConfirmation(ub, chatId, update, upUser, args);
							break;
						case "editrequest":
							// /editrequest requestId 
							// sch yaziyorsa sch yi kontrol edip kaydeder db ye, yazmiyorsa oraya null yazar
							break;
						case "seerequests":
							// see requests, fields and request ids
							break;
					}
				} else {
					switch (msgText) {
					case "hi":
						while(!MessageUtils.getMessageUtils().sendWelcomeMessage(ub, threadId, chatId, update)) {
							System.out.println("Error whilst sending the message, trying again...");
						}
					}
				}
			}
		}	
	}
}
