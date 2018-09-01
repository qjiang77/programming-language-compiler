package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */


import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.Parser.SyntaxException;


public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program program = program();
		matchEOF();
		return program;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token firstToken = t;
		Token progName = match(IDENTIFIER);
		Block block = block();
		return new Program(firstToken, progName, block);
	}
	
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {
			/* TODO  correct this */
			KW_input, KW_write, IDENTIFIER, KW_while, KW_if, KW_show, KW_sleep, KW_red, KW_green, KW_blue, KW_alpha
			};
	Kind[] firstType = { KW_int, KW_boolean, KW_float, KW_filename };
	Kind[] color = { KW_red, KW_green, KW_blue, KW_alpha };
	Kind[] predefinedname = { KW_Z, KW_default_height, KW_default_width};
	Kind[] functionname = { KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_green, KW_blue, KW_alpha};
	Kind[] firstPrimary = {INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN, KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_green, KW_blue, KW_alpha, IDENTIFIER, KW_Z, KW_default_height, KW_default_width, LPIXEL};
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	public Block block() throws SyntaxException {
		Token firstToken = t;
		match(LBRACE);
		ArrayList<ASTNode> decsOrStatements = new ArrayList<ASTNode>();
		while (isKind(firstDec)|isKind(firstStatement)) {
	     if (isKind(firstDec)) {
			//declaration();
			decsOrStatements.add(declaration());
		} else if (isKind(firstStatement)) {
			decsOrStatements.add(statement());
		}
			match(SEMI);
		}
		match(RBRACE);
		return new Block(firstToken, decsOrStatements);
	}
	
	
	/*
	 * Declaration ::= Type IDENTIFIER | image IDENTIFIER [ Expression , Expression ]
	 */
	public Declaration declaration() throws SyntaxException{
		Token firstToken = t;
		//Expression width = null;
		//Expression height = null;
		if(isKind(firstType)){
			Token type = consume();
			Token name = match(IDENTIFIER);
			Expression width = null;
			Expression height = null;
			return new Declaration(firstToken, type, name, width, height);
		}else if(isKind(firstDec)&&!isKind(firstType)) {
			Token type1 = match(KW_image);
			Token name = match(IDENTIFIER);
			if(isKind(LSQUARE)) {
				consume();
				Expression width = expression();
				match(COMMA);
				Expression height = expression();
				match(RSQUARE);
				return new Declaration(firstToken, type1, name, width, height);
			}else {
				Expression width = null;
				Expression height = null;
				return new Declaration(firstToken, type1, name, width, height);
			}
		}else {
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	
	public Statement statement() throws SyntaxException {
		switch (t.kind) {
			case KW_input: {
			return	statementinput();
			}
		
			case KW_write: {
			return 	statementwrite();
			}
			
			case KW_while: {
			return	statementwhile();
			}
			
			case KW_if: {
			return	statementif();
			}
			
			case KW_show: {
			return	statementshow();
			}
			
			case KW_sleep: {
			return	statementsleep();
			}
			
			default: {
				if(isKind(IDENTIFIER)|isKind(color)) {
					return statementassignment();
				}else {
					throw new SyntaxException(t,"Syntax Error");
				}
			}
		}
	}
	
	public StatementInput statementinput() throws SyntaxException {
		Token firstToken = t;
		match(KW_input);
		Token destName = match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		Expression e = expression();
		return new StatementInput(firstToken, destName, e);
	}
	
	public StatementWrite statementwrite() throws SyntaxException {
		Token firstToken = t;
		match(KW_write);
		Token sourceName = match(IDENTIFIER);
		match(KW_to);
		Token destName = match(IDENTIFIER);
		return new StatementWrite(firstToken, sourceName, destName);
	}
	
	public StatementAssign statementassignment() throws SyntaxException {
		Token firstToken = t;
		LHS lhs = lhs();
		match(OP_ASSIGN);
		Expression e = expression();
		return new StatementAssign(firstToken, lhs, e);
	}
	
	public StatementWhile statementwhile() throws SyntaxException {
		Token firstToken = t;
		match(KW_while);
		match(LPAREN);
		Expression guard = expression();
		match(RPAREN);
		Block b = block();
		return new StatementWhile(firstToken, guard, b);
	}
	
	/*
	 * StatementIf ::=  if ( Expression ) Block 
	 * */
	public StatementIf statementif() throws SyntaxException {
		Token firstToken = t;
		match(KW_if);
		match(LPAREN);
		Expression guard = expression();
		match(RPAREN);
		Block b = block();
		return new StatementIf(firstToken, guard, b);
	}
	
	public StatementShow statementshow() throws SyntaxException {
		Token firstToken = t;
		match(KW_show);
		Expression e = expression();
		return new StatementShow(firstToken, e);
	}
	
	public StatementSleep statementsleep() throws SyntaxException {
		Token firstToken = t;
		match(KW_sleep);
		Expression duration = expression();
		return new StatementSleep(firstToken, duration);
	}
	
	
	/*
    LHS ::=  IDENTIFIER | IDENTIFIER PixelSelector | Color ( IDENTIFIER PixelSelector )
    Color ::= red | green | blue | alpha
    */
	public LHS lhs() throws SyntaxException {
		Token firstToken = t;
		if(isKind(IDENTIFIER)) {
			Token name = match(IDENTIFIER);
			if(isKind(LSQUARE)) {
				PixelSelector pixelSelector = pixelselector();
				return new LHSPixel(firstToken, name, pixelSelector);
			}
			return new LHSIdent(firstToken, name);
		}else if(isKind(color)) {
			Token color = consume();
			match(LPAREN);
			Token name = match(IDENTIFIER);
			PixelSelector pixel = pixelselector();
			match(RPAREN);
			return new LHSSample(firstToken, name, pixel, color);
		}
		throw new SyntaxException(t,"Syntax Error");
	}
	
	public void color() throws SyntaxException {
		if(isKind(color)) {
			consume();
		}else {
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	public PixelSelector pixelselector() throws SyntaxException {
		Token firstToken = t;
		match(LSQUARE);
		Expression ex = expression();
		match(COMMA);
		Expression ey = expression();
		match(RSQUARE);
		return new PixelSelector(firstToken, ex, ey);
	}
	
	
	
	/**
	 * 	Expression ::=  OrExpression  ? Expression : Expression
	               |   OrExpression

	 */
	public Expression expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = orexpression();
		if(isKind(OP_QUESTION)) {
			match(OP_QUESTION);
			Expression e1 = expression();
			match(OP_COLON);
			Expression e2 = expression();
			e0 = new ExpressionConditional(firstToken, e0, e1, e2);
		}
		return e0;
	}
	
	
	/**
	 * OrExpression ::= AndExpression   (  |  AndExpression) *
	 *
	 */
	public Expression orexpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = andexpression();
		while(isKind(OP_OR)) {
			Token op = match(OP_OR);
			Expression e1 = andexpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	public Expression andexpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = eqexpression();
		while(isKind(OP_AND)) {
			Token op = match(OP_AND);
			Expression e1 = eqexpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	public Expression eqexpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = relexpression();
		while(isKind(OP_EQ)|isKind(OP_NEQ)) {
			if(isKind(OP_EQ) ) {
				Token op = match(OP_EQ);
				Expression e1 = relexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}else if(isKind(OP_NEQ)) {
				Token op = match(OP_NEQ);
				Expression e1 = relexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		}
		return e0;
	}
	
	public Expression relexpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = addexpression();
		while(isKind(OP_LT)|isKind(OP_GT)|isKind(OP_LE)|isKind(OP_GE)) {
			if(isKind(OP_LT)) {
				Token op = match(OP_LT);
				Expression e1 = addexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}else if(isKind(OP_GT)) {
				Token op = match(OP_GT);
				Expression e1 = addexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}else if(isKind(OP_LE)) {
				Token op = match(OP_LE);
				Expression e1 = addexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}else if(isKind(OP_GE)) {
				Token op = match(OP_GE);
				Expression e1 = addexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		}
		return e0;
	}
	
	public Expression addexpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = multexpression();
		while(isKind(OP_PLUS)|isKind(OP_MINUS)) {
			if(isKind(OP_PLUS)) {
				Token op = match(OP_PLUS);
				Expression e1 = multexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}else if(isKind(OP_MINUS)) {
				Token op = match(OP_MINUS);
				Expression e1 = multexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		}
		return e0;
	}
	
	public Expression multexpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = powerexpression();
		while(isKind(OP_TIMES)|isKind(OP_DIV)|isKind(OP_MOD)) {
			if(isKind(OP_TIMES)) {
				Token op = match(OP_TIMES);
				Expression e1 = powerexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}else if(isKind(OP_DIV)) {
				Token op = match(OP_DIV);
				Expression e1 = powerexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}else if(isKind(OP_MOD)) {
				Token op = match(OP_MOD);
				Expression e1 = powerexpression();
				e0 = new ExpressionBinary(firstToken, e0, op, e1);
			}
		}
		return e0;
	}
	
	public Expression powerexpression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = unaryexpression();
		if(isKind(OP_POWER)) {
			Token op = match(OP_POWER);
			Expression e1 = powerexpression();
			e0 = new ExpressionBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	public Expression unaryexpression() throws SyntaxException {
		Token firstToken = t;
		if(isKind(OP_PLUS)) {
			Token op = match(OP_PLUS);
			Expression e0 = unaryexpression();
			e0 = new ExpressionUnary(firstToken, op, e0);
			return e0;
		}else if(isKind(OP_MINUS)) {
			Token op = match(OP_MINUS);
			Expression e0 = unaryexpression();
			e0 = new ExpressionUnary(firstToken, op, e0);
			return e0;
		}else if(isKind(OP_EXCLAMATION)|isKind(firstPrimary)) {
			return unaryexpressionnotplusminus();
		}else {
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	public Expression unaryexpressionnotplusminus() throws SyntaxException {
		Token firstToken = t;
		if(isKind(OP_EXCLAMATION)) {
			Token op = match(OP_EXCLAMATION);
			Expression e0 = unaryexpression();
			e0 = new ExpressionUnary(firstToken, op, e0);
			return e0;
		}else if(isKind(firstPrimary)){
			return primary();
		}else {
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	
	/*	Primary ::= INTEGER_LITERAL | BOOLEAN_LITERAL | FLOATING_POINT_LITERAL | 
    ( Expression ) | FunctionApplication  | PixelExpression | 
     PredefinedName | PixelConstructor
     
	ExpressionPixelConstructor ::= << Expression , Expression , Expression , Expression >> ;
	ExpressionPixel ::= IDENTIFIER PixelSelector
	ExpressionSample ::= Color ( Expression )


	 */
	public Expression primary() throws SyntaxException {
		Token firstToken = t;
		switch (t.kind) {
			case INTEGER_LITERAL: {
				Token intLiteral = match(INTEGER_LITERAL);
				return new ExpressionIntegerLiteral(firstToken, intLiteral);
			}
			case BOOLEAN_LITERAL: {
				Token booleanLiteral = match(BOOLEAN_LITERAL);
				return new ExpressionBooleanLiteral(firstToken, booleanLiteral);
			}
			case FLOAT_LITERAL: {
				Token floatLiteral = match(FLOAT_LITERAL);
				return new ExpressionFloatLiteral(firstToken, floatLiteral);
			}
			case LPAREN: {
				match(LPAREN);
				Expression e = expression();
				match(RPAREN);
				return e;
			}
			case IDENTIFIER: {
				if(isKind(IDENTIFIER)) {
					Token name = match(IDENTIFIER);
					if(isKind(LSQUARE)) {
						PixelSelector pixelSelector = pixelselector();
						return new ExpressionPixel(firstToken, name, pixelSelector);
					}else
						return new ExpressionIdent(firstToken, name);
				}
			}
			
			
			case LPIXEL: {
				return pixelconstructor();
			}
			default: {
				if(isKind(functionname)) {
					return functionapplication();	
				}else if(isKind(predefinedname)) {
					return predefinedname();
					//consume();
				}else {
					throw new SyntaxException(t,"Syntax Error");
				}
			}
		}
	}
	
	public Expression pixelconstructor() throws SyntaxException {
		Token firstToken = t;
		match(LPIXEL);
		Expression alpha = expression();
		match(COMMA);
		Expression red = expression();
		match(COMMA);
		Expression green = expression();
		match(COMMA);
		Expression blue = expression();
		match(RPIXEL);
		return new ExpressionPixelConstructor(firstToken, alpha, red, green, blue);
	}
	
	
	public Expression functionapplication() throws SyntaxException {
		Token firstToken = t;
		Token function = consume();
		if(isKind(LPAREN)) {
			match(LPAREN);
			Expression e = expression();
			match(RPAREN);
			return new ExpressionFunctionAppWithExpressionArg(firstToken, function, e);
		}else if(isKind(LSQUARE)) {
			match(LSQUARE);
			Expression e0 = expression();
			match(COMMA);
			Expression e1 = expression();
			match(RSQUARE);
			return new ExpressionFunctionAppWithPixel(firstToken, function, e0, e1);
		}
		throw new SyntaxException(t,"Syntax Error");
	}
	
	public void functionname() throws SyntaxException {
		if(isKind(functionname)) {
			consume();
		}
	}
	
	public Expression predefinedname() throws SyntaxException {
		Token firstToken = t;
		if(isKind(predefinedname)) {
			Token name = consume();
			return new ExpressionPredefinedName(firstToken, name);
		}else {
			throw new SyntaxException(t,"Syntax Error");
		}
	}
	
	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		System.out.println(t.kind);
		System.out.println(kind);

		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Syntax Error:"+t); //TODO  give a better error message!
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind( EOF)) {
			throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	

}

