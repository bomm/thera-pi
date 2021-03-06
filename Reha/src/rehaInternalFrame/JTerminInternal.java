package rehaInternalFrame;

import hauptFenster.AktiveFenster;
import hauptFenster.FrameSave;
import hauptFenster.Reha;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.InternalFrameEvent;

import CommonTools.RehaEvent;
import CommonTools.RehaEventClass;
import CommonTools.RehaEventListener;

public class JTerminInternal extends JRehaInternal implements RehaEventListener{
	/**
	 * 
	 */
	public static boolean inIniSave = false;
	private static final long serialVersionUID = -3063788551323647566L;
	RehaEventClass rEvent = null;
	public JTerminInternal(String titel, ImageIcon img, int desktop) {
		super(titel, img, desktop);
		rEvent = new RehaEventClass();
		rEvent.addRehaEventListener((RehaEventListener) this);
		addInternalFrameListener(this);
		// TODO Auto-generated constructor stub
	}
	/*
	public void internalFrameActivated(InternalFrameEvent arg0) {
		isActive = true;
		TerminFenster.thisClass.getViewPanel().requestFocus();
		toFront();
		super.repaint();
		frameAktivieren(super.getName());
	}
	*/
	@Override
	public void internalFrameClosed(InternalFrameEvent arg0) {
		if(! this.isIcon && !inIniSave ){
			inIniSave = true;
			new FrameSave ((Dimension)this.getSize().clone(), 
					(Point)this.getLocation().clone(), 
					(Integer) Integer.valueOf(this.desktop), 
					(Integer) Integer.valueOf((this.getImmerGross()? 1 : 0)),
					String.valueOf("kalender.ini"),
					String.valueOf("Kalender"));	
		}

		Reha.thisClass.desktops[this.desktop].remove(this);
		this.removeInternalFrameListener(this);
		Reha.thisFrame.requestFocus();
		Reha.thisClass.aktiviereNaechsten(this.desktop);
		this.removeAll();
		////System.out.println("Lösche Termin Internal von Desktop-Pane = "+Reha.thisClass.desktops[this.desktop]);
		////System.out.println("Termin-Internal geschlossen***************");

		rEvent.removeRehaEventListener((RehaEventListener) this);
		if(Reha.thisClass.terminpanel != null){
			try{
				Reha.thisClass.terminpanel.db_Aktualisieren.interrupt();
			}catch(Exception ex){
				
			}
		}
		if(Reha.thisClass.terminpanel != null){
			Reha.thisClass.terminpanel = null;			
		}
		
		Reha.thisClass.terminpanel = null;
		this.nord = null;
		this.inhalt = null;
		this.thisContent = null;
		this.dispose();
		super.dispose();
		AktiveFenster.loescheFenster(this.getName());
		Reha.thisClass.progLoader.loescheTermine();
		/*
		SwingUtilities.invokeLater(new Runnable(){
		 	   public  void run()
		 	   {
		 			Runtime r = Runtime.getRuntime();
		 		    r.gc();
		 		    long freeMem = r.freeMemory();
		 		    ////System.out.println("Freier Speicher nach  gc():    " + freeMem);
		 	   }
		});
		*/
	}	

	@Override
	public void rehaEventOccurred(RehaEvent evt) {
		if(evt.getRehaEvent().equals("REHAINTERNAL")){
			////System.out.println("es ist ein Reha-Internal-Event");
		}
		if(evt.getDetails()[0].equals(this.getName())){
			if(evt.getDetails()[1].equals("#ICONIFIED")){
				try {
					this.setIcon(true);
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.setActive(false);
			}
		}
	}
}
final class JDesktopIcon extends JComponent implements Accessible
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * DOCUMENT ME!
   */
  protected class AccessibleJDesktopIcon extends AccessibleJComponent
    implements AccessibleValue
  {
    private static final long serialVersionUID = 5035560458941637802L;
    /**
     * Creates a new AccessibleJDesktopIcon object.
     */
    protected AccessibleJDesktopIcon()
    {
	super();
    }
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AccessibleRole getAccessibleRole()
    {
	return null;
    }
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public AccessibleValue getAccessibleValue()
    {
	return null;
    }
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Number getCurrentAccessibleValue()
    {
	return null;
    }
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Number getMaximumAccessibleValue()
    {
	return null;
    }
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Number getMinimumAccessibleValue()
    {
	return null;
    }
    /**
     * DOCUMENT ME!
     *
     * @param n DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean setCurrentAccessibleValue(Number n)
    {
	return false;
    }
  }
}  

