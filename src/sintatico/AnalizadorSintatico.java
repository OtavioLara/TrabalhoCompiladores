package sintatico;

import java.util.ArrayList;
import lexico.Constantes.TOKEN_CODIGO;
import lexico.IToken;
import lexico.Lexico;
import lexico.Token;

public class AnalizadorSintatico {
	private IToken tokenAtual;
	private ArrayList<IToken> tokens;
	private int pos;
	private ArrayList<ErroSintatico> erros;

	public void consumir(String s) {
//		System.out.println(tokenAtual + " " + s);
		this.pos++;
		if (pos < tokens.size()) {
			tokenAtual = tokens.get(this.pos);
		} else {
			tokenAtual = null;
			mostrarErros();
		}
	}
	
	private void mostrarErros(){
		if (erros.size() == 0){
			System.out.println("\n\n\n******* Não há erros *************");
		}
		for (ErroSintatico e : this.erros){
			System.out.println(e);
		}
		System.exit(0);
	}

	public AnalizadorSintatico(String entrada) {
		tokens = new Lexico(entrada).getAllTokens();
		this.pos = 0;
		tokenAtual = tokens.get(0);
		erros = new ArrayList<>();
//		for (IToken t : tokens){
//			System.out.println(t);
//		}
	}

	private void erro(String desc, String s) {
		ErroSintatico e = new ErroSintatico(desc, tokenAtual);
		System.out.println(e + "---" + tokenAtual + "---" + s);
		erros.add(e);
		consumir("ERRO");
	}

	private boolean match(String str) {
		if (tokenAtual == null) return false;
		return tokenAtual.match(str);
	}

	public void analizar() {
		compilationUnit();
	}

	private void compilationUnit() {
		if (tokenAtual == null) return;
		if (match("package")) {
			consumir("compilationUnit");
			qualifiedIdentifier();
			if (match(";")) {
				consumir("compilationUnit");
			} else {
				erro("Esperado ';'","compilationUnit");
			}
		}
		while (match("import")) {
			consumir("compilationUnit");
			qualifiedIdentifier();
			if (match(";")) {
				consumir("compilationUnit");
			} else {
				erro("Esperado ';'","compilationUnit");
			}
		}
		while (!match("EOF")) {
			typeDeclaration();
		}

		if (match("EOF")) {
			consumir("compilationUnit");
			return;
		} else {
			erro("Esperado 'EOF'","compilationUnit");
		}
	}

	private void qualifiedIdentifier() {
		if (tokenAtual == null) return;
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("qualifiedIdentifier");
			while (match(".")) {
				consumir("qualifiedIdentifier");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
					consumir("qualifiedIdentifier");
				} else {
					erro("Esperado um <identificador>","qualifiedIdentifier");
				}
			}
		}
	}

	private void typeDeclaration() {
		if (tokenAtual == null) return;
		modifiers();
		classDeclaration();
	}

	private void modifiers() {
		if (tokenAtual == null) return;
		while (match("public") || match("private") || match("protected") || match("abstract") || match("static")) {
			consumir("modifier");
		}
	}

	private void classDeclaration() {
		if (tokenAtual == null) return;
		if (match("class")) {
			consumir("classDeclaration");
		} else {
			erro("Esperado 'class'","classDeclaration");
		}
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("classDeclaration");
		} else {
			erro("Esperado um identificador","classDeclaration");
		}
		if (match("extends")) {
			consumir("classDeclaration");
			qualifiedIdentifier();
		}
		classBody();
	}

	private void classBody() {
		if (tokenAtual == null) return;
		if (match("{")) {
			consumir("classBody");
		} else {
			erro("Esperado '{'","classBody");
		}
		while (!match("}")) {
			modifiers();
			memberDecl();
		}
		if (match("}")) {
			consumir("classBody");
		} else {
			erro("Esperado '{'","classBody");
		}
	}

	private void memberDecl() {
		if (tokenAtual == null) return;
		boolean constructor = false;
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // construtor
			constructor = true;
			int pos_ant = this.pos;
			consumir("memberDecl");
			if (match("(")){
				formalParameters();
				block();
			} else {
				this.pos = pos_ant - 1;
				consumir("memberDecl_backtracking");
				constructor = false;
			}
		}
		if (match("void") && !constructor) {
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
				erro("Esperado um <identificador>","memberDecl");
			}
		} else if (!constructor){
			type();
			int pos_ant = this.pos;
			if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {// nvoid
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
					if (match(";")) {
						consumir("memberDecl");
					} else {
						erro("Esperado ';'","memberDecl");
					}
				}
			} else {
				erro("Esperado um <identificador>","memberDecl");
			}
		}
	}

	private void block() {
		if (tokenAtual == null) return;
		if (match("{")) {
			consumir("block");
		} else {
			erro("Esperado '{'","block");
		}
		while (!match("}")) {
			blockStatement();
		}
		if (match("}")) {
			consumir("block");
		} else {
			erro("Esperado '}'","block");
		}
	}

	private void blockStatement() {
		if (tokenAtual == null) return;
		if (isLocalVariableDeclaratiomStatementORstatement()) {
			localVariableDeclaratiomStatement();
		} else {
			statement();
		}
	}

	private boolean isLocalVariableDeclaratiomStatementORstatement() { // conferir
		boolean value_return = false;
		if (match("boolean") || match("int") || match("char")) {
			value_return = true;
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			int pos_ant = this.pos;
			consumir("OR");
			while (match(".")) {
				consumir("OR");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
					consumir("OR");
				} else {
					erro("Esperado <identificador>","OK");
				}
			}
			if (match("[") || tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
				value_return = true;
			}
			this.pos = pos_ant - 1;
			consumir("OK");
		}
		return value_return;
	}

	private void statement() {
		if (tokenAtual == null) return;
		if (match("{")) {
			block();
//		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
//			consumir("statement");
////			if (match(":")) {
////				consumir("statement");
////				statement();
////			} else {
////				erro("Esperado ':'");
////			}
//			statement();
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
				erro("Esperado ';'","statement");
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
		if (tokenAtual == null) return;
		if (match("(")) {
			consumir("formalParameters");
		} else {
			erro("Esperado '('","formalParameters");
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
			erro("Esperado ')'","formalParameters");
		}
	}

	private void formalParameter() {
		if (tokenAtual == null) return;
		type();
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("formalParameter");
		} else {
			erro("Esperado <identificador>","formalParameter");
		}
	}

	private void parExpression() {
		if (tokenAtual == null) return;
		if (match("(")) {
			consumir("parExpression");
			expression();
		} else {
			erro("Esperado '('","parExpression");
		}
		if (match(")")) {
			consumir("parExpression");
		} else {
			erro("Esperado ')'","parExpression");
		}
	}

	private void localVariableDeclaratiomStatement() {
		if (tokenAtual == null) return;
		type();
		variableDeclarators();
		if (match(";")) {
			consumir("localVariableDeclaratiomStatement");
		} else {
			erro("Esperado ';'","localVariableDeclaratiomStatement");
		}
	}

	private void variableDeclarators() {
		if (tokenAtual == null) return;
		variableDeclarator();
		while (match(",")) {
			consumir("variableDeclarators");
			variableDeclarator();
		}
	}

	private void variableDeclarator() {
 		if (tokenAtual == null) return;
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("variableDeclarator");
			if (match("=")) {
				consumir("variableDeclarator");
				variableInitializer();
			}
		} else {
			erro("Esperado <identificador>","variableDeclarator");
		}
	}

	private void variableInitializer() {
		if (tokenAtual == null) return;
		if (match("{")) {
			arrayInitializer();
		} else {
			expression();
		}
	}

	private void arrayInitializer() {
		if (tokenAtual == null) return;
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
				erro("Esperado '}'","arrayInitializer");
			}
		} else {
			erro("Esperado '{'","arrayInitializer");
		}
	}

	private void arguments() {
		if (tokenAtual == null) return;
		if (match("(")) {
			consumir("arguments");
			if (!match(")")) {
				expression();
				while (match(",")) {
					consumir("arguments");
					expression();
				}
			}
			if (match(")")) {
				consumir("arguments");
			} else {
				erro("Esperado ')'","arguments");
			}
		} else {
			erro("Esperado '('","arguments");
		}
	}

	private void type() {
		if (tokenAtual == null) return;
		if (isBasicType()) {
			basicType();
		} else {
			referenceType();
		}
	}

	private boolean isBasicType() {
		boolean isBasicType = false;
		int pos_ant = this.pos;
		if (match("boolean") || match("char") || match("int")) {
			consumir("isBasicType");
			if (!match("[")) {
				isBasicType = true;
			}
		}
		this.pos = pos_ant - 1;
		consumir("OK");
		return isBasicType;
	}

	private void basicType() {
		if (tokenAtual == null) return;
		if (match("boolean") || match("char") || match("int")) {
			consumir("basicType");
		} else {
			erro("Esperado <basicType>","basicType");
		}
	}

	private void referenceType() {
		if (tokenAtual == null) return;
		if (match("boolean") || match("int") || match("char")) {
			basicType();
			if (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					erro("Esperado ']'","referenceType");
				}
			} else {
				erro("Esperado '['","referenceType");
			}
			while (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					erro("Esperado ']'","referenceType");
				}
			}
		} else {
			qualifiedIdentifier();
			while (match("[")) {
				consumir("referenceType");
				if (match("]")) {
					consumir("referenceType");
				} else {
					erro("Esperado ']'","referenceType");
				}
			}
		}
	}

	private void statementExpression() {
		if (tokenAtual == null) return;
		expression();
	}

	private void expression() {
		if (tokenAtual == null) return;
		assignmentExpression();
	}

	private void assignmentExpression() {
		if (tokenAtual == null) return;
		conditionalAndExpression();
		if (match("=") || match("+=")) {
			consumir("assignmentExpression");
			assignmentExpression();
		}
	}

	private void conditionalAndExpression() {
		if (tokenAtual == null) return;
		equalityExpression();
		while (match("&&")) {
			consumir("conditionalAndExpression");
			equalityExpression();
		}
	}

	private void equalityExpression() {
		if (tokenAtual == null) return;
		relationalExpression();
		while (match("==")) {
			consumir("equalityExpression");
			relationalExpression();
		}
	}

	private void relationalExpression() {
		if (tokenAtual == null) return;
		additiveExpression();
		if (match(">") || match("<=")) {
			consumir("relationalExpression");
			additiveExpression();
		} else if (match("instanceof")) {
			consumir("relationalExpression");
			referenceType();
		}
	}

	private void additiveExpression() {
		if (tokenAtual == null) return;
		multiplicativeExpression();
		while (match("+") || match("-")) {
			consumir("equalityExpression");
			relationalExpression();
		}
	}

	private void multiplicativeExpression() {
		if (tokenAtual == null) return;
		unaryExpression();
		while (match("*")) {
			consumir("multiplicativeExpression");
			unaryExpression();
		}
	}

	private void unaryExpression() {
		if (tokenAtual == null) return;
		if (match("++")) {
			consumir("unaryExpression");
			unaryExpression();
		} else if (match("-")) {
			consumir("unaryExpression");
			unaryExpression();
		} else {
			simpleUnaryExpression();
		}
	}
	
	public boolean isCast(){
		int pos_ant = this.pos;
		boolean isCast = false;
		consumir("("); //(
		if (isBasicType()){
			consumir(""); // basicType
			if (match(")")){
				isCast = true;
			}
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR){
			consumir(""); //.
			while(match(".")){
				consumir("."); //.
				consumir("<id>");	
			}
			while(match("[")){
				consumir("["); //.
				consumir("]");
			}
			if (match(")")){
				isCast = true;
			}
		} else if(match("char") || match("boolean") || match("int")){
			consumir(""); //.
			if (match("[")){
				consumir("["); //.
				consumir("]");
				while (match("[")){
					consumir("["); //.
					consumir("]");	
				}
				if (match(")")){
					isCast = true;
				}
			}			
		}		
		this.pos = pos_ant - 1;
		consumir("isCast");
		return isCast;
	}

	private void simpleUnaryExpression() {
		if (tokenAtual == null) return;
		if (match("!")) {
			consumir("simpleUnaryExpression");
			unaryExpression();
		} else if (match("(") && isCast()) {
			consumir("simpleUnaryExpression");
			if (isBasicType()) {
				basicType();
				consumir("simpleUnaryExpression");
				if (match(")")) {
					consumir("simpleUnaryExpression");
					unaryExpression();
				} else {
					erro("Esperado ')'","simpleUnaryExpression");
				}
			} else {
				referenceType();
				if (match(")")) {
					consumir("simpleUnaryExpression");
					simpleUnaryExpression();
				} else {
					erro("Esperado ')'","simpleUnaryExpression");
				}
			}
		} else {
			postfixExpression();
		}
	}

	private void postfixExpression() {
		if (tokenAtual == null) return;
		primary();
		while (match(".") || match("[")) {
			selector();
		}
		while (match("--")) {
			consumir("postifixExpression");
		}
	}

	private void selector() {
		if (tokenAtual == null) return;
		if (match(".")) {
			consumir("selector");
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
				erro("Esperado ']'","selector");
			}
		} else {
			erro("Esperado '.' ou '['","selector");
		}
	}

	private void primary() {
		if (tokenAtual == null) return;
		if (match("(")) {
			parExpression();
		} else if (match("this")) {
			consumir("primary");
			if (match("(")) {
				arguments();
			}
		} else if (match("super")) {
			consumir("primary");
			if (match("(")) {
				arguments();
			} else if (match(".")) {
				consumir("primary");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
					consumir("primary");
				} else {
					erro("Esperado <identificador>","primary");
				}
				if (match("(")) {
					arguments();
				}
			} else {
				erro("Esperado '(' ou '.'","primary");
			}
		} else if (match("new")) {
			consumir("primary");
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

	private void creator() {
		if (tokenAtual == null) return;
		if (match("boolean") || match("int") || match("char")) {
			basicType();
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
						erro("Esperado ']'","creator");
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

	private void newArrayDeclarator() {
		if (tokenAtual == null) return;
		if (match("[")) {
			consumir("newArrayDeclarator");
			expression();
		} else {
			erro("Esperado '['","newArrayDeclarator");
		}
		if (match("]")) {
			consumir("newArrayDeclarator");
		} else {
			erro("Esperado ']'","newArrayDeclarator");
		}
		while (match("[")) {
			consumir("newArrayDeclarator");
			expression();
			if (match("]")) {
				consumir("newArrayDeclarator");
			} else {
				erro("Esperado ']'","newArrayDeclarator");
			}
		}
		while (match("[")) {
			consumir("newArrayDeclarator");
			if (match("]")) {
				consumir("newArrayDeclarator");
			} else {
				erro("Esperado ']'","newArrayDeclarator");
			}
		}
	}

	private void literal() {
		if (tokenAtual == null) return;
		if (((Token)tokenAtual).cod == TOKEN_CODIGO.INT_LITERAL) {
			consumir("literal");
		} else if (((Token)tokenAtual).cod == TOKEN_CODIGO.CHAR_LITERAL) {
			consumir("literal");
		} else if (((Token)tokenAtual).cod == TOKEN_CODIGO.STRING_LITERAL) {
			consumir("literal");
		} else if (match("true")) {
			consumir("literal");
		} else if (match("false")) {
			consumir("literal");
		} else if (match("null")) {
			consumir("literal");
		} else {
			erro("Esperado <literal>","literal");
		}
	}

	public static void main(String[] args) {
		AnalizadorSintatico as = new AnalizadorSintatico("entradas/SyntaxErrors.txt");
		as.analizar();
		as.mostrarErros();
	}
	
	public String toString(){
		return tokenAtual.getLine() + "";
	}
}
