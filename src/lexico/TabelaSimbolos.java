package lexico;

import java.util.HashMap;
import java.util.Set;

public class TabelaSimbolos {
	
	private static Integer posicaoMemoria = 0;
	
	private static HashMap<String, Integer> memoria = new HashMap<>();
	
	public Integer addIdentificador(String identificador){
		Integer posicao = memoria.get(identificador);
		if (posicao == null){
			memoria.put(identificador, posicaoMemoria);
			posicao = TabelaSimbolos.posicaoMemoria;
			TabelaSimbolos.posicaoMemoria++;
		}
		return posicao;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		Set<String> indentificadores = memoria.keySet();
		str.append("Indentificador | Posição \n");
		for(String ident : indentificadores){
			str.append(ident+" : "+memoria.get(ident)+"\n");
		}
		return str.toString();
		//return memoria.toString();		
	}

}
	