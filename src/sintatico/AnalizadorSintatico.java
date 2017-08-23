package sintatico;

import java.util.ArrayList;
import lexico.Constantes.TOKEN_CODIGO;
import semantica.ConstantesSemantica.TIPO_DADOS;
import semantica.Escopo;
import semantica.PilhaEscopo;
import semantica.TabelaSimbolosSemantica;
import lexico.IToken;
import lexico.Lexico;
import lexico.Token;

public class AnalizadorSintatico {
	private IToken tokenAtual;
	private ArrayList<IToken> tokens;
	private int pos;
	private ArrayList<ErroSintatico> erros;
	private int indexEscopo = -1;
	private TabelaSimbolosSemantica tabela = new TabelaSimbolosSemantica();
	
	//-----------------------

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

	//----------------------------------
	
	private String getObjectName(){
		String name = tokenAtual.getLexema();
		int pos_ant = this.pos;
		consumir("");
		while(match(".")){
			consumir("");
			name = tokenAtual.getLexema();
			consumir("");
		}
		this.pos = pos_ant -1;
		consumir("");		
		return name;
	}
	
	private boolean isArray(){
		String name = tokenAtual.getLexema();
		boolean isArray = false;
		int pos_ant = this.pos;
		consumir("");
		while(match(".")){
			consumir("");
			name = tokenAtual.getLexema();
			consumir("");
		}
		if (match("[")){
			consumir("");
			if (match("]")){
				isArray = true;
			}
		}
		this.pos = pos_ant -1;
		consumir("");		
		return isArray;
	}
	
	public void createEscopo(String name){
		Escopo esc = new Escopo();
		esc.name = name;
		this.indexEscopo = PilhaEscopo.insertEscopo(esc);
	}
	
	public void insertTabela(String lexema, String type, boolean isArray){
		this.tabela.insert(lexema, PilhaEscopo.getLastEscopo(), getType(type, isArray));
	}
	
	public void removeEscopo(){
		PilhaEscopo.removeLastEscopo();
	}
	
	//--------------------------------------------

	private void compilationUnit() {
		createEscopo("GLOBAL");
//		insertTabela("String", "String"); @obede: tabela de import
		
		
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
//			String name = getObjectName();
//			insertTabela(name, name); @obede: tabela de import
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
			removeEscopo();
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
			createEscopo(tokenAtual.getLexema());
			consumir("classDeclaration");
		} else {
			erro("Esperado um identificador","classDeclaration");
		}
		if (match("extends")) {
//			analise semantica? @obede
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
			removeEscopo();
		} else {
			erro("Esperado '{'","classBody");
		}
	}

	private void memberDecl() { //analise semantica? @obede
		if (tokenAtual == null) return;
		boolean constructor = false;
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) { // construtor
			String name = tokenAtual.getLexema();
			constructor = true;
			int pos_ant = this.pos;
			consumir("memberDecl");
			if (match("(")){
				if (!name.equals(PilhaEscopo.getLastEscopo().name)){
					System.out.println("Construtor não válido");
				}
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
			String type = getObjectName();
			boolean isArray = isArray();
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
					variableDeclarators(type, isArray);
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
		createEscopo("block");
		while (!match("}")) {
			blockStatement();
		}
		if (match("}")) {
			removeEscopo();
			consumir("block");
		} else {
			erro("Esperado '}'","block");
		}
	}

	private void blockStatement() {
		if (tokenAtual == null) return;
		if (isLocalVariableDeclaratiomStatement()) {
			localVariableDeclaratiomStatement();
		} else {
			statement();
		}
	}

	private boolean isLocalVariableDeclaratiomStatement() {
		boolean isLocalVD = false;
		int pos_ant = this.pos;
		if (isBasicType()){
			isLocalVD = true;
		} else if(match("char") || match("boolean") || match("int")){
			consumir(""); //.
			if (match("[")){
				consumir("["); //.
				if (match("]")){
					isLocalVD = true;
				}
			}			
		} else if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			consumir("OR");
			while (match(".")) {
				consumir("OR");
				if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
					consumir("OR");
				}
			}
			if (match("[")) {
				consumir("");
				if (match("]")){
					isLocalVD = true;
				}
			}
			
		}
		this.pos = pos_ant - 1;
		consumir("OK");
		return isLocalVD;

	}

	private void statement() { //analise semantica @obede
		if (tokenAtual == null) return;
		if (match("{")) {
			block();
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
	
	public TIPO_DADOS getType(String type, boolean isArray){
		if (isArray){
			if (type.equals("int")){
				return TIPO_DADOS.INT_ARRAY;
			} else if (type.equals("char")){
				return TIPO_DADOS.CHAR_ARRAY;
			} else if (type.equals("boolean")){
				return TIPO_DADOS.BOOLEAN_ARRAY;
			} else if (type.equals("String")){
				return TIPO_DADOS.STRING_ARRAY;
			} else {
				return TIPO_DADOS.OBJECT_ARRAY;
			}
		} else {
			if (type.equals("int")){
				return TIPO_DADOS.INT;
			} else if (type.equals("char")){
				return TIPO_DADOS.CHAR;
			} else if (type.equals("boolean")){
				return TIPO_DADOS.BOOLEAN;
			} else if (type.equals("String")){
				return TIPO_DADOS.STRING;
			} else {
				return TIPO_DADOS.OBJECT;
			}
		}
	}

	private void formalParameter() {
		if (tokenAtual == null) return;
		String type = getObjectName();
		boolean isArray = isArray();
		type();
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			insertTabela(tokenAtual.getLexema(), type, isArray);
			consumir("formalParameter");
		} else {
			erro("Esperado <identificador>","formalParameter");
		}
	}

	private void parExpression() { //analiss semantica @obede
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
		String type = getObjectName();
		boolean isArray = isArray();
		type();
		variableDeclarators(type, isArray);
		if (match(";")) {
			consumir("localVariableDeclaratiomStatement");
		} else {
			erro("Esperado ';'","localVariableDeclaratiomStatement");
		}
	}

	private void variableDeclarators(String type,boolean isArray) {
		if (tokenAtual == null) return;
		variableDeclarator(type, isArray);
		while (match(",")) {
			consumir("variableDeclarators");
			variableDeclarator(type, isArray);
		}
	}

	private void variableDeclarator(String type, boolean isArray) {
 		if (tokenAtual == null) return;
		if (tokenAtual.tokenTipo() == TOKEN_CODIGO.IDENTIFICADOR) {
			insertTabela(tokenAtual.getLexema(), type, isArray);
			consumir("variableDeclarator");
			if (match("=")) {
				consumir("variableDeclarator");
				variableInitializer();
				// analise semantica - inserir valor na tablea de valores @obede
			}
		} else {
			erro("Esperado <identificador>","variableDeclarator");
		}
	}

	private void variableInitializer() {
		// analise semantica - inserir valor na tablea de valores @obede
		if (tokenAtual == null) return;
		if (match("{")) {
			arrayInitializer();
		} else {
			expression();
		}
	}

	private void arrayInitializer() { //@obede : anaslise semantica
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

	private void statementExpression() { //@obede
		if (tokenAtual == null) return;
		expression();
	}

	private void expression() { //@obede
		if (tokenAtual == null) return;
		assignmentExpression();
	}

	private void assignmentExpression() { //@obede
		if (tokenAtual == null) return;
		conditionalAndExpression();
		if (match("=") || match("+=")) {
			consumir("assignmentExpression");
			assignmentExpression();
		}
	}

	private void conditionalAndExpression() { //@obede
		if (tokenAtual == null) return;
		equalityExpression();
		while (match("&&")) {
			consumir("conditionalAndExpression");
			equalityExpression();
		}
	}

	private void equalityExpression() { //@obede
		if (tokenAtual == null) return;
		relationalExpression();
		while (match("==")) {
			consumir("equalityExpression");
			relationalExpression();
		}
	}

	private void relationalExpression() { //@obede
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

	private void additiveExpression() { //@obede
		if (tokenAtual == null) return;
		multiplicativeExpression();
		while (match("+") || match("-")) {
			consumir("equalityExpression");
			relationalExpression();
		}
	}

	private void multiplicativeExpression() { //@obede
		if (tokenAtual == null) return;
		unaryExpression();
		while (match("*")) {
			consumir("multiplicativeExpression");
			unaryExpression();
		}
	}

	private void unaryExpression() { //@obede
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

	private void simpleUnaryExpression() { //@obede
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

	private void postfixExpression() { //@obede
		if (tokenAtual == null) return;
		primary();
		while (match(".") || match("[")) {
			selector();
		}
		while (match("--")) {
			consumir("postifixExpression");
		}
	}

	private void selector() { //voltar
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

	private void primary() { //voltar
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

	private void creator() { //voltar
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

	private void newArrayDeclarator() { //voltar
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

	private TIPO_DADOS literal() {
		if (tokenAtual == null) return null;
		if (((Token)tokenAtual).cod == TOKEN_CODIGO.INT_LITERAL) {
			consumir("literal");
			return TIPO_DADOS.INT;
		} else if (((Token)tokenAtual).cod == TOKEN_CODIGO.CHAR_LITERAL) {
			consumir("literal");
			return TIPO_DADOS.CHAR;
		} else if (((Token)tokenAtual).cod == TOKEN_CODIGO.STRING_LITERAL) {
			consumir("literal");
			return TIPO_DADOS.STRING;
		} else if (match("true")) {
			consumir("literal");
			return TIPO_DADOS.BOOLEAN;
		} else if (match("false")) {
			consumir("literal");
			return TIPO_DADOS.BOOLEAN;
		} else if (match("null")) {
			consumir("literal");
			return TIPO_DADOS.NULL;
		} else {
			erro("Esperado <literal>","literal");
			return null;
		}		
	}

	
	//------------------------------------------------------
	
	
	public static void main(String[] args) {
		AnalizadorSintatico as = new AnalizadorSintatico("entradas/SyntaxErrors.txt");
		//AnalizadorSintatico as = new AnalizadorSintatico("entradas/entrada_certa.txt");
		as.analizar();
		as.mostrarErros();
	}
	
	public void analizar() {
		compilationUnit();
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
	
	public String toString(){
		return tokenAtual.getLine() + "";
	}
}
