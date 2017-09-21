package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;

import info.iconmaster.typhon.antlr.TyphonBaseVisitor;
import info.iconmaster.typhon.antlr.TyphonParser.AssignStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.BinOps1ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.BinOps2ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.BitOpsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.BlockContext;
import info.iconmaster.typhon.antlr.TyphonParser.BlockStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.BreakStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.CastExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.CharConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ContStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.DefStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.FalseConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.FuncCallExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.IfStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.LvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.MemberExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.MemberLvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.NullConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.NumConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParamNameContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParensExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.RelOpsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.RepeatStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.antlr.TyphonParser.StringConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ThisConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TrueConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.UnOpsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarLvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.WhileStatContext;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.errors.DuplicateVarNameError;
import info.iconmaster.typhon.errors.LabelNotFoundError;
import info.iconmaster.typhon.errors.NotAllowedHereError;
import info.iconmaster.typhon.errors.ReadOnlyError;
import info.iconmaster.typhon.errors.StringFormatError;
import info.iconmaster.typhon.errors.ThisInStaticContextError;
import info.iconmaster.typhon.errors.TypeError;
import info.iconmaster.typhon.errors.UndefinedOperatorError;
import info.iconmaster.typhon.errors.UndefinedVariableError;
import info.iconmaster.typhon.errors.WriteOnlyError;
import info.iconmaster.typhon.model.AnnotationDefinition;
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
import info.iconmaster.typhon.util.LookupUtils;
import info.iconmaster.typhon.util.LookupUtils.LookupArgument;
import info.iconmaster.typhon.util.LookupUtils.LookupElement;
import info.iconmaster.typhon.util.LookupUtils.LookupElement.AccessType;
import info.iconmaster.typhon.util.LookupUtils.LookupPath;
import info.iconmaster.typhon.util.LookupUtils.LookupPath.Subject;
import info.iconmaster.typhon.util.SourceInfo;
import info.iconmaster.typhon.util.StringUtils;
import info.iconmaster.typhon.util.TemplateUtils;

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
			param.setVar(scope.addVar(param.getName(), param.getType(), param.source));
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
			
			@Override
			public Void visitBlockStat(BlockStatContext ctx) {
				Label beginLabel;
				Label endLabel;
				
				Scope newScope = new Scope(scope.getCodeBlock(), scope);
				
				if (ctx.tnBlock.tnLabel != null) {
					beginLabel = newScope.addLabel(ctx.tnBlock.tnLabel.getText()+":begin");
				} else {
					beginLabel = newScope.addTempLabel();
				}
				
				if (ctx.tnBlock.tnLabel != null) {
					endLabel = newScope.addLabel(ctx.tnBlock.tnLabel.getText()+":end");
				} else {
					endLabel = newScope.addTempLabel();
				}
				
				newScope.beginScopeLabel = beginLabel; newScope.endScopeLabel = endLabel;
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnBlock), OpCode.LABEL, new Object[] {beginLabel}));
				
				for (StatContext stat : ctx.tnBlock.tnBlock) {
					compileStat(newScope, stat, expectedType);
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnBlock), OpCode.LABEL, new Object[] {endLabel}));
				return null;
			}
			
			@Override
			public Void visitWhileStat(WhileStatContext ctx) {
				Label beginLabel;
				Label endLabel;
				
				Scope newScope = new Scope(scope.getCodeBlock(), scope);
				
				if (ctx.tnBlock.tnLabel != null) {
					beginLabel = newScope.addLabel(ctx.tnBlock.tnLabel.getText()+":begin");
				} else {
					beginLabel = newScope.addTempLabel();
				}
				
				if (ctx.tnBlock.tnLabel != null) {
					endLabel = newScope.addLabel(ctx.tnBlock.tnLabel.getText()+":end");
				} else {
					endLabel = newScope.addTempLabel();
				}
				
				newScope.beginScopeLabel = beginLabel; newScope.endScopeLabel = endLabel;
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {beginLabel}));
				
				Variable condVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(ctx.tnExpr));
				compileExpr(scope, ctx.tnExpr, Arrays.asList(condVar));
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnExpr), OpCode.NOT, new Object[] {condVar, condVar}));
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnExpr), OpCode.JUMPIF, new Object[] {condVar, endLabel}));
				
				for (StatContext stat : ctx.tnBlock.tnBlock) {
					compileStat(newScope, stat, expectedType);
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {beginLabel}));
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {endLabel}));
				
				return null;
			}
			
			@Override
			public Void visitRepeatStat(RepeatStatContext ctx) {
				Label beginLabel;
				Label endLabel;
				
				Scope newScope = new Scope(scope.getCodeBlock(), scope);
				
				if (ctx.tnBlock.tnLabel != null) {
					beginLabel = newScope.addLabel(ctx.tnBlock.tnLabel.getText()+":begin");
				} else {
					beginLabel = newScope.addTempLabel();
				}
				
				if (ctx.tnBlock.tnLabel != null) {
					endLabel = newScope.addLabel(ctx.tnBlock.tnLabel.getText()+":end");
				} else {
					endLabel = newScope.addTempLabel();
				}
				
				newScope.beginScopeLabel = beginLabel; newScope.endScopeLabel = endLabel;
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {beginLabel}));
				
				for (StatContext stat : ctx.tnBlock.tnBlock) {
					compileStat(newScope, stat, expectedType);
				}
				
				Variable condVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(ctx.tnExpr));
				compileExpr(newScope, ctx.tnExpr, Arrays.asList(condVar));
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnExpr), OpCode.NOT, new Object[] {condVar, condVar}));
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnExpr), OpCode.JUMPIF, new Object[] {condVar, beginLabel}));
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {endLabel}));
				
				return null;
			}
			
			@Override
			public Void visitBreakStat(BreakStatContext ctx) {
				Label label = null;
				
				if (ctx.tnLabel == null) {
					Scope search = scope;
					while (search != null && label == null) {
						label = search.endScopeLabel;
						search = search.getParent();
					}
					
					if (label == null) {
						// error; can't break here
						core.tni.errors.add(new NotAllowedHereError(new SourceInfo(ctx), "break statements"));
						return null;
					}
				} else {
					String s = ctx.tnLabel.getText()+":end";
					Scope search = scope;
					while (search != null) {
						if (search.endScopeLabel != null && search.endScopeLabel.name != null && search.endScopeLabel.name.equals(s)) {
							label = search.endScopeLabel;
							break;
						}
						
						search = search.getParent();
					}
					
					if (label == null) {
						// error; break not found
						core.tni.errors.add(new LabelNotFoundError(new SourceInfo(ctx), ctx.tnLabel.getText()));
						return null;
					}
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {label}));
				
				return null;
			}
			
			@Override
			public Void visitContStat(ContStatContext ctx) {
				Label label = null;
				
				if (ctx.tnLabel == null) {
					Scope search = scope;
					while (search != null && label == null) {
						label = search.beginScopeLabel;
						search = search.getParent();
					}
					
					if (label == null) {
						// error; can't break here
						core.tni.errors.add(new NotAllowedHereError(new SourceInfo(ctx), "continue statements"));
						return null;
					}
				} else {
					String s = ctx.tnLabel.getText()+":begin";
					Scope search = scope;
					while (search != null) {
						if (search.beginScopeLabel != null && search.beginScopeLabel.name != null && search.beginScopeLabel.name.equals(s)) {
							label = search.beginScopeLabel;
							break;
						}
						
						search = search.getParent();
					}
					
					if (label == null) {
						// error; break not found
						core.tni.errors.add(new LabelNotFoundError(new SourceInfo(ctx), ctx.tnLabel.getText()));
						return null;
					}
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {label}));
				
				return null;
			}
			
			@Override
			public Void visitIfStat(IfStatContext ctx) {
				Label allEndLabel = scope.addTempLabel();
				List<Label> labels = new ArrayList<>();
				
				// add all the labels in advance
				if (ctx.tnIfBlock.tnLabel != null) {
					core.tni.errors.add(new NotAllowedHereError(new SourceInfo(ctx.tnIfBlock.tnLabel), "block label"));
				}
				
				for (BlockContext block : ctx.tnElseifBlocks) {
					if (block.tnLabel != null) {
						core.tni.errors.add(new NotAllowedHereError(new SourceInfo(block.tnLabel), "block label"));
					}
					
					labels.add(scope.addTempLabel());
				}
				
				if (ctx.tnElseBlock != null) {
					if (ctx.tnElseBlock.tnLabel != null) {
						core.tni.errors.add(new NotAllowedHereError(new SourceInfo(ctx.tnElseBlock.tnLabel), "block label"));
					}
					
					labels.add(scope.addTempLabel());
				}
				
				labels.add(allEndLabel);
				
				// parse the 'if' block
				{
					Variable condVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(ctx.tnIfExpr));
					compileExpr(scope, ctx.tnIfExpr, Arrays.asList(condVar));
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.NOT, new Object[] {condVar, condVar}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPIF, new Object[] {labels.get(0)}));
					
					for (StatContext stat : ctx.tnIfBlock.tnBlock) {
						compileStat(scope, stat, expectedType);
					}
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {allEndLabel}));
				}
				
				// parse the 'elseif' blocks
				int i = 0;
				for (ExprContext cond : ctx.tnElseifExprs) {
					BlockContext block = ctx.tnElseifBlocks.get(i);
					Label label = labels.remove(0);
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {label}));
					
					Variable condVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(cond));
					compileExpr(scope, cond, Arrays.asList(condVar));
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.NOT, new Object[] {condVar, condVar}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPIF, new Object[] {labels.get(0)}));
					
					for (StatContext stat : block.tnBlock) {
						compileStat(scope, stat, expectedType);
					}
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {allEndLabel}));
				}
				
				// parse the 'else' block
				if (ctx.tnElseBlock != null) {
					Label label = labels.remove(0);
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {label}));
					
					for (StatContext stat : ctx.tnElseBlock.tnBlock) {
						compileStat(scope, stat, expectedType);
					}
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {allEndLabel}));
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
					List<MemberAccess> members = access.getMembers(ctx.tnValue.getText(), scope.getCodeBlock().instance == null ? new HashMap<>() : TemplateUtils.matchAllTemplateArgs(scope.getCodeBlock().instance.type));
					
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
				// turn the rule into a list of member accesses
				List<LookupElement> names = new ArrayList<>();
				ExprContext expr = ctx;
				
				while (true) {
					if (expr instanceof MemberExprContext) {
						names.add(0, new LookupElement(((MemberExprContext) expr).tnValue.getText(), new SourceInfo(expr), AccessType.get(((MemberExprContext) expr).tnOp.getText())));
						
						names.addAll(0, ((MemberExprContext) expr).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate == null ? Arrays.asList() : e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), AccessType.get(e.tnOp.getText()), template);
						}).collect(Collectors.toList()));
						
						expr = ((MemberExprContext) expr).tnLhs;
					} else if (expr instanceof VarExprContext) {
						names.add(0, new LookupElement(((VarExprContext) expr).tnValue.getText(), new SourceInfo(expr), null));
						
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
				
				List<LookupPath> paths = LookupUtils.findPaths(scope, base, names);
				if (paths.isEmpty()) {
					// error, no path found
					core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnValue.getText()));
					return Arrays.asList(TypeRef.var(core.tni));
				}
				
				// process the chosen path
				LookupPath path = paths.get(0);
				Variable var = LookupUtils.getSubjectOfPath(scope, path);
				
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.MOV, new Object[] {insertInto.get(0), var}));
				}
				
				return Arrays.asList(var.type);
			}
			
			@Override
			public List<TypeRef> visitFuncCallExpr(FuncCallExprContext ctx) {
				// turn the rule into a list of member accesses
				List<LookupElement> names = new ArrayList<>();
				ExprContext expr = ctx.tnCallee;
				
				while (true) {
					if (expr instanceof MemberExprContext) {
						names.add(0, new LookupElement(((MemberExprContext) expr).tnValue.getText(), new SourceInfo(expr), AccessType.get(((MemberExprContext) expr).tnOp.getText())));
						
						names.addAll(0, ((MemberExprContext) expr).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), AccessType.get(e.tnOp.getText()), template);
						}).collect(Collectors.toList()));
						
						expr = ((MemberExprContext) expr).tnLhs;
					} else if (expr instanceof VarExprContext) {
						names.add(0, new LookupElement(((VarExprContext) expr).tnValue.getText(), new SourceInfo(expr), null));
						
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
				
				List<LookupPath> paths = LookupUtils.findPaths(scope, base, names);
				
				List<LookupArgument> args = new ArrayList<>();
				List<Variable> vars = new ArrayList<>();
				
				Map<Variable, ExprContext> argmap = new HashMap<>();
				TyphonModelReader.readArgs(core.tni, ctx.tnArgs.tnArgs).stream().forEach((arg)->{
					Variable var = scope.addTempVar(TypeRef.var(core.tni), arg.source);
					vars.add(var);
					
					args.add(new LookupArgument(var, arg.getLabel()));
					
					argmap.put(var, arg.getRawValue());
				});
				
				paths.removeIf((path)->{
					MemberAccess member = path.members.get(path.members.size()-1);
					
					if (member instanceof Function) {
						Function f = (Function) member;
						
						// check if the argumnet's number/labels all match up to the signature
						Map<Parameter, Variable> map = LookupUtils.getFuncArgMap(f, args);
						if (map == null) {
							return true;
						}
						
						// check if the types match up to the signature
						for (Entry<Parameter, Variable> entry : map.entrySet()) {
							if (!getExprType(scope, argmap.get(entry.getValue()), Arrays.asList(entry.getKey().getType())).get(0).canCastTo(entry.getKey().getType())) {
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
				LookupPath path = paths.get(0);
				Subject sub = path.popSubject();
				
				if (sub.member instanceof Function) {
					Function f = (Function) sub.member;
					Type fieldOf = f.getFieldOf();
					
					List<Variable> outputVars = new ArrayList<>(insertInto.subList(0, Math.min(f.getRetType().size(), insertInto.size())));
					List<Variable> inputVars = new ArrayList<>();
					
					Map<Parameter, Variable> map = LookupUtils.getFuncArgMap(f, args);
					
					for (Parameter param : f.getParams()) {
						if (map.containsKey(param)) {
							inputVars.add(map.get(param));
							map.get(param).type = getExprType(scope, argmap.get(map.get(param)), Arrays.asList(param.getType())).get(0);
							
							compileExpr(scope, argmap.get(map.get(param)), Arrays.asList(map.get(param)));
						} else {
							Variable var = scope.addTempVar(param.getType(), new SourceInfo(ctx));
							// TODO: assign default value to variable
							inputVars.add(var);
						}
					}
					
					Variable instanceVar = LookupUtils.getSubjectOfPath(scope, path);
					
					if (fieldOf == null) {
						// CALLSTATIC
						if (sub.infix == AccessType.NULLABLE_DOT || sub.infix == AccessType.DOUBLE_DOT) {
							// TODO: error
						}
						
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALLSTATIC, new Object[] {outputVars, f, inputVars}));
					} else {
						// CALL
						Label label = null;
						if (sub.infix == AccessType.NULLABLE_DOT) {
							label = scope.addTempLabel();
							
							Variable tempVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), sub.source);
							scope.getCodeBlock().ops.add(new Instruction(core.tni, sub.source, OpCode.ISNULL, new Object[] {tempVar, instanceVar}));
							scope.getCodeBlock().ops.add(new Instruction(core.tni, sub.source, OpCode.JUMPIF, new Object[] {tempVar, label}));
						}
						
						if (sub.infix == AccessType.DOUBLE_DOT) {
							outputVars = Arrays.asList();
						}
						
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALL, new Object[] {outputVars, instanceVar, f, inputVars}));
						
						if (sub.infix == AccessType.DOUBLE_DOT && !insertInto.isEmpty()) {
							scope.getCodeBlock().ops.add(new Instruction(core.tni, sub.source, OpCode.MOV, new Object[] {instanceVar, insertInto.get(0)}));
						}
						
						if (label != null) {
							scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, sub.source, OpCode.LABEL, new Object[] {label}));
						}
						
						if (sub.infix == AccessType.DOUBLE_DOT) {
							return Arrays.asList(instanceVar.type);
						}
					}
					
					return f.getRetType().subList(0, outputVars.size());
				} else {
					// TODO: CALLFPTR
					return Arrays.asList();
				}
			}
			
			@Override
			public List<TypeRef> visitBinOps1Expr(BinOps1ExprContext ctx) {
				return compileBinOp(scope, ctx.tnLhs, ctx.tnRhs, ctx.tnOp.getText(), insertInto);
			}
			
			@Override
			public List<TypeRef> visitBinOps2Expr(BinOps2ExprContext ctx) {
				return compileBinOp(scope, ctx.tnLhs, ctx.tnRhs, ctx.tnOp.getText(), insertInto);
			}
			
			@Override
			public List<TypeRef> visitBitOpsExpr(BitOpsExprContext ctx) {
				String op = ctx.tnOp.getText();
				if (ctx.tnOp2 != null) op += ctx.tnOp2.getText();
				
				return compileBinOp(scope, ctx.tnLhs, ctx.tnRhs, op, insertInto);
			}
			
			@Override
			public List<TypeRef> visitRelOpsExpr(RelOpsExprContext ctx) {
				return compileBinOp(scope, ctx.tnLhs, ctx.tnRhs, ctx.tnOp.getText(), insertInto);
			}
			
			@Override
			public List<TypeRef> visitCastExpr(CastExprContext ctx) {
				TypeRef newType = TyphonTypeResolver.readType(core.tni, ctx.tnRhs, scope);
				TypeRef oldType = getExprType(scope, ctx.tnLhs, Arrays.asList(newType)).get(0);
				
				TypeRef oldInsertIntoType = null;
				if (!insertInto.isEmpty()) {
					// we temporarily alter the type of the var to the old one, so the expression compiles correctly
					oldInsertIntoType = insertInto.get(0).type;
					insertInto.get(0).type = oldType;
				}
				
				compileExpr(scope, ctx.tnLhs, insertInto.isEmpty() ? Arrays.asList() : Arrays.asList(insertInto.get(0)));
				
				if (!insertInto.isEmpty()) {
					// restore the changes to the type
					insertInto.get(0).type = oldInsertIntoType;
				}
				
				if (!insertInto.isEmpty() && ctx.tnOp.getText().equals("as?")) {
					Label label = scope.addTempLabel();
					Variable var1 = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(ctx));
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.INSTANCEOF, new Object[] {var1, insertInto.get(0), newType}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPIF, new Object[] {label, var1}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVNULL, new Object[] {insertInto.get(0)}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {label}));
				}
				
				return Arrays.asList(newType);
			}
			
			@Override
			public List<TypeRef> visitUnOpsExpr(UnOpsExprContext ctx) {
				CorePackage core = scope.getCodeBlock().tni.corePackage;
				
				Variable lhs = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx.tnArg));
				compileExpr(scope, ctx.tnArg, Arrays.asList(lhs));
				
				AnnotationDefinition operator;
				
				switch (ctx.tnOp.getText()) {
				case "-":
					operator = core.LIB_OPS.ANNOT_NEG;
					break;
				case "+":
					operator = core.LIB_OPS.ANNOT_POS;
					break;
				case "~":
					operator = core.LIB_OPS.ANNOT_BNOT;
					break;
				case "!":
					if (!lhs.type.canCastTo(new TypeRef(core.TYPE_BOOL))) {
						core.tni.errors.add(new TypeError(new SourceInfo(ctx), lhs.type, new TypeRef(core.TYPE_BOOL)));
					}
					
					if (!insertInto.isEmpty()) scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.NOT, new Object[] {insertInto.get(0), lhs}));
					
					return Arrays.asList(new TypeRef(core.TYPE_BOOL));
				default:
					throw new IllegalArgumentException("unknown operator in getUnOp: "+ctx.tnOp.getText());
				}
				
				List<Function> handlers = lhs.type.getType().getOperatorHandlers(operator);
				handlers.removeIf((f)->{
					if (f.getParams().size() != 0) {
						return true;
					}
					
					return false;
				});
				
				if (handlers.isEmpty()) {
					// error; handler not found
					core.tni.errors.add(new UndefinedOperatorError(new SourceInfo(ctx.tnArg), ctx.tnOp.getText(), lhs.type, null));
					return Arrays.asList(TypeRef.var(core.tni));
				}
				
				Function handler = handlers.get(0);
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnArg), OpCode.CALL, new Object[] {insertInto.isEmpty() ? Arrays.asList() : Arrays.asList(insertInto.get(0)), lhs, handler, Arrays.asList()}));
				
				return handler.getRetType();
			}
			
			@Override
			public List<TypeRef> visitCharConstExpr(CharConstExprContext ctx) {
				String s = StringUtils.formatTyphonString(ctx.tnValue.getText());
				if (s == null || s.length() != 1) {
					// error; string format bad
					core.tni.errors.add(new StringFormatError(new SourceInfo(ctx), ctx.tnValue.getText()));
					return Arrays.asList(new TypeRef(core.TYPE_CHAR));
				}
				
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVCHAR, new Object[] {insertInto.get(0), s.charAt(0)}));
				}
				
				return Arrays.asList(new TypeRef(core.TYPE_CHAR));
			}
			
			@Override
			public List<TypeRef> visitStringConstExpr(StringConstExprContext ctx) {
				String s = StringUtils.formatTyphonString(ctx.tnValue.getText());
				if (s == null) {
					// error; string format bad
					core.tni.errors.add(new StringFormatError(new SourceInfo(ctx), ctx.tnValue.getText()));
					return Arrays.asList(new TypeRef(core.TYPE_CHAR));
				}
				
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVSTR, new Object[] {insertInto.get(0), s}));
				}
				
				return Arrays.asList(new TypeRef(core.TYPE_STRING));
			}
			
			@Override
			public List<TypeRef> visitFalseConstExpr(FalseConstExprContext ctx) {
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVFALSE, new Object[] {insertInto.get(0)}));
				}
				
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
			}
			
			@Override
			public List<TypeRef> visitNullConstExpr(NullConstExprContext ctx) {
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVNULL, new Object[] {insertInto.get(0)}));
				}
				
				return Arrays.asList(new TypeRef(core.TYPE_ANY));
			}
			
			@Override
			public List<TypeRef> visitThisConstExpr(ThisConstExprContext ctx) {
				if (scope.getCodeBlock().instance == null) {
					// error; accessing 'this' in static context
					core.tni.errors.add(new ThisInStaticContextError(new SourceInfo(ctx)));
					return Arrays.asList(new TypeRef(core.TYPE_ANY));
				}
				
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOV, new Object[] {insertInto.get(0), scope.getCodeBlock().instance}));
				}
				
				return Arrays.asList(scope.getCodeBlock().instance.type);
			}
			
			@Override
			public List<TypeRef> visitTrueConstExpr(TrueConstExprContext ctx) {
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVTRUE, new Object[] {insertInto.get(0)}));
				}
				
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
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
	 * Returns the type of an expression rule.
	 * 
	 * @param scope The current scope.
	 * @param rule The rule representing the expression.
	 * @param expectedTypes What the compiler expects the type to be. May be empty.
	 * @return A list of at least one type.
	 */
	public static List<TypeRef> getExprType(Scope scope, ExprContext rule, List<TypeRef> expectedTypes) {
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
		TyphonBaseVisitor<List<TypeRef>> visitor = new TyphonBaseVisitor<List<TypeRef>>() {
			@Override
			public List<TypeRef> visitNumConstExpr(NumConstExprContext ctx) {
				// try to determine what type this constant should be
				TypeRef var = expectedTypes.isEmpty() ? null : expectedTypes.get(0);
				
				String text = ctx.tnValue.getText();
				if (var == null) {
					if (text.contains(".") || text.contains("e")) {
						return Arrays.asList(new TypeRef(core.TYPE_DOUBLE));
					} else {
						return Arrays.asList(new TypeRef(core.TYPE_INT));
					}
				} else {
					if (var.getType() == core.TYPE_BYTE || var.getType() == core.TYPE_UBYTE) {
						return Arrays.asList(var);
					} else if (var.getType() == core.TYPE_DOUBLE || var.getType() == core.TYPE_REAL) {
						return Arrays.asList(var);
					} else if (var.getType() == core.TYPE_FLOAT) {
						return Arrays.asList(var);
					} else if (var.getType() == core.TYPE_LONG || var.getType() == core.TYPE_ULONG) {
						return Arrays.asList(var);
					} else if (var.getType() == core.TYPE_SHORT || var.getType() == core.TYPE_USHORT) {
						return Arrays.asList(var);
					} else if (var.getType() == core.TYPE_UINT) {
						return Arrays.asList(var);
					} else {
						if (text.contains(".") || text.contains("e")) {
							return Arrays.asList(new TypeRef(core.TYPE_DOUBLE));
						} else {
							return Arrays.asList(new TypeRef(core.TYPE_INT));
						}
					}
				}
			}
			
			@Override
			public List<TypeRef> visitVarExpr(VarExprContext ctx) {
				MemberAccess access = scope;
				while (access != null) {
					List<MemberAccess> members = access.getMembers(ctx.tnValue.getText(), scope.getCodeBlock().instance == null ? new HashMap<>() : TemplateUtils.matchAllTemplateArgs(scope.getCodeBlock().instance.type));
					
					for (MemberAccess member : members) {
						if (member instanceof Field) {
							Field f = (Field) member;
							return Arrays.asList(f.type);
						} else if (member instanceof Variable) {
							Variable var = (Variable) member;
							return Arrays.asList(var.type);
						}
					}
					
					access = access.getMemberParent();
				}
				
				// error, not found
				return Arrays.asList(TypeRef.var(core.tni));
			}
			
			@Override
			public List<TypeRef> visitMemberExpr(MemberExprContext ctx) {
				// turn the rule into a list of member accesses
				List<LookupElement> names = new ArrayList<>();
				ExprContext expr = ctx;
				
				while (true) {
					if (expr instanceof MemberExprContext) {
						names.add(0, new LookupElement(((MemberExprContext) expr).tnValue.getText(), new SourceInfo(expr), AccessType.get(((MemberExprContext) expr).tnOp.getText())));
						
						names.addAll(0, ((MemberExprContext) expr).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate == null ? Arrays.asList() : e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), AccessType.get(e.tnOp.getText()), template);
						}).collect(Collectors.toList()));
						
						expr = ((MemberExprContext) expr).tnLhs;
					} else if (expr instanceof VarExprContext) {
						names.add(0, new LookupElement(((VarExprContext) expr).tnValue.getText(), new SourceInfo(expr), null));
						
						expr = null;
						break;
					} else {
						break;
					}
				}
				
				// create a list of possible member access routes
				MemberAccess base = scope;
				if (expr != null) {
					base = getExprType(scope, expr, Arrays.asList()).get(0);
				}
				
				List<LookupPath> paths = LookupUtils.findPaths(scope, base, names);
				if (paths.isEmpty()) {
					// error, no path found
					return Arrays.asList(TypeRef.var(core.tni));
				}
				
				// process the chosen path
				LookupPath path = paths.get(0);
				return Arrays.asList(path.returnedSubject().type);
			}
			
			@Override
			public List<TypeRef> visitBinOps1Expr(BinOps1ExprContext ctx) {
				return getTypesBinOp(scope, ctx.tnLhs, ctx.tnRhs, ctx.tnOp.getText());
			}
			
			@Override
			public List<TypeRef> visitBinOps2Expr(BinOps2ExprContext ctx) {
				return getTypesBinOp(scope, ctx.tnLhs, ctx.tnRhs, ctx.tnOp.getText());
			}
			
			@Override
			public List<TypeRef> visitBitOpsExpr(BitOpsExprContext ctx) {
				String op = ctx.tnOp.getText();
				if (ctx.tnOp2 != null) op += ctx.tnOp2.getText();
				
				return getTypesBinOp(scope, ctx.tnLhs, ctx.tnRhs, op);
			}
			
			@Override
			public List<TypeRef> visitRelOpsExpr(RelOpsExprContext ctx) {
				return getTypesBinOp(scope, ctx.tnLhs, ctx.tnRhs, ctx.tnOp.getText());
			}
			
			@Override
			public List<TypeRef> visitCastExpr(CastExprContext ctx) {
				return Arrays.asList(TyphonTypeResolver.readType(core.tni, ctx.tnRhs, scope));
			}
			
			@Override
			public List<TypeRef> visitParensExpr(ParensExprContext ctx) {
				return getExprType(scope, ctx.tnExpr, expectedTypes);
			}
			
			@Override
			public List<TypeRef> visitUnOpsExpr(UnOpsExprContext ctx) {
				CorePackage core = scope.getCodeBlock().tni.corePackage;
				
				AnnotationDefinition operator;
				
				switch (ctx.tnOp.getText()) {
				case "-":
					operator = core.LIB_OPS.ANNOT_NEG;
					break;
				case "+":
					operator = core.LIB_OPS.ANNOT_POS;
					break;
				case "~":
					operator = core.LIB_OPS.ANNOT_BNOT;
					break;
				case "!":
					return Arrays.asList(new TypeRef(core.TYPE_BOOL));
				default:
					throw new IllegalArgumentException("unknown operator in getUnOp: "+ctx.tnOp.getText());
				}
				
				
				List<Function> handlers = getExprType(scope, ctx.tnArg, Arrays.asList()).get(0).getType().getOperatorHandlers(operator);
				handlers.removeIf((f)->{
					if (f.getParams().size() != 0) {
						return true;
					}
					
					return false;
				});
				
				if (handlers.isEmpty()) {
					// error; handler not found
					return Arrays.asList(TypeRef.var(core.tni));
				}
				
				Function handler = handlers.get(0);
				return handler.getRetType();
			}
			
			@Override
			public List<TypeRef> visitCharConstExpr(CharConstExprContext ctx) {
				return Arrays.asList(new TypeRef(core.TYPE_CHAR));
			}
			
			@Override
			public List<TypeRef> visitStringConstExpr(StringConstExprContext ctx) {
				return Arrays.asList(new TypeRef(core.TYPE_STRING));
			}
			
			@Override
			public List<TypeRef> visitFalseConstExpr(FalseConstExprContext ctx) {
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
			}
			
			@Override
			public List<TypeRef> visitNullConstExpr(NullConstExprContext ctx) {
				return Arrays.asList(new TypeRef(core.TYPE_ANY));
			}
			
			@Override
			public List<TypeRef> visitThisConstExpr(ThisConstExprContext ctx) {
				return scope.getCodeBlock().instance == null ? Arrays.asList(new TypeRef(core.TYPE_ANY)) : Arrays.asList(scope.getCodeBlock().instance.type);
			}
			
			@Override
			public List<TypeRef> visitTrueConstExpr(TrueConstExprContext ctx) {
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
			}
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
	public static Variable compileLvalue(Scope scope, LvalueContext rule, List<Instruction> postfix) {
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
		TyphonBaseVisitor<Variable> visitor = new TyphonBaseVisitor<Variable>() {
			@Override
			public Variable visitVarLvalue(VarLvalueContext ctx) {
				MemberAccess access = scope;
				while (access != null) {
					List<MemberAccess> members = access.getMembers(ctx.tnName.getText(), scope.getCodeBlock().instance == null ? new HashMap<>() : TemplateUtils.matchAllTemplateArgs(scope.getCodeBlock().instance.type));
					
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
									return var;
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
				// turn the rule into a list of member accesses
				List<LookupElement> names = new ArrayList<>();
				ParserRuleContext lval = ctx;
				
				while (true) {
					if (lval instanceof MemberLvalueContext) {
						if (((MemberLvalueContext) lval).tnRhs instanceof VarLvalueContext) {
							names.add(0, new LookupElement(((MemberLvalueContext) lval).tnRhs.getText(), new SourceInfo(lval), AccessType.get(((MemberLvalueContext) lval).tnOp.getText())));
						} else {
							// TODO
						}
						
						names.addAll(0, ((MemberLvalueContext) lval).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), AccessType.get(e.tnOp.getText()), template);
						}).collect(Collectors.toList()));
						
						lval = ((MemberLvalueContext) lval).tnLhs;
					} else if (lval instanceof MemberExprContext) {
						names.add(0, new LookupElement(((MemberExprContext) lval).tnValue.getText(), new SourceInfo(lval), AccessType.get(((MemberExprContext) lval).tnOp.getText())));
						
						names.addAll(0, ((MemberExprContext) lval).tnLookup.stream().map((e)->{
							List<TemplateArgument> template = TyphonTypeResolver.readTemplateArgs(core.tni, e.tnTemplate.tnArgs, scope);
							return new LookupElement(e.tnName.getText(), new SourceInfo(e), AccessType.get(e.tnOp.getText()), template);
						}).collect(Collectors.toList()));
						
						lval = ((MemberExprContext) lval).tnLhs;
					} else if (lval instanceof VarExprContext) {
						names.add(0, new LookupElement(((VarExprContext) lval).tnValue.getText(), new SourceInfo(lval), null));
						
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
				
				List<LookupPath> paths = LookupUtils.findPaths(scope, base, names);
				paths.removeIf((path)->!(path.returnedSubject().member instanceof Field || path.returnedSubject().member instanceof Variable));
				
				if (paths.isEmpty()) {
					// error, no path found
					core.tni.errors.add(new UndefinedVariableError(new SourceInfo(ctx), ctx.tnRhs.getText()));
					return scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx));
				}
				LookupPath path = paths.get(0);
				
				// process the chosen path
				
				Subject sub;
				if (path.lastSubject().infix == AccessType.DOUBLE_DOT) {
					sub = path.lastSubject().previous;
				} else {
					sub = path.popSubject();
				}
				
				Variable instanceVar = LookupUtils.getSubjectOfPath(scope, path);
				
				if (sub.member instanceof Field) {
					Field f = (Field) sub.member;
					Type fieldOf = f.getFieldOf();
					Variable var = scope.addTempVar(f.type, new SourceInfo(ctx));
					
					Label label = null;
					if (sub.infix == AccessType.NULLABLE_DOT) {
						label = scope.addTempLabel();
							
						Variable tempVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), sub.source);
						scope.getCodeBlock().ops.add(new Instruction(core.tni, sub.source, OpCode.ISNULL, new Object[] {tempVar, instanceVar}));
						scope.getCodeBlock().ops.add(new Instruction(core.tni, sub.source, OpCode.JUMPIF, new Object[] {tempVar, label}));
					}
					
					if (f.getSetter() == null) {
						// error; field is read-only
						core.tni.errors.add(new ReadOnlyError(new SourceInfo(ctx), f));
						return var;
					}
					
					if (fieldOf == null) {
						postfix.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALLSTATIC, new Object[] {Arrays.asList(), f.getSetter(), Arrays.asList(var)}));
					} else {
						postfix.add(new Instruction(core.tni, new SourceInfo(rule), OpCode.CALL, new Object[] {Arrays.asList(), instanceVar, f.getSetter(), Arrays.asList(var)}));
					}
					
					if (label != null) {
						scope.getCodeBlock().ops.add(new Instruction(core.tni, sub.source, OpCode.LABEL, new Object[] {label}));
					}
					
					return var;
				}
				
				if (sub.member instanceof Variable) {
					return (Variable) sub.member;
				}
				
				throw new IllegalArgumentException("Unknown instance type in member lvalue");
			}
		};
		
		return visitor.visit(rule);
	}
	
	private static AnnotationDefinition getBinOp(CorePackage core, String s) {
		switch (s) {
		case "+":
			return core.LIB_OPS.ANNOT_ADD;
		case "-":
			return core.LIB_OPS.ANNOT_SUB;
		case "*":
			return core.LIB_OPS.ANNOT_MUL;
		case "/":
			return core.LIB_OPS.ANNOT_DIV;
		case "%":
			return core.LIB_OPS.ANNOT_MOD;
		case "&":
			return core.LIB_OPS.ANNOT_BAND;
		case "|":
			return core.LIB_OPS.ANNOT_BOR;
		case "^":
			return core.LIB_OPS.ANNOT_XOR;
		case "<<":
			return core.LIB_OPS.ANNOT_SHL;
		case ">>":
			return core.LIB_OPS.ANNOT_SHR;
		case "<":
			return core.LIB_OPS.ANNOT_LT;
		case "<=":
			return core.LIB_OPS.ANNOT_LE;
		case ">":
			return core.LIB_OPS.ANNOT_GT;
		case ">=":
			return core.LIB_OPS.ANNOT_GE;
		default:
			throw new IllegalArgumentException("unknown operator in getBinOp: "+s+"");
		}
	}
	
	private static List<TypeRef> getTypesBinOp(Scope scope, ExprContext lhsExpr, ExprContext rhsEpr, String opStr) {
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
		Variable lhs = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(lhsExpr));
		Variable rhs = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(rhsEpr));
		
		AnnotationDefinition operator = getBinOp(core, opStr);
		
		compileExpr(scope, lhsExpr, Arrays.asList(lhs));
		compileExpr(scope, rhsEpr, Arrays.asList(rhs));
		
		List<Function> handlers = lhs.type.getType().getOperatorHandlers(operator);
		handlers.removeIf((f)->{
			if (f.getParams().size() != 1) {
				return true;
			}
			
			return !rhs.type.canCastTo(f.getParams().get(0).getType());
		});
		
		if (handlers.isEmpty()) {
			// error; handler not found
			return Arrays.asList(TypeRef.var(core.tni));
		}
		
		Function handler = handlers.get(0);
		return handler.getRetType();
	}
	
	private static List<TypeRef> compileBinOp(Scope scope, ExprContext lhsExpr, ExprContext rhsExpr, String opStr, List<Variable> insertInto) {
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
		Variable lhs = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(lhsExpr));
		Variable rhs = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(rhsExpr));
		
		AnnotationDefinition operator = getBinOp(core, opStr);
		
		compileExpr(scope, lhsExpr, Arrays.asList(lhs));
		compileExpr(scope, rhsExpr, Arrays.asList(rhs));
		
		List<Function> handlers = lhs.type.getType().getOperatorHandlers(operator);
		handlers.removeIf((f)->{
			if (f.getParams().size() != 1) {
				return true;
			}
			
			return !rhs.type.canCastTo(f.getParams().get(0).getType());
		});
		
		if (handlers.isEmpty()) {
			// error; handler not found
			core.tni.errors.add(new UndefinedOperatorError(new SourceInfo(lhsExpr, rhsExpr), opStr, lhs.type, rhs.type));
			return Arrays.asList(TypeRef.var(core.tni));
		}
		
		Function handler = handlers.get(0);
		scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(lhsExpr, rhsExpr), OpCode.CALL, new Object[] {insertInto.isEmpty() ? Arrays.asList() : Arrays.asList(insertInto.get(0)), lhs, handler, Arrays.asList(rhs)}));
		
		return handler.getRetType();
	}
}
