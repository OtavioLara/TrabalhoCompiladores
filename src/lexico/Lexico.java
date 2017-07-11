package lexico;

import java.util.ArrayList;

import lexico.Constantes.TOKEN_CODIGO;
import lexico.Constantes.TOKEN_CODIGO_ERRO;

public class Lexico {

	private ArrayList<IToken> tokens = new ArrayList<>();
	private char[] codEntrada;
	private int pos;
	private TabelaSimbolos tabelaSimbolos;
	private int linhaAtual;

	public Lexico(String nomeArq) {
		char[] entrada = Entrada.getConteudoArquivo(nomeArq);
		this.codEntrada = entrada;
		this.pos = 0;
		this.tokens = new ArrayList<>();
		this.tabelaSimbolos = new TabelaSimbolos();
		this.linhaAtual = 1;
	}
	public ArrayList<IToken> getAllTokens() {
		int pos_bk = pos;
		ArrayList<IToken> tokens_bk = this.tokens;
		this.pos = 0;
		this.tokens = new ArrayList<>();
		IToken token = this.getNextToken();
		while (token.tokenTipo() != TOKEN_CODIGO.EOF) {
			token = this.getNextToken();
		}
		ArrayList<IToken> tokensFinal = this.tokens;
		this.pos = pos_bk;
		this.tokens = tokens_bk;
		return tokensFinal;
	}

	public IToken getNextToken() {
		int start;
		int end;
		while (pos < codEntrada.length) {
			if (codEntrada[pos] == '\n') { // nova linha
				this.linhaAtual++;
				pos++;
			} else if (codEntrada[pos] == '\r' || codEntrada[pos] == '\t' || codEntrada[pos] == '\u000c'
					|| codEntrada[pos] == ' ') {// espaco em branco
				pos++;
			} else if (codEntrada[pos] == '/') {
				start = pos;
				end = pos;
				pos++;
				if (codEntrada[pos] == '/') {
					pos++;
					while (codEntrada[pos] != '\n') {
						end = pos;
						pos++;
					}
					if (codEntrada[pos] == '\n') { // comentario
						this.linhaAtual++;
						pos++;
					}
				} else {
					IToken token = new TokenErro(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO_ERRO.OPERADOR_INDEFINIDO, this.linhaAtual);
					tokens.add(token);
					return token;
				}
			} else if (codEntrada[pos] == '=') {
				start = pos;
				end = pos;
				pos++;
				if (codEntrada[pos] == '=') {
					end = pos;
					pos++;
				}
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.OPERATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '+') {
				start = pos;
				end = pos;
				pos++;
				if (codEntrada[pos] == '+' || codEntrada[pos] == '=') {
					end = pos;
					pos++;
				}
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.OPERATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '>') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.OPERATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '!') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.OPERATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '*') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.OPERATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '&') {
				start = pos;
				end = pos;
				pos++;
				if (codEntrada[pos] == '&') {
					end = pos;
					pos++;
					IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.OPERATOR,
							this.linhaAtual);
					tokens.add(token);
					return token;
				} else {
					IToken token = new TokenErro(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO_ERRO.OPERADOR_INDEFINIDO, this.linhaAtual);
					tokens.add(token);
					return token;
				}
			} else if (codEntrada[pos] == '<') {
				start = pos;
				end = pos;
				pos++;
				if (codEntrada[pos] == '=') {
					end = pos;
					pos++;
					IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.OPERATOR,
							this.linhaAtual);
					tokens.add(token);
					return token;
				} else {
					IToken token = new TokenErro(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO_ERRO.OPERADOR_INDEFINIDO, this.linhaAtual);
					tokens.add(token);
					return token;
				}
			} else if (codEntrada[pos] == '-') {
				start = pos;
				end = pos;
				pos++;
				if (codEntrada[pos] == '-') {
					end = pos;
					pos++;
				}
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.OPERATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == ',') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '.') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == ';') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '[') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == ']') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '{') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '}') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '(') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == ')') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.SEPARATOR,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '0') {
				start = pos;
				end = pos;
				pos++;
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.INT_LITERAL,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if (codEntrada[pos] == '"') {// comentario
				start = pos;
				end = pos;
				pos++;
				while (codEntrada[pos] != '"' && codEntrada[pos] != '\n') {
					end = pos;
					pos++;
				}
				if (codEntrada[pos] == '"') {
					end = pos;
					pos++;
					IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO.STRING_LITERAL, this.linhaAtual);
					tokens.add(token);
					return token;
				} else if (codEntrada[pos] == '\n') {
					IToken token = new TokenErro(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO_ERRO.STRING_INDEFINIDA, this.linhaAtual);
					tokens.add(token);
					return token;
				}
			} else if (codEntrada[pos] == '\'') {
				start = pos;
				end = pos;
				pos++;
				if (codEntrada[pos] != '\'' && codEntrada[pos] != '\n') {
					end = pos;
					pos++;
				}
				if (codEntrada[pos] == '\'') {
					end = pos;
					pos++;
					IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO.CHAR_LITERAL, this.linhaAtual);
					tokens.add(token);
					return token;
				} else if (codEntrada[pos] == '\n') {
					IToken token = new TokenErro(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO_ERRO.SIMBOLO_INDEFINIDO, this.linhaAtual);
					tokens.add(token);
					return token;
				}
			} else if ('1' <= codEntrada[pos] && '9' >= codEntrada[pos]) {
				start = pos;
				end = pos;
				pos++;
				while ('0' <= codEntrada[pos] && '9' >= codEntrada[pos]) {
					end = pos;
					pos++;
				}
				IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1), TOKEN_CODIGO.INT_LITERAL,
						this.linhaAtual);
				tokens.add(token);
				return token;
			} else if ((codEntrada[pos] >= 'a' && codEntrada[pos] <= 'z')
					|| (codEntrada[pos] >= 'A' && codEntrada[pos] <= 'Z') || codEntrada[pos] == '_'
					|| codEntrada[pos] == '$') {
				start = pos;
				end = pos;
				pos++;
				while ((codEntrada[pos] >= 'a' && codEntrada[pos] <= 'z')
						|| (codEntrada[pos] >= 'A' && codEntrada[pos] <= 'Z') || codEntrada[pos] == '_'
						|| codEntrada[pos] == '$' || (codEntrada[pos] <= '9' && codEntrada[pos] >= '0')) {
					end = pos;
					pos++;
				}
				String id = String.valueOf(codEntrada, start, end - start + 1);
				if (isReservedWord(id)) {
					IToken token = new Token(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO.RESERVED_WORD, this.linhaAtual);
					tokens.add(token);
					return token;
				} else {
					IToken token = new TokenIdentificador(String.valueOf(codEntrada, start, end - start + 1),
							TOKEN_CODIGO.IDENTIFICADOR, this.tabelaSimbolos.addIdentificador(id), this.linhaAtual);
					tokens.add(token);
					return token;
				}
			} else {
				pos++;
				return new TokenErro(String.valueOf(this.codEntrada[this.pos-1]), TOKEN_CODIGO_ERRO.INDEFINIDO,
						this.linhaAtual);
			}
		}
		IToken eof = new Token("EOF",TOKEN_CODIGO.EOF, linhaAtual); 
		tokens.add(eof);
		return eof;
	}

	public boolean isReservedWord(String id) {
		return Constantes.getInstance().reserved_word.contains(id);
	}

	public TabelaSimbolos getTabelaSimbolos() {
		return this.tabelaSimbolos;
	}

}
