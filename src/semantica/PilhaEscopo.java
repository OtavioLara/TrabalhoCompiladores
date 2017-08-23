package semantica;

import java.util.ArrayList;

public class PilhaEscopo {
	private static ArrayList<Escopo> escopos = new ArrayList<>();
	private static int lastPos = -1;
	
	public static int insertEscopo(Escopo e){
		escopos.add(e);
		lastPos ++;
		return lastPos;
	}
	
	public static Escopo getLastEscopo(){
		if (lastPos >= 0){
			return escopos.get(lastPos);
		}
		return null;
	}
	
	public static Escopo removeLastEscopo(){
		Escopo last = escopos.get(lastPos);
		escopos.remove(last);
		lastPos--;
		return last;
	}
	
	public static Escopo getEscopo(int index){
		return escopos.get(index);
	}
	

}
