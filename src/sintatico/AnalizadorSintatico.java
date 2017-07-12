package sintatico;

import java.util.ArrayList;
import lexico.Constantes.TOKEN_CODIGO;
import lexico.IToken;
import lexico.Lexico;

public class AnalizadorSintatico {
	private IToken tokenAtual;
	private ArrayList<IToken> tokens;
	private int pos;

	public void consumir(String s) {
		System.out.println(tokenAtual + " " + s);
		this.pos++;
		tokenAtual = tokens.get(this.pos);
	}

	public AnalizadorSintatico(String entrada) {
		tokens = new Lexico(entrada).getAllTokens();
		this.pos = 0;
		tokenAtual = tokens.get(0);
	}

	private boolean match(String str) {
		return tokenAtual.match(str);
	}

	public void analizar() {
		compilationUnit();
	}

	private void compilationUnit() {
		if (match("package")) {
			consumir("compilationUnit");
			qualifiedIdentifier();
			if (match(";")) {
				consumir("compilationUnit");
			} else {
				// erro
			}
		}
		while (match("import")) {
			consumir("compilationUnit");
			qualifiedIdentifier();
			if (match(";")) {
				consumir("compilationUnit");
			} else {
				// erro
			}
		}
		while (!match("EOF")) {
			typeDeclaration();
		}
		// EOF
		if (match("EOF")) {
			return;
		} else {
			// erro
		}
	}

	private void typeDeclaration() {
		modifier();
		classDeclaration();
	}

	private void qualifiedIdentifier() {
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("qualifiedIdentifier");
			while (match(".")) {
				consumir("qualifiedIdentifier");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
					consumir("qualifiedIdentifier");
				} else {
					// erro
				}
			}
		}
	}

	private void modifier() {
		if (!match("public") && !match("private") && !match("protected") && !match("abstract") && !match("static")) {
			// erro
		}
		while (match("public") || match("private") || match("protected") || match("abstract") || match("static")) {
			consumir("modifier");
		}
	}

	private void classDeclaration() {
		if (match("class")) {
			consumir("classDeclaration");
		} else {
			// no class found error;
		}
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("classDeclaration");
		} else {
			// erro, expected a identifier
		}
		if (match("extends")) {
			qualifiedIdentifier();
		}
		classBody();
	}

	private void classBody() {
		if (match("{")) {
			consumir("classBody");
		} else {
			// erro
		}
		while (!match("}")) {
			modifier();
			memberDecl();
		}
		if (match("}")) {
			consumir("classBody");
		} else {
			// erro
		}
	}

	private void memberDecl() {
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // construtor
			consumir("memberDecl");
			formalParameters();
			block();
		} else if (match("void")) {
			consumir("memberDecl");
			if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // void
				consumir("memberDecl");
				formalParameters();
				if (match(";")) {
					consumir("memberDecl");
				} else {
					block();
				}
			} else {
				// erro
			}
		} else {
			type();
			int pos_ant = this.pos;
			if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // n
																		// void
				consumir("memberDecl");
				if (match("(")) {
					formalParameters();
					if (match(";")) {
						consumir("memberDecl");
					} else {
						block();
					}
				} else {
					this.pos = pos_ant - 1;
					consumir("memberDecl_backtracking");
					variableDeclarators();
				}
			} else {
				// erro
			}
		}
	}

	private void block() {
		if (match("{")) {
			consumir("block");
		} else {
			// erro
		}
		while (!match("}")) {
			blockStatement();
		}
		if (match("}")) {
			consumir("block");
		} else {
			// erro
		}
	}

	private void blockStatement() { // conferir
		localVariableDeclaratiomStatement();
		statement();
	}

	private void statement() {
		if (match("{")) {
			block();
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("statement");
			if (match(":")) {
				consumir("statement");
				statement();
			} else {
				// erro
			}
		} else if (match("if")) {
			consumir("statement");
			parExpression();
			statement();
			if (match("else")) {
				consumir("statement");
				statement();
			}
		} else if (match("while")) {
			consumir("statement");
			parExpression();
			statement();
		} else if (match("return")) {
			consumir("statement");
			if (!match(";")) {
				expression();
			}
			if (match(";")) {
				consumir("statement");
			} else {
				// erro
			}
		} else if (match(";")) {
			consumir("statement");
		} else {
			statementExpression();
			if (match(";")) {
				consumir("statement");
			}
		}

	}

	private void formalParameters() {
		if (match("(")) {
			consumir("formalParameters");
		} else {
			// erro
		}
		if (!match(")")) {
			formalParameter();
			while (match(",")) {
				consumir("formalParameters");
				formalParameter();
			}
		}
		if (match(")")) {
			consumir("formalParameters");
		} else {
			// erro
		}
	}

	private void formalParameter() {
		type();
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("formalParameter");
		} else {
			// erro
		}
	}

	private void parExpression() {
		if (match("(")) {
			consumir("parExpression");
			expression();
		} else {
			// erro
		}
		if (match(")")) {
			consumir("parExpression");
		} else {
			// erro
		}
	}

	private void localVariableDeclaratiomStatement() {
		type();
		variableDeclarators();
	}

	private void variableDeclarators() {
		variableDeclarator();
		if (match(",")) {
			consumir("variableDeclarators");
			variableDeclarator();
		}
	}

	private void variableDeclarator() {
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("variableDeclarator");
			if (match("=")) {
				variableInitializer();
			}
		} else {
			// erro
		}
	}

	private void variableInitializer() {// obede
		if (match("{")) {
			arrayInitializer();
		} else {
			expression();
		}
	}

	private void arrayInitializer() {// otavio
		if (match("{")) {
			consumir("arrayInitializer");
			if (!match("}")) {
				variableInitializer();
				while (match(",")) {
					consumir("arrayInitializer");
					variableInitializer();
				}
			}
			if (match("}")) {
				consumir("arrayInitializer");
			} else {
				// erro
			}
		} else {
			// erro
		}
	}

	private void arguments() {// obede
		if (match("(")) {
			consumir("arguments");
			expression();
			while (match(",")) {
				consumir("arguments");
				expression();
			}
			if (match(")")) {
				consumir("arguments");
			} else {
				// erro
			}
		} else {
			// erro
		}
	}

	private void type() {// otavio
		if (match("boolean") || match("int") || match("char")) {
			consumir("type");
		} else {
			referenceType();
		}
	}

	@Deprecated
	private void basicType() {// obede
		if (match("boolean") || match("char") || match("int")) {
			consumir("basicType");
		} else {
			// erro
		}
	}

	private void referenceType() {// otavio
		if (match("boolean") || match("int") || match("char")) {
			consumir("referenceType");
			if (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					// erro
				}
			} else {
				// erro
			}
			while (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					// erro
				}
			}
		} else {
			qualifiedIdentifier();
			while (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					// erro
				}
			}
		}
	}

	private void statementExpression() {// obede
		expression();
	}

	private void expression() {// otavio
		assignmentExpression();
	}

	private void assignmentExpression() {// obede
		conditionalAndExpression();
		if (match("=") || match("+=")) {
			consumir("assignmentExpression");
			assignmentExpression();
		}
	}

	private void conditionalAndExpression() {// otavio
		equalityExpression();
		while (match("&&")) {
			consumir("conditionalAndExpression");
			equalityExpression();
		}
	}

	private void equalityExpression() {// obede
		relationalExpression();
		while (match("==")) {
			consumir("equalityExpression");
			relationalExpression();
		}
	}

	private void relationalExpression() {// otavio
		additiveExpression();
		if (match(">") || match("<=")) {
			consumir("relationalExpression");
			additiveExpression();
		} else if (match("instanceof")) {
			consumir("relationalExpression");
			referenceType();
		}
	}

	private void additiveExpression() {// obede
		multiplicativeExpression();
		while (match("+") || match("-")) {
			consumir("equalityExpression");
			relationalExpression();
		}
	}

	private void multiplicativeExpression() {// otavio
		unaryExpression();
		while (match("*")) {
			consumir("multiplicativeExpression");
			unaryExpression();
		}
	}

	private void unaryExpression() {// obede
		if (match("++")) {
			consumir("unaryExpression");
			unaryExpression();
		}
		if (match("-")) {
			consumir("unaryExpression");
			unaryExpression();
		} else {
			simpleUnaryExpression();
		}
	}

	private void simpleUnaryExpression() {// otavio
		if (match("!")) {
			unaryExpression();
		} else if (match("(")) {
			consumir("simpleUnaryExpression");
			if (match("boolean") || match("int") || match("char")) {
				consumir("simpleUnaryExpression");
				if (match(")")) {
					consumir("simpleUnaryExpression");
					unaryExpression();
				} else {
					// erro
				}
			} else {
				referenceType();
				if (match(")")) {
					consumir("simpleUnaryExpression");
					simpleUnaryExpression();
				} else {
					// erro
				}
			}
		} else {
			postfixExpression();
		}
	}

	private void postfixExpression() {// obede
		primary();
		while (match(".") || match("[")) {
			selector();
		}
		while (match("--")) {
			consumir("postifixExpression");
		}
	}

	private void selector() {// otavio
		if (match(".")) {
			qualifiedIdentifier();
			if (match("(")) {
				arguments();
			}
		} else if (match("[")) {
			consumir("selector");
			expression();
			if (match("]")) {
				consumir("selector");
			} else {
				// erro
			}
		} else {
			// erro
		}
	}

	private void primary() {// obede
		if (match("(")) {
			parExpression();
		} else if (match("this")) {
			if (match("(")) {
				arguments();
			}
		} else if (match("super")) {
			if (match("(")) {
				arguments();
			} else if (match(".")) {
				consumir("primary");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
					consumir("primary");
				} else {
					// erro
				}
				if (match("(")) {
					arguments();
				}
			} else {
				// erro
			}
		} else if (match("new")) {
			creator();
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			qualifiedIdentifier();
			if (match("(")) {
				arguments();
			}
		} else {
			literal();
		}
	}

	private void creator() {// otavio
		if (match("boolean") || match("int") || match("char")) {
			consumir("creator");
		} else {
			qualifiedIdentifier();
		}
		if (match("(")) {
			arguments();
		} else if (match("[")) {
			int pos_ant = this.pos;
			consumir("creator");
			if (match("]")) {
				consumir("creator");
				while (match("[")) {
					consumir("creator");
					if (match("]")) {
						consumir("creator");
					} else {
						// erro
					}
				}
				if (match("{")) {
					arrayInitializer();
				}
			} else {
				this.pos = pos_ant - 1;
				consumir("creator");
				newArrayDeclarator();
			}
		}
	}

	private void newArrayDeclarator() {// obede
		if (match("[")) {
			consumir("newArrayDeclarator");
			expression();
		} else {
			// erro
		}
		if (match("]")) {
			consumir("newArrayDeclarator");
		} else {
			// erro
		}
		while (match("[")) {
			consumir("newArrayDeclarator");
			expression();
			if (match("]")) {
				consumir("newArrayDeclarator");
			} else {
				// erro
			}
		}
		while (match("[")) {
			consumir("newArrayDeclarator");
			if (match("]")) {
				consumir("newArrayDeclarator");
			} else {
				// erro
			}
		}
	}

	private void literal() {// otavio
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.INT_LITERAL) {
			consumir("literal");
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.CHAR_LITERAL) {
			consumir("literal");
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.STRING_LITERAL) {
			consumir("literal");
		} else if (match("true")) {
			consumir("literal");
		} else if (match("false")) {
			consumir("literal");
		} else if (match("null")) {
			consumir("literal");
		} else {
			// erro
		}
	}

	public static void main(String[] args) {
		AnalizadorSintatico as = new AnalizadorSintatico("entradas/entrada_errada_Durelli");
		as.analizar();
	}
}
