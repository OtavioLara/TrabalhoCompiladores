package semantica;

import java.util.ArrayList;

public class Escopo {
	public String name;
	public ArrayList<Variavel> variaveis = new ArrayList<>();
	
	public class Variavel{
		String lexema;
		Integer posMemoria;
	}
	
	public Variavel getVariavel(String lexema){
		for (Variavel v : variaveis){
			if (v.lexema.equals(lexema)){
				return v;
			}
		}
		return null;
	}
	
	public void insertVariavel(String lexema, Integer posMemoria){
		Variavel var = new Variavel();
		var.lexema = lexema;
		var.posMemoria = posMemoria;
		variaveis.add(var);
	}
}
