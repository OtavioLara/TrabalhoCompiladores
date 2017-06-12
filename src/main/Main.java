package main;

import main.IToken;

public class Main {

	public static void main(String[] args) {
		char[] entrada = Entrada.getConteudoArquivo("entradas/entrada_errada.txt");
//		char[] entrada = Entrada.getConteudoArquivo(args[0]);
		Lexico l = new Lexico(entrada);
		Constantes.init();
		System.out.println("\nTokens:\n");
		IToken t = l.getNextToken();
		while (t != null) {
			System.out.println(t);
			t = l.getNextToken();
		}
		System.out.println("\nTabela de simbolos: (identificador=posicao)\n");
		System.out.println(l.getTabelaSimbolos());
	}
}
