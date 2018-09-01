package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types;
import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.SymbolTable;

public class TypeChecker implements ASTVisitor {
	SymbolTable symboltable;

	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
	    symboltable = new SymbolTable();
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symboltable.enterScope();
		for(int i = 0; i < block.decsOrStatements.size(); i++) {
			block.decsOrStatements.get(i).visit(this, arg);
		}
		symboltable.leaveScope();
		return null;
		//throw new SemanticException();
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(declaration.width != null)
		declaration.width.visit(this, arg);
		if(declaration.height != null)
		declaration.height.visit(this, arg);
		if(!symboltable.judge(declaration.name)) {
			if(declaration.width == null || ((declaration.width.type_ == Types.Type.INTEGER) 
					&& (Types.getType(declaration.type) == Types.Type.IMAGE))) {
				if(declaration.height == null || ((declaration.height.type_ == Types.Type.INTEGER) 
					&& (Types.getType(declaration.type) == Types.Type.IMAGE))) {
					if((declaration.width == null) == (declaration.height == null)) {
						symboltable.add(declaration.name,declaration);
						declaration.type_= Types.getType(declaration.type);
						//System.out.println(declaration.name+declaration);
					}else throw new SemanticException(null,"Wrong Declaration!");
				}else throw new SemanticException(null,"Wrong Declaration!");
			}else throw new SemanticException(null,"Wrong Declaration!");
		}else throw new SemanticException(null,"Wrong Declaration!");
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementWrite.sourceDeclaration = symboltable.lookupDec(statementWrite.sourceName, symboltable.current_scope);
		statementWrite.destDeclaration = symboltable.lookupDec(statementWrite.destName, symboltable.current_scope);
		statementWrite.sourceDec = symboltable.lookup(statementWrite.sourceName,symboltable.current_scope);
		if(statementWrite.sourceDec != null) {
			statementWrite.destDec = symboltable.lookup(statementWrite.destName,symboltable.current_scope);
			if(statementWrite.destDec != null) {
				if(statementWrite.sourceDec == Types.Type.IMAGE) {
					if(statementWrite.destDec == Types.Type.FILE) {
					}else throw new SemanticException(null, "Wrong StatementWrite!");
				}else throw new SemanticException(null, "Wrong StatementWrite!");
			}else throw new SemanticException(null, "Wrong StatementWrite!");
		}else throw new SemanticException(null, "Wrong StatementWrite!");
		
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementInput.e.visit(this, arg);
		statementInput.dec = symboltable.lookup(statementInput.destName,symboltable.current_scope);
		statementInput.declaration = symboltable.lookupDec(statementInput.destName,symboltable.current_scope);
		if(statementInput.dec == Type.NONE) 
			throw new SemanticException(null, "Wrong statementInput!");
		if(statementInput.e.type_ != Types.Type.INTEGER) 
			throw new SemanticException(null, "Wrong statementInput!");
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
		if(pixelSelector.ex.type_ != pixelSelector.ey.type_) 
			throw new SemanticException(null, "Wrong pixelSelector!");
		if(pixelSelector.ex.type_ != Types.Type.INTEGER 
					&& pixelSelector.ex.type_ != Types.Type.FLOAT) 
			throw new SemanticException(null, "Wrong pixelSelector!");
		
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionConditional.guard.visit(this, arg);
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		if(expressionConditional.guard.type_ != Types.Type.BOOLEAN) 
			throw new SemanticException(null, "Wrong expressionConditional!");
		if(expressionConditional.trueExpression.type_ != expressionConditional.falseExpression.type_) 
			throw new SemanticException(null, "Wrong expressionConditional!");
		expressionConditional.type_ = expressionConditional.trueExpression.type_;
		
		return null;
	}
	//
	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		//expressionBinary.type_ = inferredType;
		
		if(expressionBinary.op == Kind.OP_PLUS
				||expressionBinary.op == Kind.OP_MINUS
				||expressionBinary.op ==Kind.OP_TIMES
				||expressionBinary.op ==Kind.OP_DIV
				||expressionBinary.op == Kind.OP_MOD
				||expressionBinary.op ==Kind.OP_POWER
				||expressionBinary.op ==Kind.OP_AND
				||expressionBinary.op ==Kind.OP_OR) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER 
					&& expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				expressionBinary.type_ = Types.Type.INTEGER;
			}
		}
		if(expressionBinary.op == Kind.OP_PLUS
				||expressionBinary.op == Kind.OP_MINUS
				||expressionBinary.op ==Kind.OP_DIV
				||expressionBinary.op ==Kind.OP_POWER
				||expressionBinary.op ==Kind.OP_TIMES) {
			if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT 
					|| expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				expressionBinary.type_ = Types.Type.FLOAT;
			}
		}
		if(expressionBinary.op == Kind.OP_AND
				||expressionBinary.op ==Kind.OP_OR) {
			if(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN
					&& expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN) {
				expressionBinary.type_ = Types.Type.BOOLEAN;
			}else if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER 
					&& expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				expressionBinary.type_ = Types.Type.INTEGER;
			}
		}
		if(expressionBinary.op == Kind.OP_EQ
				||expressionBinary.op ==Kind.OP_NEQ
				||expressionBinary.op ==Kind.OP_GT
				||expressionBinary.op ==Kind.OP_GE
				||expressionBinary.op ==Kind.OP_LT
				||expressionBinary.op ==Kind.OP_LE) {
			if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT 
					&& expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				expressionBinary.type_ = Types.Type.BOOLEAN;
			}else if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER
					&& expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				expressionBinary.type_ = Types.Type.BOOLEAN;
			}else if(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN 
					&& expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN) {
				expressionBinary.type_ = Types.Type.BOOLEAN;
			}
		}
		//throw new SemanticException();
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		expressionUnary.type_ = expressionUnary.expression.type_;
		//throw new SemanticException();
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionIntegerLiteral.type_ = Types.Type.INTEGER;
		//throw new SemanticException();
		return null;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBooleanLiteral.type_ = Types.Type.BOOLEAN;
		//throw new SemanticException();
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPredefinedName.type_ = Types.Type.INTEGER;
		//throw new SemanticException();
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFloatLiteral.type_ = Types.Type.FLOAT;
		//throw new SemanticException();
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		//expressionFunctionAppWithExpressionArg.type_ = inferredtype;
		if(expressionFunctionAppWithExpressionArg.function == Kind.KW_abs
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_red
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_green
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_blue
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_alpha) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER)
				expressionFunctionAppWithExpressionArg.type_ = Types.Type.INTEGER;
		}
		if(expressionFunctionAppWithExpressionArg.function == Kind.KW_abs
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_sin
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_cos
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_atan
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_log) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.FLOAT)
				expressionFunctionAppWithExpressionArg.type_ = Types.Type.FLOAT;
		}
		if(expressionFunctionAppWithExpressionArg.function == Kind.KW_width
				||expressionFunctionAppWithExpressionArg.function == Kind.KW_height) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.IMAGE)
				expressionFunctionAppWithExpressionArg.type_ = Types.Type.INTEGER;
		}
		if(expressionFunctionAppWithExpressionArg.function == Kind.KW_float) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER)
				expressionFunctionAppWithExpressionArg.type_ = Types.Type.FLOAT;
			else if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.FLOAT)
				expressionFunctionAppWithExpressionArg.type_ = Types.Type.FLOAT;
		}
		if(expressionFunctionAppWithExpressionArg.function == Kind.KW_int) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER)
				expressionFunctionAppWithExpressionArg.type_ = Types.Type.INTEGER;
			else if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.FLOAT)
				expressionFunctionAppWithExpressionArg.type_ = Types.Type.INTEGER;
		}
		//throw new SemanticException();
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionFunctionAppWithPixel.e0.visit(this, arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		
		if(expressionFunctionAppWithPixel.name == Kind.KW_cart_x || expressionFunctionAppWithPixel.name == Kind.KW_cart_y) {
			if(expressionFunctionAppWithPixel.e0.type_ == Types.Type.FLOAT) {
				if(expressionFunctionAppWithPixel.e1.type_ == Types.Type.FLOAT) {
					expressionFunctionAppWithPixel.type_ = Types.Type.INTEGER;
				}else throw new SemanticException(null, "Wrong expressionFunctionAppWithPixel!");
			}else throw new SemanticException(null, "Wrong expressionFunctionAppWithPixel!");
		}else if(expressionFunctionAppWithPixel.name == Kind.KW_polar_a || expressionFunctionAppWithPixel.name == Kind.KW_polar_r) {
			if(expressionFunctionAppWithPixel.e0.type_ == Types.Type.INTEGER) {
				if(expressionFunctionAppWithPixel.e1.type_ == Types.Type.INTEGER) {
					expressionFunctionAppWithPixel.type_ = Types.Type.FLOAT;
				}else throw new SemanticException(null, "Wrong expressionFunctionAppWithPixel!");
			}else throw new SemanticException(null, "Wrong expressionFunctionAppWithPixel!");
		}else throw new SemanticException(null, "Wrong expressionFunctionAppWithPixel!");
		
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		if(expressionPixelConstructor.alpha.type_ != Types.Type.INTEGER)
			throw new SemanticException(null, "Wrong expressionPixelConstructor!");
		if(expressionPixelConstructor.red.type_ != Types.Type.INTEGER)
			throw new SemanticException(null, "Wrong expressionPixelConstructor!");
		if(expressionPixelConstructor.green.type_ != Types.Type.INTEGER)
			throw new SemanticException(null, "Wrong expressionPixelConstructor!");
		if(expressionPixelConstructor.blue.type_ != Types.Type.INTEGER)
			throw new SemanticException(null, "Wrong expressionPixelConstructor!");
		expressionPixelConstructor.type_ = Types.Type.INTEGER;
		//throw new SemanticException();
		return null;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementAssign.lhs.visit(this, arg);
		statementAssign.e.visit(this, arg);
		if(statementAssign.lhs.type_ != statementAssign.e.type_) 
			throw new SemanticException(null, "Wrong StatementAssign!");
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementShow.e.visit(this, arg);
		if(statementShow.e.type_ != Types.Type.INTEGER 
				&& statementShow.e.type_ != Types.Type.BOOLEAN 
				&& statementShow.e.type_ != Types.Type.FLOAT 
				&& statementShow.e.type_ != Types.Type.IMAGE) 
			throw new SemanticException(null, "Wrong statementShow!");
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionPixel.pixelSelector.visit(this, arg);
		expressionPixel.dec = symboltable.lookup(expressionPixel.name,symboltable.current_scope);
		expressionPixel.declaration = symboltable.lookupDec(expressionPixel.name,symboltable.current_scope);

		if(expressionPixel.dec == null)
			throw new SemanticException(null, "Wrong expressionPixel!");
		if(expressionPixel.dec != Types.Type.IMAGE) 
			throw new SemanticException(null, "Wrong expressionPixel!");
		expressionPixel.type_ = Types.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionIdent.dec = symboltable.lookup(expressionIdent.name,symboltable.current_scope);
		expressionIdent.declaration = symboltable.lookupDec(expressionIdent.name,symboltable.current_scope);

		//System.out.println(expressionIdent.name);
		//System.out.println(expressionIdent.dec);
		
		if(expressionIdent.dec == null) 
			throw new SemanticException(null, "Wrong expressionIdent!");
		expressionIdent.type_ = expressionIdent.dec;
		
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsSample.pixelSelector.visit(this, arg);
		lhsSample.dec = symboltable.lookup(lhsSample.name,symboltable.current_scope);
		lhsSample.declaration = symboltable.lookupDec(lhsSample.name,symboltable.current_scope);
		if(lhsSample.dec == null) 
			throw new SemanticException(null, "Wrong lhsSample!");
		if(lhsSample.dec != Types.Type.IMAGE) 
			throw new SemanticException(null, "Wrong lhsSample!");
		lhsSample.type_ = Types.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsPixel.pixelSelector.visit(this, arg);
		lhsPixel.dec = symboltable.lookup(lhsPixel.name,symboltable.current_scope);
		lhsPixel.declaration = symboltable.lookupDec(lhsPixel.name,symboltable.current_scope);
		if(lhsPixel.dec == null) 
			throw new SemanticException(null, "Wrong lhsPixel!");
		if(lhsPixel.dec != Types.Type.IMAGE) 
			throw new SemanticException(null, "Wrong lhsPixel!");
		lhsPixel.type_ = Types.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsIdent.dec = symboltable.lookup(lhsIdent.name,symboltable.current_scope);
		lhsIdent.declaration = symboltable.lookupDec(lhsIdent.name,symboltable.current_scope);
		if(lhsIdent.dec == null) 
			throw new SemanticException(null, "Wrong lhsIdent!");
		lhsIdent.type_ = lhsIdent.dec;
		
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementIf.guard.visit(this, arg);
		statementIf.b.visit(this, arg);
		if(statementIf.guard.type_ != Types.Type.BOOLEAN) 
			throw new SemanticException(null, "Wrong statementIf!");
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementWhile.guard.visit(this, arg);
		statementWhile.b.visit(this, arg);
		if(statementWhile.guard.type_ != Types.Type.BOOLEAN)
			throw new SemanticException(null, "Wrong statementWhile!");
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementSleep.duration.visit(this, arg);
		if(statementSleep.duration.type_ != Types.Type.INTEGER) 
			throw new SemanticException(null, "Wrong statementSleep!");
		return null;
	}


}
