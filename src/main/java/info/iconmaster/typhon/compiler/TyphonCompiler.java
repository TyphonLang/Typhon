package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.antlr.TyphonBaseVisitor;
import info.iconmaster.typhon.antlr.TyphonParser.DefStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.LvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.tnil.Instruction;
import info.iconmaster.typhon.types.TypeRef;

/**
 * The Typhon compiler.
 * 
 * @author iconmaster
 *
 */
public class TyphonCompiler {
	private TyphonCompiler() {}
	
	/**
	 * Compiles all the members of a package.
	 * 
	 * @param p
	 */
	public void compile(Package p) {
		if (!p.needsCompiled()) {
			return;
		}
		p.needsCompiled(false);
		
		p.getFunctions().stream().forEach((f)->compile(f));
		p.getFields().stream().forEach((f)->compile(f));
		
		p.getSubpackges().stream().forEach((f)->compile(f));
	}
	
	/**
	 * Compiles a function. Updates the contents of the argument.
	 * 
	 * @param f
	 */
	public void compile(Function f) {
		if (!f.needsCompiled() || f.getForm() == Function.Form.STUB) {
			return;
		}
		f.needsCompiled(false);
		
		CodeBlock block = new CodeBlock(f.tni, f.source, f);
		f.setCode(block);
		Scope scope = new Scope(block);
		
		for (Parameter param : f.getParams()) {
			scope.addVar(param.getName(), param.getType(), param.source);
		}
		for (TypeRef retType : f.getRetType()) {
			block.returnVars.add(scope.addTempVar(retType, null));
		}
		
		if (f.getForm() == Function.Form.BLOCK) {
			// block form
			for (StatContext stat : (List<StatContext>) f.getRawCode()) {
				compileStat(scope, stat, f.getRetType());
			}
		} else {
			// expr form
			List<Variable> vars = new ArrayList<>(block.returnVars);
			
			for (ExprContext expr : (List<ExprContext>) f.getRawCode()) {
				int used = compileExpr(scope, expr, vars);
				vars = vars.subList(0, used);
			}
		}
	}
	
	/**
	 * Compiles a field. Updates the contents of the argument.
	 * 
	 * @param f
	 */
	public void compile(Field f) {
		if (!f.needsCompiled()) {
			return;
		}
		f.needsCompiled(false);
		
		// TODO
	}
	
	/**
	 * Compiles a statement, placing the translated instructions in the provided code block.
	 * 
	 * @param scope The current scope. Instructions get placed in this scope's code block.
	 * @param rule The rule representing the statement.
	 * @param expectedType The expected return type of the block. May be null.
	 */
	public void compileStat(Scope scope, StatContext rule, List<TypeRef> expectedType) {
		// TODO
		
		TyphonBaseVisitor<Void> visitor = new TyphonBaseVisitor<Void>() {
			
		};
		
		visitor.visit(rule);
	}
	
	/**
	 * Compiles an expression, placing the translated instructions in the provided code block.
	 * It translates it into instructions that place the results of the expression into <tt>insertInto</tt>.
	 * 
	 * @param scope The current scope. Instructions get placed in this scope's code block.
	 * @param rule The rule representing the expression.
	 * @param insertInto The variables that the expression will be evaluated into in runtime.
	 * @return The number of variables that were filled.
	 */
	public int compileExpr(Scope scope, ExprContext rule, List<Variable> insertInto) {
		// TODO
		
		TyphonBaseVisitor<Integer> visitor = new TyphonBaseVisitor<Integer>() {
			
		};
		
		return visitor.visit(rule);
	}
	
	/**
	 * Produces the instructions and variable necessary to assign to the lvalue provided.
	 * 
	 * @param scope The current scope.
	 * @param rule The rule representing the lvalue.
	 * @param postfix This function inserts postfix instructions into this list.
	 * After you assign the expression to the variable returned, add these instructions to the code block.
	 * @return The variable you need to assign the rvalue to (before adding the postfix).
	 */
	public Variable compileLvalue(Scope scope, LvalueContext rule, List<Instruction> postfix) {
		// TODO
		
		TyphonBaseVisitor<Variable> visitor = new TyphonBaseVisitor<Variable>() {
			
		};
		
		return visitor.visit(rule);
	}
}
