package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;

import info.iconmaster.typhon.antlr.TyphonBaseVisitor;
import info.iconmaster.typhon.antlr.TyphonParser.AssignStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.DefStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.FuncCallExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.LvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.MemberExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.MemberLvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.NumConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParamNameContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParensExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarLvalueContext;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.compiler.LookupUtils.LookupArgument;
import info.iconmaster.typhon.compiler.LookupUtils.LookupElement;
import info.iconmaster.typhon.errors.DuplicateVarNameError;
import info.iconmaster.typhon.errors.ReadOnlyError;
import info.iconmaster.typhon.errors.TypeError;
import info.iconmaster.typhon.errors.UndefinedVariableError;
import info.iconmaster.typhon.errors.WriteOnlyError;
import info.iconmaster.typhon.model.CorePackage;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.model.TyphonModelReader;
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
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
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
						vars = vars.subList(Math.min(compileExpr(scope, expr, vars), vars.size()), vars.size());
					}
				}
				
				return null;
			}
			
			@Override
			public Void visitAssignStat(AssignStatContext ctx) {
				// move all the right hand sides to temp vars
				List<Variable> rhs = new ArrayList<>();
				
				for (LvalueContext lval : ctx.tnLvals) {
					Variable var = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(lval));
					rhs.add(var);
				}
				
				List<Variable> tempRhs = rhs;
				for (ExprContext expr : ctx.tnValues) {
					tempRhs = tempRhs.subList(Math.min(compileExpr(scope, expr, tempRhs), tempRhs.size()), tempRhs.size());
				}
				
				// assign to the lvalues
				int i = 0;
				for (LvalueContext lval : ctx.tnLvals) {
					Variable src = rhs.get(i);
					List<Instruction> postfix = new ArrayList<>();
					
					Variable dest = compileLvalue(scope, lval, postfix);
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.MOV, new Object[] {dest, src}));
					scope.getCodeBlock().ops.addAll(postfix);
					
					if (!src.type.canCastTo(dest.type)) {
						core.tni.errors.add(new TypeError(new SourceInfo(rule), src.type, dest.type));
					}
					
					i++;
				}
				
				return null;
			}
			
			@Override
			public Void visitExprStat(ExprStatContext ctx) {
				compileExpr(scope, ctx.tnExpr, Arrays.asList());
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
		if (rule instanceof ParensExprContext) {
			return compileExpr(scope, ((ParensExprContext) rule).tnExpr, insertInto);
		}
		
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
		TyphonBaseVisitor<List<TypeRef>> visitor = new TyphonBaseVisitor<List<TypeRef>>() {
			@Override
			public List<TypeRef> visitNumConstExpr(NumConstExprContext ctx) {
				if (insertInto.size() == 0) return Arrays.asList();
				
				// try to determine what type this constant should be
				Variable var = insertInto.get(0);
				OpCode op;
				Object arg;
				Type type;
				
				String text = ctx.tnValue.getText();
				if (text.contains(".") || text.contains("e")) {
					 if (var.type.getType() == core.TYPE_FLOAT) {
						op = OpCode.MOVFLOAT;
						arg = Float.parseFloat(text);
						type = var.type.getType();
					} else {
						op = OpCode.MOVDOUBLE;
						arg = Double.parseDouble(text);
						type = core.TYPE_DOUBLE;
					}
				} else {
					if (var.type.getType() == core.TYPE_BYTE || var.type.getType() == core.TYPE_UBYTE) {
						op = OpCode.MOVBYTE;
						arg = Byte.parseByte(text);
						type = var.type.getType();
					} else if (var.type.getType() == core.TYPE_DOUBLE || var.type.getType() == core.TYPE_REAL) {
						op = OpCode.MOVDOUBLE;
						arg = Double.parseDouble(text);
						type = core.TYPE_DOUBLE;
					} else if (var.type.getType() == core.TYPE_FLOAT) {
						op = OpCode.MOVFLOAT;
						arg = Float.parseFloat(text);
						type = var.type.getType();
					} else if (var.type.getType() == core.TYPE_LONG || var.type.getType() == core.TYPE_ULONG) {
						op = OpCode.MOVLONG;
						arg = Long.parseUnsignedLong(text);
						type = var.type.getType();
					} else if (var.type.getType() == core.TYPE_SHORT || var.type.getType() == core.TYPE_USHORT) {
						op = OpCode.MOVSHORT;
						arg = Short.parseShort(text);
						type = var.type.getType();
					} else if (var.type.getType() == core.TYPE_UINT) {
						op = OpCode.MOVINT;
						arg = Integer.parseUnsignedInt(text);
						type = var.type.getType();
					} else {
						op = OpCode.MOVINT;
						arg = Integer.parseUnsignedInt(text);
						type = core.TYPE_INT;
					}
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), op, new Object[] {var, arg}));
				return Arrays.asList(new TypeRef(type));
			}
			
			@Override
			public List<TypeRef> visitVarExpr(VarExprContext ctx) {
				if (insertInto.size() == 0) return Arrays.asList();
				
				MemberAccess access = scope;
				while (access != null) {
					List<MemberAccess> members = access.getMembers(ctx.tnValue.getText());
					
					for (MemberAccess member : members) {
						if (member instanceof Field) {
							Field f = (Field) member;
							Type fieldOf = f.getFieldOf();
							if (fieldOf == null) {
								// it's a static field!
								if (f.getGetter() == null) {
									// error; field is write-only
									core.tni.errors.add(new WriteOnlyError(new SourceInfo(ctx), f));
									return Arrays.asList(f.type);
								}
								
								scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALLSTATIC, new Object[] {Arrays.asList(insertInto.get(0)), f.getGetter(), new ArrayList<>()}));
								return Arrays.asList(f.type);
							} else if (scope.getCodeBlock().instance != null && fieldOf.equals(scope.getCodeBlock().instance.type.getType())) {
								// it's an instance field!
								if (f.getGetter() == null) {
									// error; field is write-only
									core.tni.errors.add(new WriteOnlyError(new SourceInfo(ctx), f));
									return Arrays.asList(f.type);
								}
								
								scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALL, new Object[] {Arrays.asList(insertInto.get(0)), scope.getCodeBlock().instance, f.getGetter(), new ArrayList<>()}));
								return Arrays.asList(f.type);
							}
						} else if (member instanceof Variable) {
							// it's a variable!
							Variable var = (Variable) member;
							scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.MOV, new Object[] {insertInto.get(0), var}));
							return Arrays.asList(var.type);
						}
					}
					
					access = access.getMemberParent();
				}
				
				// error, not found
				core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnValue.getText()));
				return Arrays.asList(TypeRef.var(core.tni));
			}
			
			@Override
			public List<TypeRef> visitMemberExpr(MemberExprContext ctx) {
				// TODO: we assume all are .'s and no .?'s
				
				// turn the rule into a list of member accesses
				List<LookupElement> names = new ArrayList<>();
				ExprContext expr = ctx;
				
				while (true) {
					if (expr instanceof MemberExprContext) {
						names.add(0, new LookupElement(((MemberExprContext) expr).tnValue.getText(), new SourceInfo(expr)));
						
						names.addAll(0, ((MemberExprContext) expr).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), template);
						}).collect(Collectors.toList()));
						
						expr = ((MemberExprContext) expr).tnLhs;
					} else if (expr instanceof VarExprContext) {
						names.add(0, new LookupElement(((VarExprContext) expr).tnValue.getText(), new SourceInfo(expr)));
						
						expr = null;
						break;
					} else {
						break;
					}
				}
				
				// create a list of possible member access routes
				MemberAccess base = scope;
				if (expr != null) {
					Variable exprVar = scope.addTempVar(TypeRef.var(core.tni), null);
					base = exprVar;
					
					compileExpr(scope, expr, Arrays.asList(exprVar));
				}
				
				List<List<MemberAccess>> paths = LookupUtils.findPaths(scope, base, names);
				if (paths.isEmpty()) {
					// error, no path found
					core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnValue.getText()));
					return Arrays.asList(TypeRef.var(core.tni));
				}
				
				// process the chosen path
				List<MemberAccess> path = paths.get(0);
				Variable var = LookupUtils.getSubjectOfPath(scope, path, names);
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.MOV, new Object[] {insertInto.get(0), var}));
				
				return Arrays.asList(var.type);
			}
			
			@Override
			public List<TypeRef> visitFuncCallExpr(FuncCallExprContext ctx) {
				// TODO: we assume all are .'s and no .?'s
				
				// turn the rule into a list of member accesses
				List<LookupElement> names = new ArrayList<>();
				ExprContext expr = ctx.tnCallee;
				
				while (true) {
					if (expr instanceof MemberExprContext) {
						names.add(0, new LookupElement(((MemberExprContext) expr).tnValue.getText(), new SourceInfo(expr)));
						
						names.addAll(0, ((MemberExprContext) expr).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), template);
						}).collect(Collectors.toList()));
						
						expr = ((MemberExprContext) expr).tnLhs;
					} else if (expr instanceof VarExprContext) {
						names.add(0, new LookupElement(((VarExprContext) expr).tnValue.getText(), new SourceInfo(expr)));
						
						expr = null;
						break;
					} else {
						break;
					}
				}
				
				// create a list of possible member access routes
				MemberAccess base = scope;
				if (expr != null) {
					Variable exprVar = scope.addTempVar(TypeRef.var(core.tni), null);
					base = exprVar;
					
					compileExpr(scope, expr, Arrays.asList(exprVar));
				}
				
				List<List<MemberAccess>> paths = LookupUtils.findPaths(scope, base, names);
				paths.removeIf((path)->{
					MemberAccess member = path.get(path.size()-1);
					
					if (member instanceof Field || member instanceof Function || member instanceof Variable) {
						return false;
					}
					
					return true;
				});
				
				if (paths.isEmpty()) {
					// error, no path found
					core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnCallee.getText()));
					return Arrays.asList(TypeRef.var(core.tni));
				}
				
				List<LookupArgument> args = new ArrayList<>();
				List<Variable> vars = new ArrayList<>();
				
				TyphonModelReader.readArgs(core.tni, ctx.tnArgs.tnArgs).stream().forEach((arg)->{
					Variable var = scope.addTempVar(TypeRef.var(core.tni), arg.source);
					compileExpr(scope, arg.getRawValue(), Arrays.asList(var));
					vars.add(var);
					
					args.add(new LookupArgument(var, arg.getLabel()));
				});
				
				paths.removeIf((path)->{
					MemberAccess member = path.get(path.size()-1);
					
					if (member instanceof Function) {
						Function f = (Function) member;
						
						// check if the argumnet's number/labels all match up to the signature
						Map<Parameter, Variable> map = LookupUtils.getFuncArgMap(f, args);
						if (map == null) {
							return true;
						}
						
						// check if the types match up to the signature
						for (Entry<Parameter, Variable> entry : map.entrySet()) {
							if (!entry.getValue().type.canCastTo(entry.getKey().getType())) {
								return true;
							}
						}
						
						return false;
					} else {
						// TODO: CALLFPTR
						return true;
					}
				});
				
				if (paths.isEmpty()) {
					// error, no path found
					core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnCallee.getText()));
					return Arrays.asList(TypeRef.var(core.tni));
				}
				
				// process the chosen path
				List<MemberAccess> path = paths.get(0);
				MemberAccess member = path.get(path.size()-1);
				
				if (member instanceof Function) {
					Function f = (Function) member;
					Type fieldOf = f.getFieldOf();
					
					List<Variable> outputVars = new ArrayList<>(insertInto.subList(0, Math.min(f.getRetType().size(), insertInto.size())));
					List<Variable> inputVars = new ArrayList<>();
					
					Map<Parameter, Variable> map = LookupUtils.getFuncArgMap(f, args);
					for (Parameter param : f.getParams()) {
						if (map.containsKey(param)) {
							inputVars.add(map.get(param));
						} else {
							Variable var = scope.addTempVar(param.getType(), new SourceInfo(ctx));
							// TODO: assign default value to variable
							inputVars.add(var);
						}
					}
					
					if (fieldOf == null) {
						// CALLSTATIC
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALLSTATIC, new Object[] {outputVars, f, inputVars}));
					} else {
						// CALL
						Variable instanceVar = LookupUtils.getSubjectOfPath(scope, path, names);
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALL, new Object[] {outputVars, instanceVar, f, inputVars}));
					}
					
					return f.getRetType().subList(0, outputVars.size());
				} else {
					// TODO: CALLFPTR
					return Arrays.asList();
				}
			}
		};
		
		List<TypeRef> a = visitor.visit(rule);
		if (a == null) return 0;
		
		int i = 0;
		for (TypeRef t : a) {
			if (i >= insertInto.size()) break;
			
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
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
		TyphonBaseVisitor<Variable> visitor = new TyphonBaseVisitor<Variable>() {
			@Override
			public Variable visitVarLvalue(VarLvalueContext ctx) {
				MemberAccess access = scope;
				while (access != null) {
					List<MemberAccess> members = access.getMembers(ctx.tnName.getText());
					
					for (MemberAccess member : members) {
						if (member instanceof Field) {
							Field f = (Field) member;
							Type fieldOf = f.getFieldOf();
							Variable var = scope.addTempVar(f.type, new SourceInfo(ctx));
							
							if (fieldOf == null) {
								// it's a static field!
								if (f.getSetter() == null) {
									// error; field is write-only
									core.tni.errors.add(new ReadOnlyError(new SourceInfo(ctx), f));
									return null;
								}
								
								postfix.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALLSTATIC, new Object[] {Arrays.asList(), f.getSetter(), Arrays.asList(var)}));
								return var;
							} else if (scope.getCodeBlock().instance != null && fieldOf.equals(scope.getCodeBlock().instance.type.getType())) {
								// it's an instance field!
								if (f.getSetter() == null) {
									// error; field is read-only
									core.tni.errors.add(new ReadOnlyError(new SourceInfo(ctx), f));
									return var;
								}
								
								postfix.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALL, new Object[] {Arrays.asList(), scope.getCodeBlock().instance, f.getSetter(), Arrays.asList(var)}));
								return var;
							}
						} else if (member instanceof Variable) {
							// it's a variable!
							return (Variable) member;
						}
					}
					
					access = access.getMemberParent();
				}
				
				// error, not found
				core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnName.getText()));
				return scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx));
			}
			
			@Override
			public Variable visitMemberLvalue(MemberLvalueContext ctx) {
				// TODO: we assume all are .'s and no .?'s
				
				// turn the rule into a list of member accesses
				List<LookupElement> names = new ArrayList<>();
				ParserRuleContext lval = ctx;
				
				while (true) {
					if (lval instanceof MemberLvalueContext) {
						if (((MemberLvalueContext) lval).tnRhs instanceof VarLvalueContext) {
							names.add(0, new LookupElement(((MemberLvalueContext) lval).tnRhs.getText(), new SourceInfo(lval)));
						} else {
							// TODO
						}
						
						names.addAll(0, ((MemberLvalueContext) lval).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), template);
						}).collect(Collectors.toList()));
						
						lval = ((MemberLvalueContext) lval).tnLhs;
					} else if (lval instanceof MemberExprContext) {
						names.add(0, new LookupElement(((MemberExprContext) lval).tnValue.getText(), new SourceInfo(lval)));
						
						names.addAll(0, ((MemberExprContext) lval).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), template);
						}).collect(Collectors.toList()));
						
						lval = ((MemberExprContext) lval).tnLhs;
					} else if (lval instanceof VarExprContext) {
						names.add(0, new LookupElement(((VarExprContext) lval).tnValue.getText(), new SourceInfo(lval)));
						
						lval = null;
						break;
					} else {
						break;
					}
				}
				
				// create a list of possible member access routes
				MemberAccess base = scope;
				if (lval instanceof ExprContext) {
					Variable exprVar = scope.addTempVar(TypeRef.var(core.tni), null);
					base = exprVar;
					
					compileExpr(scope, (ExprContext) lval, Arrays.asList(exprVar));
				}
				
				List<List<MemberAccess>> paths = LookupUtils.findPaths(scope, base, names);
				paths.removeIf((path)->!(path.get(path.size()-1) instanceof Field));
				
				if (paths.isEmpty()) {
					// error, no path found
					core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnRhs.getText()));
					return null;
				}
				List<MemberAccess> path = paths.get(0);
				
				// process the chosen path
				
				Field f = (Field) path.remove(path.size()-1);
				Type fieldOf = f.getFieldOf();
				
				if (f.getSetter() == null) {
					// error; field is read-only
					core.tni.errors.add(new ReadOnlyError(new SourceInfo(ctx), f));
					return null;
				}
				
				Variable var = scope.addTempVar(f.type, new SourceInfo(ctx));
				
				if (fieldOf == null) {
					postfix.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALLSTATIC, new Object[] {Arrays.asList(), f.getSetter(), Arrays.asList(var)}));
				} else {
					Variable fieldsInstance = LookupUtils.getSubjectOfPath(scope, path, names);
					postfix.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALL, new Object[] {Arrays.asList(), fieldsInstance, f.getSetter(), Arrays.asList(var)}));
				}
				
				return var;
			}
		};
		
		return visitor.visit(rule);
	}
}
