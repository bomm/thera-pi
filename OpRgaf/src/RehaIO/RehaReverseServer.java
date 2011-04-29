package RehaIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import opRgaf.OpRgaf;

import org.jdesktop.swingworker.SwingWorker;


public class RehaReverseServer extends SwingWorker<Void,Void>{
	public ServerSocket serv = null;
	StringBuffer sb = new StringBuffer();
	InputStream input = null;
	OutputStream output = null;
	//public int port = 6000;
	public static boolean OpRgafIsActive = false;
	public static boolean offenePostenIsActive = false;
	
	public RehaReverseServer(int x){
		OpRgaf.xport = x;
		execute();
	}	
		
	public String getPort(){
		return Integer.toString(OpRgaf.xport);
	}
	/*****
	 * 
	 * 
	 * 301-er
	 */
	private void doOpRgaf(String op){
		if(op.split("#")[1].equals(RehaIOMessages.IS_STARTET)){
			OpRgaf.thisFrame.setCursor(OpRgaf.thisClass.cdefault);
			OpRgafIsActive = true;
			return;
		}else if(op.split("#")[1].equals(RehaIOMessages.IS_FINISHED)){
			OpRgafIsActive = false;
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                	OpRgaf.thisFrame.toFront();
                	OpRgaf.thisFrame.repaint();
                }
            });
			System.out.println("301-er  Modul beendet");
			return;
		}else if(op.split("#")[1].equals(RehaIOMessages.MUST_PATANDREZFIND)){
		}else if(op.split("#")[1].equals(RehaIOMessages.MUST_PATFIND)){
		}else if(op.split("#")[1].equals(RehaIOMessages.MUST_GOTOFRONT)){
			OpRgaf.thisFrame.setVisible(true);
		}
				
		//JOptionPane.showMessageDialog(null, "Hallo Reha hier spricht das 301-er Modul");
	}
	@Override
	protected Void doInBackground() throws Exception {
			
			while(OpRgaf.xport < 7050){
				try {
					serv = new ServerSocket(OpRgaf.xport);
					break;
				} catch (Exception e) {
					//System.out.println("In Exception währen der Portsuche - 1");
					if(serv != null){
						try {
							serv.close();
						} catch (IOException e1) {
							//System.out.println("In Exception währen der Portsuche - 2");
							e1.printStackTrace();
						}
						serv = null;
					}
					OpRgaf.xport++;
				}
			}
			if(OpRgaf.xport==7050){
				JOptionPane.showMessageDialog(null, "Fehler bei der Initialisierung des IO-Servers (kein Port frei!)");
				OpRgaf.xport = -1;
				serv = null;
				return null;
			}
			OpRgaf.xportOk = true;
			Socket client = null;
			while(true){
				try {
					client = serv.accept();
				} catch (SocketException se) {
					//se.printStackTrace();
					return null;
				}
				sb.setLength(0);
				sb.trimToSize();
				input = client.getInputStream();
				//output = client.getOutputStream();
				int byteStream;
				//String test = "";
				try {
					while( (byteStream =  input.read()) > -1){
						char b = (char)byteStream;
						sb.append(b);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("In Exception währen der while input.read()-Schleife");
				}
				/***************************/
				System.out.println("In OpRgaf - eingegangene Nachricht = "+sb.toString());
				if(sb.toString().startsWith("OpRgaf#")){
					doOpRgaf(String.valueOf(sb.toString()) );
					
				}
			}

//			return null;

		}
	private RehaReverseServer getInstance(){
		return this;
	}
	
	
}