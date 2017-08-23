package semantica;

import java.util.HashMap;
import java.util.Set;

import lexico.IToken;

import semantica.ConstantesSemantica.TIPO_DADOS;

public class TabelaSimbolosSemantica {

	
	private static Integer posicaoMemoria = 0;
	
	private static HashMap<String, Simbolo> memoria = new HashMap<>();
	
	public Simbolo insert(String identificador, Escopo escopo, TIPO_DADOS tipo){
		Simbolo simbolo = memoria.get(identificador);
		if (simbolo == null){
			simbolo = new Simbolo();
			simbolo.posMemoria = posicaoMemoria;
			if (escopo == null){
				
			}
			escopo.insertVariavel(identificador, posicaoMemoria);
			posicaoMemoria ++;
			simbolo.escopo = escopo;
			//simbolo.token = token;
			simbolo.tipo = tipo;
			memoria.put(identificador, simbolo);			
			return simbolo;
		}
		return null; //erro ao inserir
		
	}
	
	public Simbolo buscar(String identificador){
		Simbolo simbolo = memoria.get(identificador);
		return simbolo; //se null, erro
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
	}
	
	public class Simbolo{
		//public IToken token;
		public Integer posMemoria;
		public TIPO_DADOS tipo;
		public Escopo escopo;
		public String valor;
		
	}

}
