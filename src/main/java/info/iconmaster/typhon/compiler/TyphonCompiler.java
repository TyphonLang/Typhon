package info.iconmaster.typhon.compiler;

import java.util.List;

import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.types.TypeRef;

public class TyphonCompiler {
	private TyphonCompiler() {}
	
	public void compile(Package p) {
		if (!p.needsCompiled()) {
			return;
		}
		p.needsCompiled(false);
		
		p.getFunctions().stream().forEach((f)->compile(f));
		p.getFields().stream().forEach((f)->compile(f));
		
		p.getSubpackges().stream().forEach((f)->compile(f));
	}
	
	public void compile(Function f) {
		if (!f.needsCompiled()) {
			return;
		}
		f.needsCompiled(false);
		
		// TODO
	}
	
	public void compile(Field f) {
		if (!f.needsCompiled()) {
			return;
		}
		f.needsCompiled(false);
		
		// TODO
	}
	
	public CodeBlock compileBlock(List<StatContext> rules, List<TypeRef> expectedType) {
		// TODO
		return null;
	}
	
	public void compileBlock(Scope scope, List<StatContext> rules, List<TypeRef> expectedType) {
		// TODO
	}
	
	public CodeBlock compileExpr(ExprContext rule, List<TypeRef> expectedType) {
		// TODO
		return null;
	}
	
	public void compileExpr(Scope scope, ExprContext rule, List<Variable> insertInto, List<TypeRef> expectedType) {
		// TODO
	}
}
