package textBlockTherapeuten;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import systemTools.JCompTools;
import systemTools.JRtaTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TbEingabeNeu extends JXPanel implements ActionListener,KeyListener,FocusListener{
	JButton uebernahme = null;
	JButton zurueck = null;
	JButton abbrechen = null;
	Vector<String> varvec = null;
	ThTextBlock thbl = null;
	JRtaTextField rueck = null;
	String tbltext = "";
	String savetext = "";
	JLabel[] jlab = {null,null};
	JTextArea jta = null;
	JXLabel darstellung = null;
	JEditorPane editorPane = null;
		
	JRtaTextField tbeingabe = null;
	
	int aktvector = 0;
	int maxvector = 0;
	String aktreplace = "";
	
	public TbEingabeNeu(String dtext,Vector vec,ThTextBlock thb,JRtaTextField jtf){
		super();
		setOpaque(false);
		setLayout(new BorderLayout());
		this.tbltext = new String(dtext);
		String x = new String(dtext);
		x = x.replaceAll("\\^Tab\\^","&nbsp;");
		x = x.replaceAll("CRLF","<br>");
		this.savetext = "<html>"+x+"</html>";
		System.out.println("Savetext nach dem Replace " +this.savetext);
		this.varvec = vec;
		this.thbl = thb;
		this.rueck = jtf;
		this.maxvector = vec.size()-1;
		add(getFelderPanel(),BorderLayout.CENTER);
		add(getButtonPanel(),BorderLayout.SOUTH);		
		validate();
		macheTitel(0);
	}
	private JPanel getFelderPanel(){
		FormLayout lay = new FormLayout(
				//x   1    2          3           4         5          6    
					"5dlu,0dlu,right:max(40dlu;p),2dlu,fill:300dlu:grow(1.00),5dlu",
				//y	  1   2   3       4                  5    6     7                
					"5dlu,p,20dlu,fill:100dlu:grow(1.00),15dlu"
					);
		PanelBuilder pb = new PanelBuilder(lay);
		pb.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();
		pb.addLabel("Wert eingeben",cc.xy(3,2));
		tbeingabe = new JRtaTextField("NIX",true);
		tbeingabe.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode()==10){
					arg0.consume();
					machText();
				}
				if(arg0.getKeyCode()==27){
					arg0.consume();
					rueck.setText("");
					thbl.wechsleRueckwaerts();
				}

			}	
			
			
			
		});
		pb.add(tbeingabe,cc.xy(5,2));
	
		darstellung = new JXLabel();
		darstellung.setVerticalAlignment(JLabel.TOP);
		darstellung.setLineWrap(true);
		
		darstellung.setFont(new Font("Courier",Font.PLAIN,12));
		darstellung.setForeground(Color.BLUE);
		testeAufRot();

		pb.add(darstellung,cc.xyw(3,4,3));
		return pb.getPanel();
	}
	
	/****************************************/
	
	public void neueDaten(String dtext,Vector vec){
		this.tbltext = new String(dtext);
		String x = new String(dtext);
		x = x.replaceAll("\\^Tab\\^","&nbsp;");
		x = x.replaceAll("CRLF","<br>");
		this.savetext = new String("<html>"+x+"</html>");
		this.varvec = vec;
		this.aktvector = 0;
		this.maxvector = vec.size()-1;
		testeAufRot();
		tbeingabe.setText("");
		System.out.println("Neudaten = "+this.savetext);
		macheTitel(0);
	}
	
	/****************************************/
	
	private JPanel getButtonPanel(){
		FormLayout lay = new FormLayout(
		        // 1                2          3             4      5    
				"fill:0:grow(0.33),50dlu,fill:0:grow(0.33),50dlu,fill:0:grow(0.33)",
				// 1  2  3  
				"5dlu,p,5dlu");
		CellConstraints cc = new CellConstraints();
		PanelBuilder pb = new PanelBuilder(lay);
		pb.getPanel().setOpaque(false);
		uebernahme = new JButton("�bernehmen");
		uebernahme.setActionCommand("uebernahme");
		uebernahme.setName("uebernahme");
		uebernahme.addActionListener(this);
		uebernahme.addKeyListener(this);
		uebernahme.setMnemonic(KeyEvent.VK_U);
		pb.add(uebernahme,cc.xy(2,2));
		
		abbrechen = new JButton("abbrechen");
		abbrechen.setActionCommand("abbrechen");
		abbrechen.setName("abbrechen");
		abbrechen.addActionListener(this);
		abbrechen.addKeyListener(this);
		abbrechen.setMnemonic(KeyEvent.VK_A);		
		pb.add(abbrechen,cc.xy(4,2));		
		return pb.getPanel();
	}
	
	/********************************************************************/
	private void machText(){
		if(aktvector > maxvector){
			JOptionPane.showMessageDialog(null,"In diesem Textbaustein sind keine weiteren Variablen vorhanden");
			return;
		}
		
		if(varvec.get(aktvector).contains("^Tab^")){
			int i = aktvector;
			while(i <= maxvector){
				if(! varvec.get(i).contains("^Tab^")){
					break;
				}
				i++;
			}
			if(i > maxvector){
				werteUebergeben();
				System.out.println("nach der while schleife");
				this.thbl.wechsleRueckwaerts();
				return;
			}
			aktvector = i;
		}

		this.savetext = this.savetext.replace(aktreplace, "<b><font color='#000000'>"+tbeingabe.getText().trim()+"</font></b>");

			aktvector++;
			
			// 1.
			if(aktvector > maxvector){
				System.out.println("nach 1.");
				aktvector--;
				testeAufRot();
				this.savetext = this.savetext.replace(aktreplace, "<b><font color='#000000'>"+tbeingabe.getText().trim()+"</font></b>");
				werteUebergeben();
				this.thbl.wechsleRueckwaerts();				
				return;
			}
			/****** testen ob tag****/
			int i = aktvector;
			while(i <= maxvector){
				if(! varvec.get(i).contains("^Tab^")){
					break;
				}
				i++;
			}
			//2,
			if(i > maxvector){
				System.out.println("nach 2.");
				werteUebergeben();
				this.thbl.wechsleRueckwaerts();
				return;
			}
			aktvector = i;

			if(aktvector <= maxvector){
				testeAufRot();
				macheTitel(aktvector);
				tbeingabe.setText("");
			//3,
			}else{
				System.out.println("nach 3.");
				werteUebergeben();
				this.thbl.wechsleRueckwaerts();
				return;
			}

	}
	private void werteUebergeben(){
		String x = darstellung.getText();
		x = x.replaceAll("&nbsp;", "\\\t");
		x = x.replaceAll("<br>","\\\n");
		x = x.replaceAll("</b>","");
		x = x.replaceAll("<b>","");
		x = x.replaceAll("</font>","");		
		x = x.replaceAll("<font color='#ff0000'>","");		
		x = x.replaceAll("<font color='#000000'>","");
		x = x.replaceAll("<font color='#ffffff'>","");
		x = x.replaceAll("</html>","");		
		x = x.replaceAll("<html>","");
		//JOptionPane.showMessageDialog(null,x);
		this.rueck.setText(new String(x));
	
	}
	private void testeAufRot(){
		if(aktvector > maxvector){
			String titel = "<html><b>Keine weiteren Variablen vorhanden</b>";
			thbl.setzeEingabeTitel(titel);
			this.thbl.wechsleRueckwaerts();
		}
		if(varvec.get(aktvector).contains("^Tab^")){
			System.out.println("Tab gefunden "+this.savetext);
			return;
		}
		aktreplace = "<b><font color='#ff0000'>"+varvec.get(aktvector)+"</font></b>";
		this.savetext = new String(this.savetext.replace(varvec.get(aktvector), aktreplace));
		darstellung.setText(this.savetext);
		darstellung.repaint();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("abbrechen")){
			this.rueck.setText("");
			this.thbl.wechsleRueckwaerts();	
		}
		if(cmd.equals("uebernahme")){
			werteUebergeben();
			this.thbl.wechsleRueckwaerts();	
		}
		
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getKeyCode()==10){
			if( ((JComponent)arg0.getSource()).getName() != null) {
				if( ((JComponent)arg0.getSource()).getName().equals("abbrechen")){
					this.rueck.setText("");
					this.thbl.wechsleRueckwaerts();
				}
				if( ((JComponent)arg0.getSource()).getName().equals("uebernahme")){
					werteUebergeben();
					this.thbl.wechsleRueckwaerts();
				}

		}

		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	private void macheTitel(int titel){
		final int xtitel = titel;
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				String titel = "<html>Bitte Wert eingeben f�r -->&nbsp;&nbsp;<b>"+varvec.get(xtitel)+"</b>";
				thbl.setzeEingabeTitel(titel);
				tbeingabe.requestFocus();
				return null;
			}
			
		}.execute();
		
	}
}
