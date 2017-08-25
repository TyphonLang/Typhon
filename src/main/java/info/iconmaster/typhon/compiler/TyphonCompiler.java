package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import info.iconmaster.typhon.antlr.TyphonBaseVisitor;
import info.iconmaster.typhon.antlr.TyphonParser.DefStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.LvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.MemberExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.NumConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParamNameContext;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarExprContext;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.errors.DuplicateVarNameError;
import info.iconmaster.typhon.errors.TypeError;
import info.iconmaster.typhon.errors.UndefinedVariableError;
import info.iconmaster.typhon.errors.WriteOnlyError;
import info.iconmaster.typhon.model.CorePackage;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.TyphonTypeResolver;
import info.iconmaster.typhon.util.SourceInfo;

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
	public static void compile(Package p) {
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
	public static void compile(Function f) {
		if (!f.needsCompiled() || f.getForm() == Function.Form.STUB) {
			return;
		}
		f.needsCompiled(false);
		
		CodeBlock block = new CodeBlock(f.tni, f.source, f);
		f.setCode(block);
		Scope scope = new Scope(block);
		
		Type fieldOf = f.getFieldOf();
		if (fieldOf != null) {
			block.instance = scope.addTempVar(new TypeRef(fieldOf), null);
		}
		
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
				vars = vars.subList(used, vars.size());
			}
		}
	}
	
	/**
	 * Compiles a field. Updates the contents of the argument.
	 * 
	 * @param f
	 */
	public static void compile(Field f) {
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
	public static void compileStat(Scope scope, StatContext rule, List<TypeRef> expectedType) {
		TyphonBaseVisitor<Void> visitor = new TyphonBaseVisitor<Void>() {
			@Override
			public Void visitDefStat(DefStatContext ctx) {
				TypeRef type = TyphonTypeResolver.readType(scope.getCodeBlock().tni, ctx.tnType, scope.getCodeBlock().lookup);
				List<Variable> vars = new ArrayList<>();
				
				for (ParamNameContext name : ctx.tnNames) {
					String varName = name.tnName.getText();
					
					if (scope.inThisScope(varName)) {
						// error; variable exists already in scope
						scope.getCodeBlock().tni.errors.add(new DuplicateVarNameError(new SourceInfo(name), varName, scope.getVar(varName)));
						vars.add(scope.addTempVar(type.copy(), new SourceInfo(name)));
					} else {
						vars.add(scope.addVar(varName, type.copy(), new SourceInfo(name)));
					}
				}
				
				if (ctx.tnValues != null) {
					for (ExprContext expr : ctx.tnValues) {
						vars = vars.subList(compileExpr(scope, expr, vars), vars.size());
					}
				}
				
				return null;
			}
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
	public static int compileExpr(Scope scope, ExprContext rule, List<Variable> insertInto) {
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
		TyphonBaseVisitor<List<TypeRef>> visitor = new TyphonBaseVisitor<List<TypeRef>>() {
			@Override
			public List<TypeRef> visitNumConstExpr(NumConstExprContext ctx) {
				if (insertInto.size() == 0) return Arrays.asList();
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.MOVINT, new Object[] {insertInto.get(0), ctx.tnValue.getText()}));
				return Arrays.asList(new TypeRef(core.TYPE_INT));
			}
			
			@Override
			public List<TypeRef> visitVarExpr(VarExprContext ctx) {
				if (insertInto.size() == 0) return Arrays.asList();
				
				List<MemberAccess> access = lookup(scope, Arrays.asList(ctx.tnValue.getText()));
				access = access.stream().filter((a)->a instanceof Field || a instanceof Variable).collect(Collectors.toList());
				
				if (!access.isEmpty()) {
					if (access.get(0) instanceof Field) {
						// it's a field
						Field f = (Field) access.get(0);
						
						if (f.getGetter() == null) {
							// error; field is write-only
							core.tni.errors.add(new WriteOnlyError(new SourceInfo(ctx), f));
							return Arrays.asList(f.type);
						}
						
						Type fieldOf = f.getFieldOf();
						if (fieldOf == null) {
							scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALLSTATIC, new Object[] {Arrays.asList(insertInto.get(0)), f.getGetter(), new ArrayList<>()}));
						} else {
							scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALL, new Object[] {Arrays.asList(insertInto.get(0)), scope.getCodeBlock().instance, f.getGetter(), new ArrayList<>()}));
						}
						
						return Arrays.asList(f.type);
					} else {
						// it's a local variable
						Variable var = (Variable) access.get(0);
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.MOV, new Object[] {insertInto.get(0), var}));
						return Arrays.asList(var.type);
					}
				} else {
					// error, not found
					core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnValue.getText()));
					return Arrays.asList(new TypeRef(core.TYPE_ANY));
				}
			}
			
			@Override
			public List<TypeRef> visitMemberExpr(MemberExprContext ctx) {
				// TODO: we assume all are .'s and no .?'s
				//List<ParserRuleContext> rules = new ArrayList<>();
				List<String> names = new ArrayList<>();
				ExprContext expr = ctx;
				
				while (true) {
					if (expr instanceof MemberExprContext) {
						//rules.addAll(0, ((MemberExprContext) expr).tnValue);
						names.add(0, ((MemberExprContext) expr).tnValue.getText());
						
						//rules.addAll(0, ((MemberExprContext) expr).tnLookup);
						names.addAll(0, ((MemberExprContext) expr).tnLookup.stream().map((e)->e.tnName.getText()).collect(Collectors.toList()));
						
						expr = ((MemberExprContext) expr).tnLhs;
					} else if (expr instanceof VarExprContext) {
						//rules.add(0, ((VarExprContext) expr).tnValue);
						names.add(0, ((VarExprContext) expr).tnValue.getText());
						expr = null;
						break;
					} else {
						break;
					}
				}
				
				Variable exprVar = null;
				MemberAccess base;
				if (expr == null) {
					base = scope;
				} else {
					TypeRef t = TypeRef.var(core.tni);
					exprVar = scope.addTempVar(t, null);
					compileExpr(scope, expr, Arrays.asList(exprVar));
					base = exprVar.type;
				}
				
				List<MemberAccess> access = lookup(base, names);
				access = access.stream().filter((a)->a instanceof Field).collect(Collectors.toList());
				
				if (!access.isEmpty()) {
					// it's a field
					Field f = (Field)access.get(0);
					
					if (f.getGetter() == null) {
						// error; field is write-only
						core.tni.errors.add(new WriteOnlyError(new SourceInfo(ctx), f));
						return Arrays.asList(f.type);
					}
					
					Type fieldOf = f.getFieldOf();
					if (fieldOf == null) {
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALLSTATIC, new Object[] {Arrays.asList(insertInto.get(0)), f.getGetter(), new ArrayList<>()}));
					} else {
						// TODO: find the subject of the call
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALL, new Object[] {Arrays.asList(insertInto.get(0)), null, f.getGetter(), new ArrayList<>()}));
					}
					
					return Arrays.asList(f.type);
				} else {
					// error, not found
					core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnValue.getText()));
					return Arrays.asList(new TypeRef(core.TYPE_ANY));
				}
			}
		};
		
		List<TypeRef> a = visitor.visit(rule);
		if (a == null) return 0;
		
		int i = 0;
		for (TypeRef t : a) {
			if (insertInto.get(i).type.isVar()) {
				insertInto.get(i).type = t;
			} else if (!t.canCastTo(insertInto.get(i).type)) {
				core.tni.errors.add(new TypeError(new SourceInfo(rule), t, insertInto.get(i).type));
			}
			i++;
		}
		
		return a.size();
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
	public static Variable compileLvalue(Scope scope, LvalueContext rule, List<Instruction> postfix) {
		// TODO
		
		TyphonBaseVisitor<Variable> visitor = new TyphonBaseVisitor<Variable>() {
			
		};
		
		return visitor.visit(rule);
	}
	
	public static List<MemberAccess> lookup(MemberAccess base, List<String> members) {
		List<MemberAccess> result = new ArrayList<>();
		
		while (base != null) {
			List<MemberAccess> matches = new ArrayList<>();
			matches.add(base);
			
			for (String name : members) {
				List<MemberAccess> newMatches = new ArrayList<>();
				
				for (MemberAccess match : matches) {
					newMatches.addAll(match.getMembers(name));
				}
				
				matches = newMatches;
			}
			
			result.addAll(matches);
			base = base.getMemberParent();
		}
		
		return result;
	}
}
