package hauptFenster;

import java.awt.Container;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;



public class AktiveFenster {
	
	public AlleFenster[] aktiveFenster[] = null;
	public static Vector Fenster = new Vector();

	public static void Init(){
		
		
	}	

	public static void setNeuesFenster(String name,JComponent referenz,int position,Container feltern){
		AlleFenster neuFenst = new AlleFenster(name , referenz , position,feltern);
		Fenster.add(neuFenst);
		System.out.println(((AlleFenster)Fenster.get(Fenster.size()-1)).fname);
		System.out.println(((AlleFenster)Fenster.get(Fenster.size()-1)).freferenz);
		System.out.println(Fenster.size());
	}
	
	public static void loescheFenster(String name){
		int i;
		System.out.println("Aktive-Fenster: "+name+" wird gel�scht");
		System.out.println("FensterElemente vor l�schen "+Fenster.size());		
		for(i=0;i<Fenster.size();i++){
			if((boolean)((AlleFenster)Fenster.get(i)).fname.equals(name)){
				System.out.println("Setze freferenz auf null!");
				((AlleFenster)Fenster.get(i)).freferenz = null;
				Fenster.removeElementAt(i);
				Fenster.trimToSize();
				SwingUtilities.invokeLater(new Runnable(){
				 	   public  void run()
				 	   {
				 			System.out.println("Total Memory  = "+Runtime.getRuntime().totalMemory());    
				 		    System.out.println("Free Memory   = "+Runtime.getRuntime().freeMemory());
				 		    Runtime r = Runtime.getRuntime();
				 		    r.gc();
				 		    long freeMem = r.freeMemory();
				 		    System.out.println("Freed Memory  = "+freeMem);
				 	   }
				});				
				break;
			}
		}
		System.out.println("FensterElemente nach l�schen "+Fenster.size());
	}
	public static JComponent getFenster(String name){
		int i;
		JComponent gef = null;
		for(i=0;i<Fenster.size();i++){
			if((boolean)((AlleFenster)Fenster.get(i)).fname.equals(name)){
				gef = ((AlleFenster)Fenster.get(i)).freferenz;
				break;
			}
		}
		return gef; 
	}
	
	public static JComponent getFensterAlle(String name){
		int i;
		JComponent gef = null;
		for(i=0;i<Fenster.size();i++){
			if((boolean)((AlleFenster)Fenster.get(i)).fname.contains(name)){
				gef = ((AlleFenster)Fenster.get(i)).freferenz;
				break;
			}
		}
		return gef; 
	}

	public static String getFensterByName(String name){
		int i;
		String gef = "";
		for(i=0;i<Fenster.size();i++){
			if((boolean)((AlleFenster)Fenster.get(i)).fname.equals(name)){
				gef = ((AlleFenster)Fenster.get(i)).fname;
				break;
			}
		}
		return gef; 
	}
	
}

/********************************/
class AlleFenster implements Comparable<AlleFenster> {
	  String fname="";
	  JComponent freferenz=null;;
	  int fposition;
	  Container feltern=null;

	  public AlleFenster (String n,JComponent r, int f,Container e)   {
	    fname = n;
	    freferenz = r;
	    fposition = f;
	    feltern = e;
	  }
	  public int compareTo(AlleFenster o) {
	      //First order by name
	      int result = fname.compareTo(o.fname);
	      if (0 == result) {
	        //if names are equal order by age, youngest first
	        //result = fname - o.fname;
	      }
	      return result;
}
} 



