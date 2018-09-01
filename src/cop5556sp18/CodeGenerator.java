/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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


package cop5556sp18;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Types.Type;
import cop5556sp18.AST.ASTNode;
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
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner.Kind;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	
	Label startLabel;
	Label endLabel;
	int slot;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	
	@Override
	//done
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		slot = 0;
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, 0);
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	//done
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
			
		for(ASTNode node : block.decsOrStatements) {
			if(node instanceof Declaration)
				node.visit(this, arg);
		}
		startLabel = new Label();
		mv.visitLabel(startLabel);
		for(ASTNode node : block.decsOrStatements) {
			if(node instanceof Statement)
				node.visit(this, arg);
		}
		endLabel = new Label();
		mv.visitLabel(endLabel);
		
		for(ASTNode node : block.decsOrStatements) {
			if(node instanceof Declaration) {	
				visitLocalVars((Declaration)node, arg);
			}
		}
		
		return null;
	}
	//done
	public Object visitLocalVars(Declaration declaration, Object arg)throws Exception {
		
		if(declaration.type_ == Types.Type.INTEGER) {
			mv.visitLocalVariable(declaration.name, "I", null, startLabel, endLabel, declaration.slot);
		}else if(declaration.type_ == Types.Type.FLOAT) {
			mv.visitLocalVariable(declaration.name, "F", null, startLabel, endLabel, declaration.slot);
		}else if(declaration.type_ == Types.Type.BOOLEAN) {
			mv.visitLocalVariable(declaration.name, "Z", null, startLabel, endLabel, declaration.slot);
		}else if(declaration.type_ == Types.Type.FILE) {
			mv.visitLocalVariable(declaration.name, "Ljava/lang/String;", null, startLabel, endLabel, declaration.slot);
		}else if(declaration.type_ == Types.Type.IMAGE) {
			mv.visitLocalVariable(declaration.name, "Ljava/awt/image/BufferedImage;", null, startLabel, endLabel, declaration.slot);
		}
		return null;
	}
	//done
	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		slot++;
		declaration.slot = slot;
		System.out.println("type:"+declaration.type_);
		if(declaration.type_ == Type.IMAGE) {
			//TODO 
			if(declaration.width != null && declaration.height != null) {
				declaration.width.visit(this, arg);
				declaration.height.visit(this, arg);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp18/RuntimeImageSupport",
					"makeImage", "(II)Ljava/awt/image/BufferedImage;", false);
			}else {
				mv.visitLdcInsn(this.defaultWidth);
				mv.visitLdcInsn(this.defaultHeight);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp18/RuntimeImageSupport",
						"makeImage", "(II)Ljava/awt/image/BufferedImage;", false);
			}
			mv.visitVarInsn(ASTORE,declaration.slot);
		}
		
		return null;
	}
	
	
	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		
		Label settrue = new Label();
		Label l = new Label();
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		
		if(expressionBinary.op == Kind.OP_PLUS) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(IADD);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FADD);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}
		}else if(expressionBinary.op == Kind.OP_MINUS) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(ISUB);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FSUB);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP);
				mv.visitInsn(FSUB);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(I2F);
				mv.visitInsn(FSUB);
			}
		}else if(expressionBinary.op == Kind.OP_TIMES) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(IMUL);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FMUL);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}
		}else if(expressionBinary.op == Kind.OP_DIV) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(IDIV);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FDIV);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP);
				mv.visitInsn(FDIV);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(I2F);
				mv.visitInsn(FDIV);
			}
		}else if(expressionBinary.op == Kind.OP_POWER) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(POP);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2I);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(POP);
				mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(POP);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(POP);
				mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
		}else if(expressionBinary.op == Kind.OP_MOD) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(IREM);
			}
		}else if(expressionBinary.op == Kind.OP_AND) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(IAND);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN && expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN) {
				mv.visitInsn(IAND);
			}
		}else if(expressionBinary.op == Kind.OP_OR) {
			if(expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) {
				mv.visitInsn(IOR);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN && expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN) {
				mv.visitInsn(IOR);
			}
		}else if(expressionBinary.op == Kind.OP_EQ) {
			if((expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) ||
					(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN && expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN)) {
				mv.visitJumpInsn(IF_ICMPEQ, settrue);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, settrue);
			}
			mv.visitLdcInsn(false);
		}else if(expressionBinary.op == Kind.OP_NEQ) {
			if((expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) ||
					(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN && expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN)) {
				mv.visitJumpInsn(IF_ICMPNE, settrue);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFNE, settrue);
			}
			mv.visitLdcInsn(false);
		}else if(expressionBinary.op == Kind.OP_GE) {
			if((expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) ||
					(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN && expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN)) {
				mv.visitJumpInsn(IF_ICMPGE, settrue);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGE, settrue);
			}
			mv.visitLdcInsn(false);
		}else if(expressionBinary.op == Kind.OP_LE) {
			if((expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) ||
					(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN && expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN)) {
				mv.visitJumpInsn(IF_ICMPLE, settrue);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLE, settrue);
			}
			mv.visitLdcInsn(false);
		}else if(expressionBinary.op == Kind.OP_GT) {
			if((expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) ||
					(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN && expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN)) {
				mv.visitJumpInsn(IF_ICMPGT, settrue);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGT, settrue);
			}
			mv.visitLdcInsn(false);
		}else if(expressionBinary.op == Kind.OP_LT) {
			if((expressionBinary.leftExpression.type_ == Types.Type.INTEGER && expressionBinary.rightExpression.type_ == Types.Type.INTEGER) ||
					(expressionBinary.leftExpression.type_ == Types.Type.BOOLEAN && expressionBinary.rightExpression.type_ == Types.Type.BOOLEAN)) {
				mv.visitJumpInsn(IF_ICMPLT, settrue);
			}else if(expressionBinary.leftExpression.type_ == Types.Type.FLOAT && expressionBinary.rightExpression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLT, settrue);
			}
			mv.visitLdcInsn(false);
		}
		mv.visitJumpInsn(GOTO, l);
		mv.visitLabel(settrue);
		mv.visitLdcInsn(true);
		mv.visitLabel(l);
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionConditional.guard.visit(this, arg);
		Label iftrue = new Label();
		Label l = new Label();
		mv.visitJumpInsn(IFNE, iftrue);
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, l);
		mv.visitLabel(iftrue);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitLabel(l);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		if(expressionFunctionAppWithExpressionArg.function == Kind.KW_sin) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_cos) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_atan) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
			mv.visitInsn(D2F);
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_log) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
			mv.visitInsn(D2F);
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_abs) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
			}else if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.FLOAT) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
			}
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_width) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getWidth", "(Ljava/awt/image/BufferedImage;)I", false);
			}
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_height) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getHeight", "(Ljava/awt/image/BufferedImage;)I", false);
			}
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_float) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER) {
				mv.visitInsn(I2F);
			}
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_int) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.FLOAT) {
				mv.visitInsn(F2I);
			}
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_red) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getRed", "(I)I", false);
			}
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_green) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getGreen", "(I)I", false);
			}
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_blue) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getBlue", "(I)I", false);
			}
		}else if(expressionFunctionAppWithExpressionArg.function == Kind.KW_alpha) {
			if(expressionFunctionAppWithExpressionArg.e.type_ == Types.Type.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "getAlpha", "(I)I", false);
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		expressionFunctionAppWithPixel.e0.visit(this, arg);
		expressionFunctionAppWithPixel.e1.visit(this, arg);
		if(expressionFunctionAppWithPixel.name == Kind.KW_cart_x) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}else if(expressionFunctionAppWithPixel.name == Kind.KW_cart_y) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
			mv.visitInsn(FMUL);
			mv.visitInsn(F2I);
		}else if(expressionFunctionAppWithPixel.name == Kind.KW_polar_a) {
			mv.visitInsn(SWAP);
			mv.visitInsn(I2D);
			mv.visitVarInsn(DSTORE, 0);
			mv.visitInsn(I2D);
			mv.visitVarInsn(DLOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
			mv.visitInsn(D2F);
		}else if(expressionFunctionAppWithPixel.name == Kind.KW_polar_r) {
			mv.visitInsn(I2D);
			mv.visitVarInsn(DSTORE, 0);
			mv.visitInsn(I2D);
			mv.visitVarInsn(DLOAD, 0);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
			mv.visitInsn(D2F);
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(expressionIdent.type_ == Types.Type.INTEGER) {
			mv.visitVarInsn(ILOAD, expressionIdent.declaration.slot);
		}else if(expressionIdent.type_ == Types.Type.FLOAT) {
			mv.visitVarInsn(FLOAD, expressionIdent.declaration.slot);
		}else if(expressionIdent.type_ == Types.Type.BOOLEAN) {
			mv.visitVarInsn(ILOAD, expressionIdent.declaration.slot);
		}else if(expressionIdent.type_ == Types.Type.FILE) {
			mv.visitVarInsn(ALOAD, expressionIdent.declaration.slot);
		}else if(expressionIdent.type_ == Types.Type.IMAGE) {
			mv.visitVarInsn(ALOAD, expressionIdent.declaration.slot);
		}

		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, expressionPixel.declaration.slot);
		expressionPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "getPixel", RuntimeImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimePixelOps", "makePixel", RuntimePixelOps.makePixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(expressionPredefinedName.name == Kind.KW_Z) {
			mv.visitLdcInsn(255);
		}else if(expressionPredefinedName.name == Kind.KW_default_width) {
			mv.visitLdcInsn(this.defaultWidth);
		}else if(expressionPredefinedName.name == Kind.KW_default_height) {
			mv.visitLdcInsn(this.defaultHeight);
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		Label settrue = new Label();
		Label l = new Label();
		if(expressionUnary.op == Kind.OP_MINUS) {
			if(expressionUnary.expression.type_ == Types.Type.INTEGER 
					|| expressionUnary.expression.type_ == Types.Type.BOOLEAN) {
				mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE, 0);
				mv.visitJumpInsn(IFGT, settrue);
			}else if(expressionUnary.expression.type_ == Types.Type.FLOAT) {
				mv.visitInsn(DUP);
				mv.visitVarInsn(FSTORE, 0);
				mv.visitLdcInsn((float) 0);
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGT, settrue);

			}
		}
		else if(expressionUnary.op == Kind.OP_EXCLAMATION) {
			if(expressionUnary.expression.type_ == Types.Type.INTEGER) {
				mv.visitLdcInsn(-1);
				mv.visitInsn(IXOR);

			}else if(expressionUnary.expression.type_ == Types.Type.BOOLEAN) {
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(IF_ICMPEQ, settrue);
				mv.visitLdcInsn(true);
			}
		}
		if((expressionUnary.expression.type_ == Types.Type.INTEGER) && 
				(expressionUnary.op == Kind.OP_MINUS))
		{
			mv.visitVarInsn(ILOAD, 0);
		}
		if((expressionUnary.expression.type_ == Types.Type.FLOAT) && 
				(expressionUnary.op == Kind.OP_MINUS))
		{
			mv.visitVarInsn(FLOAD, 0);
		}
		mv.visitJumpInsn(GOTO, l);
		mv.visitLabel(settrue);
		if((expressionUnary.expression.type_ == Types.Type.INTEGER) && 
				(expressionUnary.op == Kind.OP_MINUS))
		{
			mv.visitVarInsn(ILOAD, 0);
			mv.visitInsn(INEG);
		}
		if((expressionUnary.expression.type_ == Types.Type.FLOAT) && 
				(expressionUnary.op == Kind.OP_MINUS))
		{
			mv.visitVarInsn(FLOAD, 0);
			mv.visitInsn(FNEG);
		}
		if((expressionUnary.expression.type_ == Types.Type.BOOLEAN) && 
				(expressionUnary.op == Kind.OP_EXCLAMATION))
		{
			mv.visitLdcInsn(false);
		}
		mv.visitLabel(l);
		return null;

	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		//INTEGER, BOOLEAN, IMAGE, FLOAT, FILE
		if(lhsIdent.dec ==Type.INTEGER||lhsIdent.dec ==Type.BOOLEAN )
			mv.visitVarInsn(ISTORE, lhsIdent.declaration.slot);
		if(lhsIdent.dec ==Type.FLOAT)
			mv.visitVarInsn(FSTORE, lhsIdent.declaration.slot);
		if(lhsIdent.dec ==Type.FILE)
			mv.visitVarInsn(ASTORE, lhsIdent.declaration.slot);
		if(lhsIdent.dec == Type.IMAGE) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", 
					"deepCopy", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			mv.visitVarInsn(ASTORE, lhsIdent.declaration.slot);
		}
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, lhsPixel.declaration.slot);
		lhsPixel.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "setPixel", RuntimeImageSupport.setPixelSig, false);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, lhsSample.declaration.slot);
		
		lhsSample.pixelSelector.visit(this, arg);
		if(lhsSample.color == Kind.KW_green)
		{
			mv.visitLdcInsn(RuntimePixelOps.GREEN);
		}
		else if(lhsSample.color == Kind.KW_red)
		{
			mv.visitLdcInsn(RuntimePixelOps.RED);
		}
		else if(lhsSample.color == Kind.KW_blue)
		{
			mv.visitLdcInsn(RuntimePixelOps.BLUE);
		}
		else if(lhsSample.color == Kind.KW_alpha)
		{
			mv.visitLdcInsn(RuntimePixelOps.ALPHA);
		}
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		pixelSelector.ex.visit(this, arg);
		if(pixelSelector.ex.type_ == Types.Type.FLOAT)
			mv.visitInsn(F2I);
		pixelSelector.ey.visit(this, arg);
		if(pixelSelector.ey.type_ == Types.Type.FLOAT)
			mv.visitInsn(F2I);
		
		return null;
	}

	
	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		statementIf.guard.visit(this, arg);
		Label start = new Label();
		mv.visitJumpInsn(IFEQ, start);
		statementIf.b.visit(this, arg);
		mv.visitLabel(start);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, 0);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);
		if(statementInput.declaration.type_ == Types.Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt","(Ljava/lang/String;)I",false);
			mv.visitVarInsn(ISTORE, statementInput.declaration.slot);			
		}
		else if(statementInput.declaration.type_ == Types.Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean","(Ljava/lang/String;)Z",false);
			mv.visitVarInsn(ISTORE, statementInput.declaration.slot);			
		}
		else if(statementInput.declaration.type_ == Types.Type.FLOAT) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat","(Ljava/lang/String;)F",false);
			mv.visitVarInsn(FSTORE, statementInput.declaration.slot);			
		}
		else if(statementInput.declaration.type_ == Types.Type.FILE) {
			mv.visitVarInsn(ASTORE, statementInput.declaration.slot);			
		}
		else if(statementInput.declaration.type_ == Types.Type.IMAGE) {
			if(statementInput.declaration.width != null && statementInput.declaration.height != null) {
				mv.visitTypeInsn(NEW, "java/lang/Integer");
				mv.visitInsn(DUP);
				statementInput.declaration.width.visit(this, arg);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V", false);
				mv.visitTypeInsn(NEW, "java/lang/Integer");
				mv.visitInsn(DUP);
				statementInput.declaration.height.visit(this, arg);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V", false);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", 
						"readImage", RuntimeImageSupport.readImageSig, false);
				mv.visitVarInsn(ASTORE, statementInput.declaration.slot);
			}else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", 
						"readImage", RuntimeImageSupport.readImageSig, false);
				mv.visitVarInsn(ASTORE, statementInput.declaration.slot);
			}
		}
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);
				//throw new UnsupportedOperationException();
			}
			// break; commented out because currently unreachable. You will need
			// it.
				break;
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
				//throw new UnsupportedOperationException();
			}
			// break; commented out because currently unreachable. You will need
			// it.
				break;
			case FILE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "((Ljava/lang/String)V", false);
				//throw new UnsupportedOperationException();
			}
			// break; commented out because currently unreachable. You will need
			// it.
				break;
			case IMAGE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", 
						"makeFrame", "(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
			}
				break;
		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		statementSleep.duration.visit(this, arg);
		Type type = statementSleep.duration.getType();
		CodeGenUtils.genLogTOS(DEVEL, mv, type);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", 
				"sleep", "(J)V", false);
		//throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label start = new Label();
		mv.visitJumpInsn(GOTO, start);
		Label end = new Label();
		mv.visitLabel(end);
		statementWhile.b.visit(this, arg);
		mv.visitLabel(start);
		statementWhile.guard.visit(this, arg);
		mv.visitJumpInsn(IFNE, end);
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, statementWrite.sourceDeclaration.slot);
		mv.visitVarInsn(ALOAD, statementWrite.destDeclaration.slot);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp18/RuntimeImageSupport", "write", RuntimeImageSupport.writeSig, false);
		return null;
	}

}
