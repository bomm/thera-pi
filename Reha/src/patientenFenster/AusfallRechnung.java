package patientenFenster;

import hauptFenster.Reha;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;

import patientenFenster.TherapieBerichte.MyBerichtTableModel;
import systemEinstellungen.SystemConfig;
import systemTools.Colors;
import systemTools.JCompTools;
import systemTools.JRtaCheckBox;
import systemTools.JRtaTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;

public class AusfallRechnung extends RehaSmartDialog implements RehaTPEventListener,WindowListener, ActionListener{
	public JRtaCheckBox[] leistung = {null,null,null,null}; 

	private RehaTPEventClass rtp = null;
	private AusfallRechnungHintergrund rgb;	
	
	public JButton uebernahme;
	public JButton abbrechen;
	

	public AusfallRechnung(Point pt){
		super(null,"AusfallRechnung");		

		
		PinPanel pinPanel = new PinPanel();
		pinPanel.setName("AusfallRechnung");
		pinPanel.getGruen().setVisible(false);
		setPinPanel(pinPanel);
		getSmartTitledPanel().setTitle("Ausfallrechnung erstellen");

		setSize(300,250);
		setPreferredSize(new Dimension(300,250));
		getSmartTitledPanel().setPreferredSize(new Dimension (300,250));
		setPinPanel(pinPanel);
		rgb = new AusfallRechnungHintergrund();
		rgb.setLayout(new BorderLayout());

		
		new SwingWorker<Void,Void>(){

			@Override
			protected Void doInBackground() throws Exception {
				// TODO Auto-generated method stub
				Point2D start = new Point2D.Float(0, 0);
			     Point2D end = new Point2D.Float(PatGrundPanel.thisClass.getWidth(),100);
			     float[] dist = {0.0f, 0.75f};
			     Color[] colors = {Color.WHITE,Colors.Yellow.alpha(0.05f)};
			     LinearGradientPaint p =
			         new LinearGradientPaint(start, end, dist, colors);
			     MattePainter mp = new MattePainter(p);
			     rgb.setBackgroundPainter(new CompoundPainter(mp));		
				return null;
			}
			
		}.execute();	
		rgb.add(getGebuehren(),BorderLayout.CENTER);
		
		getSmartTitledPanel().setContentContainer(rgb);
		getSmartTitledPanel().getContentContainer().setName("AusfallRechnung");
		setName("AusfallRechnung");
		setModal(true);
	    //Point lpt = new Point(pt.x-125,pt.y+30);
		Point lpt = new Point(pt.x-150,pt.y+30);
	    setLocation(lpt);
	    
		rtp = new RehaTPEventClass();
		rtp.addRehaTPEventListener((RehaTPEventListener) this);

		pack();
		
			
	    


	}
	
/****************************************************/	
	/*
	public JButton okknopf;
	public JRtaTextField gegeben;
	public JLabel rueckgeld;
	public JCheckBox direktdruck;
	*/

	private JPanel getGebuehren(){     // 1     2                   3         4     5        6              7
		FormLayout lay = new FormLayout("10dlu,fill:0:grow(0.50),right:80dlu,10dlu,80dlu,fill:0:grow(0.50),10dlu",
									//     1   2  3    4  5   6  7   8  9   10  11  12  13
										"15dlu,p,10dlu,p,2dlu,p,2dlu,p,2dlu,p, 20dlu,p,20dlu");
		PanelBuilder pb = new PanelBuilder(lay);
		CellConstraints cc = new CellConstraints();

		pb.getPanel().setOpaque(false);
		
		pb.addLabel("Bitte die Positionen ausw�hlen die Sie berechnen wollen",cc.xyw(2, 2, 4));

		pb.addLabel("Heilmittel 1",cc.xy(3, 4));
		String lab = (String)PatGrundPanel.thisClass.vecaktrez.get(48);
		leistung[0] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
		leistung[0].setOpaque(false);
		if(!lab.equals("")){
			leistung[0].setSelected(true);			
		}else{
			leistung[0].setSelected(false);
			leistung[0].setEnabled(false);
		}
		pb.add(leistung[0],cc.xyw(5, 4, 2));
		
		pb.addLabel("Heilmittel 2",cc.xy(3, 6));
		lab = (String)PatGrundPanel.thisClass.vecaktrez.get(49);
		leistung[1] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
		leistung[1].setOpaque(false);
		if(!lab.equals("")){
						
		}else{
			leistung[1].setSelected(false);
			leistung[1].setEnabled(false);
		}
		pb.add(leistung[1],cc.xyw(5, 6, 2));

		pb.addLabel("Heilmittel 3",cc.xy(3, 8));
		lab = (String)PatGrundPanel.thisClass.vecaktrez.get(50);
		leistung[2] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
		leistung[2].setOpaque(false);
		if(!lab.equals("")){
						
		}else{
			leistung[2].setSelected(false);
			leistung[2].setEnabled(false);
		}
		pb.add(leistung[2],cc.xyw(5, 8, 2));

		pb.addLabel("Heilmittel 4",cc.xy(3, 10));
		lab = (String)PatGrundPanel.thisClass.vecaktrez.get(51);
		leistung[3] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
		leistung[3].setOpaque(false);
		if(!lab.equals("")){
						
		}else{
			leistung[3].setSelected(false);
			leistung[3].setEnabled(false);
		}
		pb.add(leistung[3],cc.xyw(5, 10, 2));

		
		uebernahme = new JButton("drucken & buchen");
		uebernahme.setActionCommand("uebernahme");
		uebernahme.addActionListener(this);
		uebernahme.addKeyListener(this);
		pb.add(uebernahme,cc.xy(3,12));
		
		abbrechen = new JButton("abbrechen");
		abbrechen.setActionCommand("abbrechen");
		abbrechen.addActionListener(this);
		abbrechen.addKeyListener(this);
		pb.add(abbrechen,cc.xy(5,12));
		
		pb.getPanel().validate();
		return pb.getPanel();
	}
/****************************************************/	
	
	public void RehaTPEventOccurred(RehaTPEvent evt) {
		// TODO Auto-generated method stub
		try{
			if(evt.getDetails()[0] != null){
				if(evt.getDetails()[0].equals(this.getName())){
					this.setVisible(false);
					rtp.removeRehaTPEventListener((RehaTPEventListener) this);
					rtp = null;
					super.dispose();
					this.dispose();
					System.out.println("****************Ausfallrechnung -> Listener entfernt**************");				
				}
			}
		}catch(NullPointerException ne){
			System.out.println("In PatNeuanlage" +evt);
		}
	}
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		if(rtp != null){
			this.setVisible(false);			
			rtp.removeRehaTPEventListener((RehaTPEventListener) this);		
			rtp = null;
			super.dispose();
			dispose();
			System.out.println("****************Ausfallrechnung -> Listener entfernt (Closed)**********");
		}
		
		
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getActionCommand().equals("uebernahme")){
			macheAFRHmap();
			System.out.println(SystemConfig.hmAdrPDaten);
			System.out.println(SystemConfig.hmAdrAFRDaten);
			
			//doUebernahme();
		}
		if(arg0.getActionCommand().equals("abbrechen")){
			this.dispose();
		}

	}
	private void macheAFRHmap(){
		String mappos = "";
		String mappreis = "";
		Double gesamt = new Double(0.00); 
		/*
		List<String> lAdrAFRDaten = Arrays.asList(new String[]{"<AFRposition1>","<AFRposition2>","<AFRposition3>"
				,"<AFRposition4>","<AFRpreis1>","<AFRpreis2>","<AFRpreis3>","<AFRpreis4>","<AFRgesamt>","<AFRnummer>"});
		*/	
		DecimalFormat df = new DecimalFormat( "0.00" );
		
		for(int i = 0 ; i < 4; i++){
			mappos = "<AFRposition"+(i+1)+">";
			mappreis = "<AFRpreis"+(i+1)+">";
			if(leistung[i].isSelected()){
				Double preis = new Double( (String)PatGrundPanel.thisClass.vecaktrez.get(18+i));
				String s = df.format( preis);
				SystemConfig.hmAdrAFRDaten.put(mappos,leistung[i].getText());
				SystemConfig.hmAdrAFRDaten.put(mappreis,s);

				gesamt = gesamt+preis;
			}else{
				SystemConfig.hmAdrAFRDaten.put(mappos,"----");
				SystemConfig.hmAdrAFRDaten.put(mappreis,"0,00");
			}
			
		}
		SystemConfig.hmAdrAFRDaten.put("<AFRgesamt>",df.format( gesamt));
		SystemConfig.hmAdrAFRDaten.put("<AFRnummer>","AF-010101");
	}
	public void keyPressed(KeyEvent event) {
		if(event.getKeyCode()==10){
			event.consume();
			if( ((JComponent)event.getSource()).getName().equals("uebernahme")){
				//doUebernahme();
			}
			if( ((JComponent)event.getSource()).getName().equals("abbrechen")){
				this.dispose();
			}

			System.out.println("Return Gedr�ckt");
		}
	}
	
}
class AusfallRechnungHintergrund extends JXPanel{
	ImageIcon hgicon;
	int icx,icy;
	AlphaComposite xac1 = null;
	AlphaComposite xac2 = null;		
	public AusfallRechnungHintergrund(){
		super();
		hgicon = new ImageIcon(Reha.proghome+"icons/geld.png");
		icx = hgicon.getIconWidth()/2;
		icy = hgicon.getIconHeight()/2;
		xac1 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.15f); 
		xac2 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);			
		
	}
	@Override
	public void paintComponent( Graphics g ) { 
		super.paintComponent( g );
		Graphics2D g2d = (Graphics2D)g;
		
		if(hgicon != null){
			g2d.setComposite(this.xac1);
			g2d.drawImage(hgicon.getImage(), (getWidth()/2)-icx , (getHeight()/2)-icy,null);
			g2d.setComposite(this.xac2);
		}
	}
}