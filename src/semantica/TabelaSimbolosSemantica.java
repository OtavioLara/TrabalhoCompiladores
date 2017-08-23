package semantica;

import java.util.HashMap;
import java.util.Set;

import lexico.IToken;

import semantica.ConstantesSemantica.TIPO_DADOS;

public class TabelaSimbolosSemantica {

	
	private static int posicaoMemoria = 0;
	
	private static HashMap<String, Simbolo> memoria = new HashMap<>();
	
	public Simbolo insert(String identificador, Escopo escopo, TIPO_DADOS tipo){
		Simbolo simbolo = memoria.get(identificador);
		if (simbolo == null){
			simbolo = new Simbolo();
			simbolo.posMemoria = posicaoMemoria;
			escopo.insertVariavel(identificador, posicaoMemoria);
			posicaoMemoria ++;
			simbolo.escopo = escopo;
			//simbolo.token = token;
			simbolo.tipo = tipo;
			simbolo.identificador = identificador;
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
		str.append("\n\n\t\tTabela Símbolos\n\n");
		for(String ident : indentificadores){
			str.append(memoria.get(ident)+"\n");
		}
		return str.toString();
	}
	
	public class Simbolo{
		//public IToken token;
		public String identificador;
		public int posMemoria;
		public TIPO_DADOS tipo;
		public Escopo escopo;
		//public String valor;
		
		@Override
		public String toString() {
			return "ID: " + identificador + ", memoria: " + posMemoria + ",Tipo: " + tipo;
		}
		
	}

}
