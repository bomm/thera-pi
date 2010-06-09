package errorMail;

import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import emailHandling.EmailSendenExtern;
import systemEinstellungen.INIFile;
import systemEinstellungen.SystemConfig;
import hauptFenster.Reha;

public class ErrorMail extends Thread{
	String fehlertext = null;
	String maschine = null;
	String benutzer = null;
	String sender = null;
	
	public ErrorMail(String text,String comp,String user,String senderadress){
		super();
		this.fehlertext = String.valueOf(text);
		this.maschine = String.valueOf(comp);
		this.benutzer = String.valueOf(user);
		this.sender = String.valueOf(senderadress);
		start();
	}
	public void run(){
		INIFile ini = new INIFile(Reha.proghome+"ini/"+Reha.aktIK+"/error.ini");
		String empfaenger = ini.getStringProperty("Email", "RecipientAdress");
		   EmailSendenExtern oMail = new EmailSendenExtern();
			String smtphost = SystemConfig.hmEmailIntern.get("SmtpHost");
			String authent = SystemConfig.hmEmailIntern.get("SmtpAuth");
			String benutzer = SystemConfig.hmEmailIntern.get("Username") ;				
			String pass1 = SystemConfig.hmEmailIntern.get("Password");
			String sender = SystemConfig.hmEmailIntern.get("SenderAdresse"); 
			String recipient = empfaenger;
			ArrayList<String[]> attachments = new ArrayList<String[]>();
			boolean authx = (authent.equals("0") ? false : true);
			boolean bestaetigen = false;
			String emailtext = this.fehlertext+"\n"+
			"ausgelöst am Computer: "+this.maschine+"\n"+
			"eingeloggter Benutzer: "+this.benutzer+"\n"+
			"Absenderadresse: "+this.sender;
			try {
				oMail.sendMail(smtphost, benutzer, pass1, sender, recipient, "Fehler-Mail", emailtext,attachments,authx,bestaetigen);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		
		
	}

}
