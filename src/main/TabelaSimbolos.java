package main;

import java.util.HashMap;
import java.util.Map;

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
		return memoria.toString();		
	}

}
