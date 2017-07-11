package lexico;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Entrada {
	
	
	public static char[] getConteudoArquivo(String file){
		try {
			FileReader fr = new FileReader(file);
			BufferedReader buffer = new BufferedReader(fr);
			StringBuilder conteudo = new StringBuilder("");
			while (buffer.ready()) {
				conteudo.append((char)buffer.read());
			}
			buffer.close();
			return conteudo.toString().toCharArray();
		} catch (FileNotFoundException e) {
			System.out.println("Arquivo nao encontrado");
			return null;
		} catch (IOException e) {
			System.out.println("Arquivo com formato errado");
			return null;
		}
	}

}
