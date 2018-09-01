package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;


public class TypeCheckerTest{

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}



	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { show true+4; }"; //error, incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testvalidExpressionBinary1() throws Exception {
		String input = "prog{ int var1; "
				+ "boolean var2;"
				+ "var1 := 1 + 2; "
				+ "var1 := 1 - 2; "
				+ "var1 := 1 * 2; "
				+ "var1 := 1 / 2; "
				+ "var1 := 1 ** 2; "
				+ "var1 := 1 % 2; "
				+ "var1 := 1 & 2; "
				+ "var1 := 1 | 2; "
				+ "var2 := 1 == 2; "
				+ "var2 := 1 != 2; "
				+ "var2 := 1 > 2; "
				+ "var2 := 1 >= 2; "
				+ "var2 := 1 < 2; "
				+ "var2 := 1 <= 2;"
				+"}";
		typeCheck(input);
	}
	
	@Test
	public void testvalidExpressionBinary2() throws Exception {
		String input = "prog{ float var1; boolean var2;"
				+ "var1 := 1.0 + 2.0; var1 := 1.0 - 2.0; var1 := 1.0 * 2.0; "
				+ "var1 := 1.0 / 2.0; var1 := 1.0 ** 2.0; var2 := 1.0 == 2.0; "
				+ "var2 := 1.0 != 2.0; var2 := 1.0 > 2.0; var2 := 1.0 >= 2.0; "
				+ "var2 := 1.0 < 2.0; var2 := 1.0 <= 2.0;}";
		typeCheck(input);
	}
	
	@Test
	public void testvalidStatementInput1() throws Exception {
		String input = "prog{input var from @1; int var;}";
		typeCheck(input);
	}
	
	@Test
	public void testvalidStatementInput2() throws Exception {
		String input = "prog{if(true){int var;}; if(true){input var from @1;};}";
		typeCheck(input);
	}
	
	@Test
	public void testvalidStatementInput3() throws Exception {
		String input = "prog{if(true){int var;}; input var from @1;}";
		typeCheck(input);
	}
	
	@Test
	public void testvalidExpressionConditional1() throws Exception {
		String input = "prog{boolean cond;int var1; var1 := cond ? var1+ 1 : var1;"
				+ "float var2; var2 := cond ? var2 + 1.0 : var2;}";
		typeCheck(input);
	}
	
	
}