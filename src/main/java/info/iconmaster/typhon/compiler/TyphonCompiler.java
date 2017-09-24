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
import info.iconmaster.typhon.antlr.TyphonParser.ArrayConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.AssignStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.BinOps1ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.BinOps2ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.BitOpsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.BlockContext;
import info.iconmaster.typhon.antlr.TyphonParser.BlockStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.BreakStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.CaseBlockContext;
import info.iconmaster.typhon.antlr.TyphonParser.CaseExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.CastExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.CharConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ContStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.DefStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.EqOpsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.FalseConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.FuncCallExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.IfStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.IsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.LogicOpsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.LvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.MapConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.MatchExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.MemberExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.MemberLvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.NewExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.NullCoalesceExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.NullConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.NumConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParamNameContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParensExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.RelOpsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.RepeatStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.RetStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.antlr.TyphonParser.StringConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.SwitchStatContext;
import info.iconmaster.typhon.antlr.TyphonParser.TerneryOpExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.ThisConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TrueConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeConstExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.UnOpsExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarExprContext;
import info.iconmaster.typhon.antlr.TyphonParser.VarLvalueContext;
import info.iconmaster.typhon.antlr.TyphonParser.WhileStatContext;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.errors.CannotConstructError;
import info.iconmaster.typhon.errors.ConstructorNotFoundError;
import info.iconmaster.typhon.errors.DuplicateVarNameError;
import info.iconmaster.typhon.errors.LabelNotFoundError;
import info.iconmaster.typhon.errors.NotAllowedHereError;
import info.iconmaster.typhon.errors.ReadOnlyError;
import info.iconmaster.typhon.errors.ReturnArgumentNumberError;
import info.iconmaster.typhon.errors.StringFormatError;
import info.iconmaster.typhon.errors.ThisInStaticContextError;
import info.iconmaster.typhon.errors.TypeError;
import info.iconmaster.typhon.errors.UndefinedOperatorError;
import info.iconmaster.typhon.errors.UndefinedVariableError;
import info.iconmaster.typhon.errors.WriteOnlyError;
import info.iconmaster.typhon.model.AnnotationDefinition;
import info.iconmaster.typhon.model.Constructor;
import info.iconmaster.typhon.model.Field;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.model.TyphonModelReader;
import info.iconmaster.typhon.model.libs.CorePackage;
import info.iconmaster.typhon.types.AnyType;
import info.iconmaster.typhon.types.SystemType;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.TyphonTypeResolver;
import info.iconmaster.typhon.types.UserType;
import info.iconmaster.typhon.util.LookupUtils;
import info.iconmaster.typhon.util.LookupUtils.FuncArgMap;
import info.iconmaster.typhon.util.LookupUtils.LookupArgument;
import info.iconmaster.typhon.util.LookupUtils.LookupElement;
import info.iconmaster.typhon.util.LookupUtils.LookupElement.AccessType;
import info.iconmaster.typhon.util.LookupUtils.LookupPath;
import info.iconmaster.typhon.util.LookupUtils.LookupPath.Subject;
import info.iconmaster.typhon.util.Option;
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
		
		if (f.getForm() == Function.Form.BLOCK) {
			// block form
			for (StatContext stat : (List<StatContext>) f.getRawCode()) {
				compileStat(scope, stat, f.getRetType());
			}
			
			// TODO: if we don't return void, check if all paths end in a RET
		} else {
			// expr form
			List<Variable> vars = new ArrayList<>();
			for (TypeRef retType : f.getRetType()) {
				vars.add(scope.addTempVar(retType, new SourceInfo((List) f.getRawCode())));
			}
			
			for (ExprContext expr : (List<ExprContext>) f.getRawCode()) {
				int used = compileExpr(scope, expr, vars);
				vars = vars.subList(used, vars.size());
			}
			
			block.ops.add(new Instruction(f.tni, new SourceInfo((List) f.getRawCode()), OpCode.RET, new Object[] {vars}));
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
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnExpr), OpCode.JUMPFALSE, new Object[] {condVar, endLabel}));
				
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
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx.tnExpr), OpCode.JUMPFALSE, new Object[] {condVar, beginLabel}));
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
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPFALSE, new Object[] {condVar, labels.get(0)}));
					
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
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPFALSE, new Object[] {condVar, labels.get(0)}));
					
					for (StatContext stat : block.tnBlock) {
						compileStat(scope, stat, expectedType);
					}
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {allEndLabel}));
					i++;
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
			
			@Override
			public Void visitRetStat(RetStatContext ctx) {
				List<Variable> retVars = new ArrayList<>();
				
				if (ctx.tnValues != null) {
					int i = 0;
					for (ExprContext expr : ctx.tnValues) {
						Variable var;
						
						if (i < expectedType.size()) {
							var = scope.addTempVar(expectedType.get(i), new SourceInfo(ctx));
						} else {
							var = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx));
						}
						
						retVars.add(var);
						compileExpr(scope, expr, Arrays.asList(var));
						i++;
					}
				}
				
				if (retVars.size() != expectedType.size()) {
					core.tni.errors.add(new ReturnArgumentNumberError(new SourceInfo(ctx), expectedType.size(), retVars.size()));
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.RET, new Object[] {retVars}));
				return null;
			}
			
			@Override
			public Void visitSwitchStat(SwitchStatContext ctx) {
				Scope newScope = new Scope(scope.getCodeBlock(), scope);
				if (ctx.tnLabel != null) {
					newScope.beginScopeLabel = newScope.addLabel(ctx.tnLabel.getText()+":begin");
					newScope.endScopeLabel = newScope.addLabel(ctx.tnLabel.getText()+":end");
				} else {
					newScope.beginScopeLabel = newScope.addTempLabel();
					newScope.endScopeLabel = newScope.addTempLabel();
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {newScope.beginScopeLabel}));
				
				Variable switchVar = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx.tnExpr));
				compileExpr(scope, ctx.tnExpr, Arrays.asList(switchVar));
				
				Label defaultLabel = newScope.addTempLabel();
				
				for (CaseBlockContext caze : ctx.tnCaseBlocks) {
					Scope caseScope = new Scope(scope.getCodeBlock(), newScope);
					if (caze.tnBlock.tnLabel != null) {
						caseScope.beginScopeLabel = caseScope.addLabel(caze.tnBlock.tnLabel.getText()+":begin");
					} else {
						caseScope.beginScopeLabel = caseScope.addTempLabel();
					}
					caseScope.endScopeLabel = newScope.endScopeLabel;
					Label caseLabel = caseScope.addTempLabel();
					
					for (ExprContext expr : caze.tnExprs) {
						Variable caseVar = caseScope.addTempVar(switchVar.type, new SourceInfo(expr));
						compileExpr(caseScope, expr, Arrays.asList(caseVar));
						
						Variable caseCondVar = caseScope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(expr));
						compileBinOp(caseScope, switchVar, caseVar, "==", Arrays.asList(caseCondVar), new SourceInfo(expr));
						
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPTRUE, new Object[] {caseCondVar, caseLabel}));
					}
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {caseScope.endScopeLabel}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {caseLabel}));
					
					for (StatContext stat : caze.tnBlock.tnBlock) {
						compileStat(caseScope, stat, expectedType);
					}
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {newScope.endScopeLabel}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {caseScope.beginScopeLabel}));
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {defaultLabel}));
				
				if (ctx.tnDefaultBlock != null) {
					Scope caseScope = new Scope(scope.getCodeBlock(), newScope);
					if (ctx.tnDefaultBlock.tnLabel != null) {
						caseScope.beginScopeLabel = caseScope.addLabel(ctx.tnDefaultBlock.tnLabel.getText()+":begin");
					} else {
						caseScope.beginScopeLabel = caseScope.addTempLabel();
					}
					caseScope.endScopeLabel = newScope.endScopeLabel;
					
					for (StatContext stat : ctx.tnDefaultBlock.tnBlock) {
						compileStat(caseScope, stat, expectedType);
					}
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {caseScope.beginScopeLabel}));
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {newScope.endScopeLabel}));
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
				
				Map<Variable, ExprContext> argMap = new HashMap<>();
				TyphonModelReader.readArgs(core.tni, ctx.tnArgs.tnArgs).stream().forEach((arg)->{
					Variable var = scope.addTempVar(TypeRef.var(core.tni), arg.source);
					vars.add(var);
					
					args.add(new LookupArgument(var, arg.getLabel()));
					
					argMap.put(var, arg.getRawValue());
				});
				
				paths.removeIf((path)->{
					MemberAccess member = path.members.get(path.members.size()-1);
					Map<TemplateType, TypeRef> typeMap = path.lastTypeMap();
					
					if (member instanceof Function) {
						Function f = (Function) member;
						
						if (!LookupUtils.areFuncArgsCompatibleWith(scope, f, args, typeMap, argMap)) {
							return true;
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
					return compileCall(scope, sub.source, new Option<>(sub, Option.IS_A), f, args, argMap, insertInto);
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
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPTRUE, new Object[] {label, var1}));
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
			
			@Override
			public List<TypeRef> visitLogicOpsExpr(LogicOpsExprContext ctx) {
				Label label = scope.addTempLabel();
				
				Variable lhs;
				if (insertInto.isEmpty()) {
					lhs = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(ctx));
				} else {
					lhs = insertInto.get(0);
					
					if (!new TypeRef(core.TYPE_BOOL).canCastTo(lhs.type)) {
						core.tni.errors.add(new TypeError(new SourceInfo(ctx.tnLhs), new TypeRef(core.TYPE_BOOL), lhs.type));
					}
				}
				
				TypeRef oldType = lhs.type; lhs.type = new TypeRef(core.TYPE_BOOL);
				compileExpr(scope, ctx.tnLhs, Arrays.asList(lhs));
				
				if (ctx.tnOp.getText().equals("&&")) {
					// and
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPFALSE, new Object[] {lhs, label}));
				} else {
					// or
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPTRUE, new Object[] {lhs, label}));
				}
				
				compileExpr(scope, ctx.tnRhs, Arrays.asList(lhs));
				lhs.type = oldType;
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {label}));
				
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
			}
			
			@Override
			public List<TypeRef> visitEqOpsExpr(EqOpsExprContext ctx) {
				String op = ctx.tnOp.getText();
				if (op.length() == 3) {
					// raw equality
					Variable lhs = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx.tnLhs));
					Variable rhs = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx.tnRhs));
					
					compileExpr(scope, ctx.tnLhs, Arrays.asList(lhs));
					compileExpr(scope, ctx.tnRhs, Arrays.asList(rhs));
					
					if (!insertInto.isEmpty()) {
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.RAWEQ, new Object[] {insertInto.get(0), lhs, rhs}));
						
						if (op.equals("!==")) {
							scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.NOT, new Object[] {insertInto.get(0), insertInto.get(0)}));
						}
					}
					
					return Arrays.asList(new TypeRef(core.TYPE_BOOL));
				} else {
					// binary op
					List<TypeRef> result = compileBinOp(scope, ctx.tnLhs, ctx.tnRhs, "==", insertInto);
					
					if (result.size() != 1 || result.get(0).getType() != core.TYPE_BOOL) {
						// error; signature of == incorrect
						core.tni.errors.add(new UndefinedOperatorError(new SourceInfo(ctx), op, getExprType(scope, ctx.tnLhs, Arrays.asList()).get(0), getExprType(scope, ctx.tnRhs, Arrays.asList()).get(0)));
						return result;
					}
					
					if (op.equals("!=") && !insertInto.isEmpty()) {
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.NOT, new Object[] {insertInto.get(0), insertInto.get(0)}));
					}
					
					return result;
				}
			}
			
			@Override
			public List<TypeRef> visitIsExpr(IsExprContext ctx) {
				Variable lhs = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx.tnLhs));
				compileExpr(scope, ctx.tnLhs, Arrays.asList(lhs));
				
				TypeRef rhs = TyphonTypeResolver.readType(core.tni, ctx.tnRhs, scope);
				
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.INSTANCEOF, new Object[] {insertInto.get(0), lhs, rhs}));
					
					if (ctx.tnOp.equals("!")) {
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.NOT, new Object[] {insertInto.get(0), insertInto.get(0)}));
					}
				}
				
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
			}
			
			@Override
			public List<TypeRef> visitNullCoalesceExpr(NullCoalesceExprContext ctx) {
				Variable lhs;
				if (insertInto.isEmpty()) {
					lhs = scope.addTempVar(new TypeRef(core.TYPE_ANY), new SourceInfo(ctx.tnLhs));
				} else {
					lhs = insertInto.get(0);
				}
				
				TypeRef common = getExprType(scope, ctx, Arrays.asList(lhs.type)).get(0);
				
				TypeRef oldType = lhs.type; lhs.type = common;
				compileExpr(scope, ctx.tnLhs, Arrays.asList(lhs));
				lhs.type = oldType;
				
				Label label = scope.addTempLabel();
				Variable tempVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(ctx));
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.ISNULL, new Object[] {tempVar, lhs}));
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPFALSE, new Object[] {tempVar, label}));
				
				oldType = lhs.type; lhs.type = common;
				compileExpr(scope, ctx.tnRhs, Arrays.asList(lhs));
				lhs.type = oldType;
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {label}));
				
				return Arrays.asList(common);
			}
			
			@Override
			public List<TypeRef> visitParensExpr(ParensExprContext ctx) {
				return visit(ctx.tnExpr);
			}
			
			@Override
			public List<TypeRef> visitTerneryOpExpr(TerneryOpExprContext ctx) {
				TypeRef common = getExprType(scope, ctx.tnThen, Arrays.asList(new TypeRef(core.TYPE_ANY))).get(0);
				
				Label allEndLabel = scope.addTempLabel();
				List<Label> labels = new ArrayList<>();
				
				Variable out;
				if (insertInto.isEmpty()) {
					out = scope.addTempVar(new TypeRef(core.TYPE_ANY), new SourceInfo(ctx));
				} else {
					out = insertInto.get(0);
				}
				
				// add all the labels in advance
				
				for (ExprContext expr : ctx.tnElseIf) {
					labels.add(scope.addTempLabel());
				}
				
				if (ctx.tnElse != null) {
					labels.add(scope.addTempLabel());
				}
				
				labels.add(allEndLabel);
				
				// parse the 'if' block
				{
					Variable condVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(ctx.tnIf));
					compileExpr(scope, ctx.tnIf, Arrays.asList(condVar));
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPFALSE, new Object[] {condVar, labels.get(0)}));
					
					TypeRef old = out.type; out.type = TypeRef.var(core.tni);
					compileExpr(scope, ctx.tnThen, Arrays.asList(out));
					out.type = old;
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {allEndLabel}));
				}
				
				// parse the 'elseif' blocks
				int i = 0;
				for (ExprContext cond : ctx.tnElseIf) {
					ExprContext expr = ctx.tnElseThen.get(i);
					Label label = labels.remove(0);
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {label}));
					
					Variable condVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(cond));
					compileExpr(scope, cond, Arrays.asList(condVar));
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPFALSE, new Object[] {condVar, labels.get(0)}));
					
					TypeRef old = out.type; out.type = TypeRef.var(core.tni);
					compileExpr(scope, expr, Arrays.asList(out));
					common = common.commonType(out.type); out.type = old;
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {allEndLabel}));
					i++;
				}
				
				// parse the 'else' block
				if (ctx.tnElse != null) {
					Label label = labels.remove(0);
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {label}));
					
					TypeRef old = out.type; out.type = TypeRef.var(core.tni);
					compileExpr(scope, ctx.tnElse, Arrays.asList(out));
					common = common.commonType(out.type); out.type = old;
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {allEndLabel}));
				
				return Arrays.asList(common);
			}
			
			@Override
			public List<TypeRef> visitArrayConstExpr(ArrayConstExprContext ctx) {
				TypeRef common = null;
				List<Variable> vars = new ArrayList<>();
				
				// try to infer what the element type should be.
				TypeRef elemType = new TypeRef(core.TYPE_ANY);
				if (!insertInto.isEmpty() && insertInto.get(0).type.getType() == core.TYPE_LIST) {
					TemplateType tempType = core.TYPE_LIST.getTemplates().get(0);
					Map<TemplateType, TypeRef> elemTypeMap = TemplateUtils.matchTemplateArgs(insertInto.get(0).type);
					
					if (elemTypeMap.containsKey(tempType)) {
						elemType = elemTypeMap.get(tempType);
					}
				}
				
				// compile the elements.
				for (ExprContext expr : ctx.tnValues) {
					Variable var = scope.addTempVar(TypeRef.var(elemType), new SourceInfo(expr));
					compileExpr(scope, expr, Arrays.asList(var));
					
					if (common == null) {
						common = var.type;
					} else {
						common = common.commonType(var.type);
					}
					
					vars.add(var);
				}
				
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVLIST, new Object[] {insertInto.get(0), vars}));
				}
				
				// if the list is empty, infer the type.
				if (common == null) {
					common = elemType;
				}
				
				// return the list type
				return Arrays.asList(new TypeRef(core.TYPE_LIST, new TemplateArgument(common)));
			}
			
			@Override
			public List<TypeRef> visitMapConstExpr(MapConstExprContext ctx) {
				TypeRef commonKey = null;
				TypeRef commonValue = null;
				
				Map<Variable, Variable> vars = new HashMap<>();
				
				// try to infer what the element types should be.
				TypeRef keyType = new TypeRef(core.TYPE_ANY);
				TypeRef valueType = new TypeRef(core.TYPE_ANY);
				
				if (!insertInto.isEmpty() && insertInto.get(0).type.getType() == core.TYPE_MAP) {
					TemplateType keyTempType = core.TYPE_MAP.getTemplates().get(0);
					TemplateType valueTempType = core.TYPE_MAP.getTemplates().get(1);
					
					Map<TemplateType, TypeRef> elemTypeMap = TemplateUtils.matchTemplateArgs(insertInto.get(0).type);
					
					if (elemTypeMap.containsKey(keyTempType)) {
						keyType = elemTypeMap.get(keyTempType);
					}
					if (elemTypeMap.containsKey(valueTempType)) {
						valueType = elemTypeMap.get(valueTempType);
					}
				}
				
				// compile the elements.
				int i = 0;
				for (ExprContext value : ctx.tnValues) {
					ExprContext key = ctx.tnKeys.get(i);
					
					Variable keyVar = scope.addTempVar(TypeRef.var(keyType), new SourceInfo(key));
					compileExpr(scope, key, Arrays.asList(keyVar));
					
					if (commonKey == null) {
						commonKey = keyVar.type;
					} else {
						commonKey = commonKey.commonType(keyVar.type);
					}
					
					Variable valueVar = scope.addTempVar(TypeRef.var(valueType), new SourceInfo(value));
					compileExpr(scope, value, Arrays.asList(valueVar));
					
					if (commonValue == null) {
						commonValue = valueVar.type;
					} else {
						commonValue = commonValue.commonType(valueVar.type);
					}
					
					vars.put(keyVar, valueVar);
					i++;
				}
				
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVMAP, new Object[] {insertInto.get(0), vars}));
				}
				
				// if the map is empty, infer the type
				if (commonKey == null) {
					commonKey = keyType;
					commonValue = valueType;
				}
				
				// return the map type
				return Arrays.asList(new TypeRef(core.TYPE_MAP, new TemplateArgument(commonKey), new TemplateArgument(commonValue)));
			}
			
			@Override
			public List<TypeRef> visitMatchExpr(MatchExprContext ctx) {
				Variable out;
				if (insertInto.isEmpty()) {
					out = scope.addTempVar(new TypeRef(core.TYPE_ANY), new SourceInfo(ctx));
				} else {
					out = insertInto.get(0);
				}
				
				TypeRef common = getExprType(scope, ctx.tnDefault, Arrays.asList(out.type)).get(0);
				
				Scope newScope = new Scope(scope.getCodeBlock(), scope);
				newScope.beginScopeLabel = newScope.addTempLabel();
				newScope.endScopeLabel = newScope.addTempLabel();
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {newScope.beginScopeLabel}));
				
				Variable switchVar = scope.addTempVar(TypeRef.var(core.tni), new SourceInfo(ctx.tnMatch));
				compileExpr(scope, ctx.tnMatch, Arrays.asList(switchVar));
				
				Label defaultLabel = newScope.addTempLabel();
				
				for (CaseExprContext caze : ctx.tnCases) {
					Scope caseScope = new Scope(scope.getCodeBlock(), newScope);
					caseScope.beginScopeLabel = caseScope.addTempLabel();
					caseScope.endScopeLabel = newScope.endScopeLabel;
					Label caseLabel = caseScope.addTempLabel();
					
					for (ExprContext expr : caze.tnIf) {
						Variable caseVar = caseScope.addTempVar(switchVar.type, new SourceInfo(expr));
						compileExpr(caseScope, expr, Arrays.asList(caseVar));
						
						Variable caseCondVar = caseScope.addTempVar(new TypeRef(core.TYPE_BOOL), new SourceInfo(expr));
						compileBinOp(caseScope, switchVar, caseVar, "==", Arrays.asList(caseCondVar), new SourceInfo(expr));
						
						scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMPTRUE, new Object[] {caseCondVar, caseLabel}));
					}
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {caseScope.endScopeLabel}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {caseLabel}));
					
					TypeRef old = out.type; out.type = TypeRef.var(core.tni);
					compileExpr(scope, caze.tnThen, insertInto);
					common = common.commonType(out.type); out.type = old;
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.JUMP, new Object[] {newScope.endScopeLabel}));
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {caseScope.beginScopeLabel}));
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {defaultLabel}));
				
				if (ctx.tnDefault != null) {
					Scope caseScope = new Scope(scope.getCodeBlock(), newScope);
					caseScope.beginScopeLabel = caseScope.addTempLabel();
					caseScope.endScopeLabel = newScope.endScopeLabel;
					
					TypeRef old = out.type; out.type = TypeRef.var(core.tni);
					compileExpr(scope, ctx.tnDefault, insertInto);
					common = common.commonType(out.type); out.type = old;
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {caseScope.beginScopeLabel}));
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.LABEL, new Object[] {newScope.endScopeLabel}));
				return Arrays.asList(common);
			}
			
			@Override
			public List<TypeRef> visitTypeConstExpr(TypeConstExprContext ctx) {
				TypeRef type = TyphonTypeResolver.readType(core.tni, ctx.tnType, scope);
				
				if (!insertInto.isEmpty()) {
					scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.MOVTYPE, new Object[] {insertInto.get(0), type}));
				}
				
				return Arrays.asList(new TypeRef(core.LIB_REFLECT.TYPE_TYPE));
			}
			
			@Override
			public List<TypeRef> visitNewExpr(NewExprContext ctx) {
				TypeRef type = TyphonTypeResolver.readType(core.tni, ctx.tnType, scope);
				
				if (!(type.getType() instanceof UserType || type.getType() instanceof SystemType || type.getType() instanceof AnyType)) {
					// error; cannot construct
					core.tni.errors.add(new CannotConstructError(new SourceInfo(ctx), type));
					return Arrays.asList(type);
				}
				
				List<LookupArgument> args = new ArrayList<>();
				List<Variable> vars = new ArrayList<>();
				
				Map<Variable, ExprContext> argMap = new HashMap<>();
				TyphonModelReader.readArgs(core.tni, ctx.tnArgs.tnArgs).stream().forEach((arg)->{
					Variable var = scope.addTempVar(TypeRef.var(core.tni), arg.source);
					vars.add(var);
					
					args.add(new LookupArgument(var, arg.getLabel()));
					
					argMap.put(var, arg.getRawValue());
				});
				
				List<Function> constructors = type.getType().getTypePackage().getFunctions().stream().filter((f)->f instanceof Constructor && f.getFieldOf() == type.getType()).filter(f->{
					if (!LookupUtils.areFuncArgsCompatibleWith(scope, f, args, type.getTemplateMap(new HashMap<>()), argMap)) {
						return false;
					}
					
					return true;
				}).collect(Collectors.toList());
				
				if (constructors.isEmpty()) {
					// error, no constructor found
					core.tni.errors.add(new ConstructorNotFoundError(new SourceInfo(ctx), type));
					return Arrays.asList(type);
				}
				
				Constructor f = (Constructor) constructors.get(0);
				
				Variable out;
				if (insertInto.isEmpty()) {
					out = scope.addTempVar(type, new SourceInfo(ctx));
				} else {
					out = insertInto.get(0);
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, new SourceInfo(ctx), OpCode.ALLOC, new Object[] {out, type}));
				
				compileCall(scope, new SourceInfo(ctx), new Option<>(out, Option.IS_B), f, args, argMap, Arrays.asList());
				
				return Arrays.asList(type);
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
				return visit(ctx.tnExpr);
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
			
			@Override
			public List<TypeRef> visitLogicOpsExpr(LogicOpsExprContext ctx) {
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
			}
			
			@Override
			public List<TypeRef> visitEqOpsExpr(EqOpsExprContext ctx) {
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
			}
			
			@Override
			public List<TypeRef> visitIsExpr(IsExprContext ctx) {
				return Arrays.asList(new TypeRef(core.TYPE_BOOL));
			}
			
			@Override
			public List<TypeRef> visitNullCoalesceExpr(NullCoalesceExprContext ctx) {
				TypeRef lhs = visit(ctx.tnLhs).get(0);
				TypeRef rhs = visit(ctx.tnRhs).get(0);
				return Arrays.asList(lhs.commonType(rhs));
			}
			
			@Override
			public List<TypeRef> visitArrayConstExpr(ArrayConstExprContext ctx) {
				TypeRef common = null;
				
				// try to infer what the element type should be.
				TypeRef elemType = new TypeRef(core.TYPE_ANY);
				if (!expectedTypes.isEmpty() && expectedTypes.get(0).getType() == core.TYPE_LIST) {
					TemplateType tempType = core.TYPE_LIST.getTemplates().get(0);
					Map<TemplateType, TypeRef> elemTypeMap = TemplateUtils.matchTemplateArgs(expectedTypes.get(0));
					
					if (elemTypeMap.containsKey(tempType)) {
						elemType = elemTypeMap.get(tempType);
					}
				}
				
				// compile the elements.
				for (ExprContext expr : ctx.tnValues) {
					TypeRef type = getExprType(scope, expr, Arrays.asList(elemType)).get(0);
					
					if (common == null) {
						common = type;
					} else {
						common = common.commonType(type);
					}
				}
				
				// if the list is empty, infer the type.
				if (common == null) {
					common = elemType;
				}
				
				// return the list type
				return Arrays.asList(new TypeRef(core.TYPE_LIST, new TemplateArgument(common)));
			}
			
			@Override
			public List<TypeRef> visitMapConstExpr(MapConstExprContext ctx) {
				TypeRef commonKey = null;
				TypeRef commonValue = null;
				
				// try to infer what the element types should be.
				TypeRef keyType = new TypeRef(core.TYPE_ANY);
				TypeRef valueType = new TypeRef(core.TYPE_ANY);
				
				if (!expectedTypes.isEmpty() && expectedTypes.get(0).getType() == core.TYPE_MAP) {
					TemplateType keyTempType = core.TYPE_MAP.getTemplates().get(0);
					TemplateType valueTempType = core.TYPE_MAP.getTemplates().get(1);
					
					Map<TemplateType, TypeRef> elemTypeMap = TemplateUtils.matchTemplateArgs(expectedTypes.get(0));
					
					if (elemTypeMap.containsKey(keyTempType)) {
						keyType = elemTypeMap.get(keyTempType);
					}
					if (elemTypeMap.containsKey(valueTempType)) {
						valueType = elemTypeMap.get(valueTempType);
					}
				}
				
				// compile the elements.
				int i = 0;
				for (ExprContext value : ctx.tnValues) {
					ExprContext key = ctx.tnKeys.get(i);
					
					TypeRef thisKeyType = getExprType(scope, key, Arrays.asList(keyType)).get(0);
					
					if (commonKey == null) {
						commonKey = thisKeyType;
					} else {
						commonKey = commonKey.commonType(thisKeyType);
					}
					
					TypeRef thisValueType = getExprType(scope, value, Arrays.asList(valueType)).get(0);
					
					if (commonValue == null) {
						commonValue = thisValueType;
					} else {
						commonValue = commonValue.commonType(thisValueType);
					}
					
					i++;
				}
				
				// if the map is empty, infer the type
				if (commonKey == null) {
					commonKey = keyType;
					commonValue = valueType;
				}
				
				// return the map type
				return Arrays.asList(new TypeRef(core.TYPE_MAP, new TemplateArgument(commonKey), new TemplateArgument(commonValue)));
			}
			
			@Override
			public List<TypeRef> visitMatchExpr(MatchExprContext ctx) {
				TypeRef common = getExprType(scope, ctx.tnDefault, expectedTypes).get(0);
				
				for (CaseExprContext caze : ctx.tnCases) {
					common = common.commonType(getExprType(scope, caze.tnThen, expectedTypes).get(0));
				}
				
				return Arrays.asList(common);
			}
			
			@Override
			public List<TypeRef> visitTypeConstExpr(TypeConstExprContext ctx) {
				return Arrays.asList(new TypeRef(core.LIB_REFLECT.TYPE_TYPE));
			}
			
			@Override
			public List<TypeRef> visitNewExpr(NewExprContext ctx) {
				return Arrays.asList(TyphonTypeResolver.readType(core.tni, ctx.tnType, scope));
			}
		};
		
		List<TypeRef> result = visitor.visit(rule);
		return result == null ? Arrays.asList(new TypeRef(core.TYPE_ANY)) : result;
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
						scope.getCodeBlock().ops.add(new Instruction(core.tni, sub.source, OpCode.JUMPTRUE, new Object[] {tempVar, label}));
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
		case "==":
			return core.LIB_OPS.ANNOT_EQ;
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
		
		compileExpr(scope, lhsExpr, Arrays.asList(lhs));
		compileExpr(scope, rhsExpr, Arrays.asList(rhs));
		
		return compileBinOp(scope, lhs, rhs, opStr, insertInto, new SourceInfo(lhsExpr, rhsExpr));
	}
	
	private static List<TypeRef> compileBinOp(Scope scope, Variable lhs, Variable rhs, String opStr, List<Variable> insertInto, SourceInfo source) {
		CorePackage core = scope.getCodeBlock().tni.corePackage;
		
		AnnotationDefinition operator = getBinOp(core, opStr);
		
		List<Function> handlers = lhs.type.getType().getOperatorHandlers(operator);
		handlers.removeIf((f)->{
			if (f.getParams().size() != 1) {
				return true;
			}
			
			return !rhs.type.canCastTo(f.getParams().get(0).getType());
		});
		
		if (handlers.isEmpty()) {
			// error; handler not found
			core.tni.errors.add(new UndefinedOperatorError(source, opStr, lhs.type, rhs.type));
			return Arrays.asList(TypeRef.var(core.tni));
		}
		
		Function handler = handlers.get(0);
		scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.CALL, new Object[] {insertInto.isEmpty() ? Arrays.asList() : Arrays.asList(insertInto.get(0)), lhs, handler, Arrays.asList(rhs)}));
		
		return handler.getRetType();
	}
	
	private static List<TypeRef> compileCall(Scope scope, SourceInfo source, Option<Subject, Variable> subjectOrInstVar, Function f, List<LookupArgument> args, Map<Variable, ExprContext> argMap, List<Variable> insertInto) {
		if (argMap == null) argMap = new HashMap<>();
		
		Variable instanceVar = null;
		Subject sub = null;
		
		if (subjectOrInstVar != null) {
			if (subjectOrInstVar.isA()) {
				sub = subjectOrInstVar.getA();
			} else {
				instanceVar = subjectOrInstVar.getB();
			}
		}
		
		CorePackage core = f.tni.corePackage;
		Type fieldOf = f.getFieldOf();
		
		List<Variable> outputVars = new ArrayList<>(insertInto.subList(0, Math.min(f.getRetType().size(), insertInto.size())));
		List<Variable> inputVars = new ArrayList<>();
		
		FuncArgMap map = LookupUtils.getFuncArgMap(f, args);
		
		for (Parameter param : f.getParams()) {
			if (map.args.containsKey(param)) {
				inputVars.add(map.args.get(param));
				map.args.get(param).type = argMap.containsKey(map.args.get(param)) ? getExprType(scope, argMap.get(map.args.get(param)), Arrays.asList(param.getType())).get(0) : map.args.get(param).type;
				
				if (argMap.containsKey(map.args.get(param)))
					compileExpr(scope, argMap.get(map.args.get(param)), Arrays.asList(map.args.get(param)));
			} else if (map.varargs.containsKey(param)) {
				Variable listVar = scope.addTempVar(param.getType(), source);
				inputVars.add(listVar);
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.MOVLIST, new Object[] {listVar, map.varargs.get(param)}));
			} else if (map.varflags.containsKey(param)) {
				Variable mapVar = scope.addTempVar(param.getType(), source);
				inputVars.add(mapVar);
				
				Map<Variable, Variable> varmap = new HashMap<>();
				for (Entry<String, Variable> entry : map.varflags.get(param).entrySet()) {
					Variable strVar = scope.addTempVar(param.getType(), source);
					varmap.put(strVar, entry.getValue());
					
					scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.MOVSTR, new Object[] {strVar, entry.getKey()}));
				}
				
				scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.MOVMAP, new Object[] {mapVar, varmap}));
			} else {
				// TODO: assign default value to variable
				Variable var = scope.addTempVar(param.getType(), source);
				inputVars.add(var);
			}
		}
		
		if (fieldOf == null) {
			// CALLSTATIC
			if (sub != null && (sub.infix == AccessType.NULLABLE_DOT || sub.infix == AccessType.DOUBLE_DOT)) {
				// error; dots only apply in non-static context
				core.tni.errors.add(new NotAllowedHereError(source, "special dot operators"));
			}
			
			scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.CALLSTATIC, new Object[] {outputVars, f, inputVars}));
		} else {
			// CALL
			if (instanceVar == null) {
				instanceVar = LookupUtils.getSubjectOfPath(scope, sub.path);
			}
			
			Label label = null;
			if (sub != null && sub.infix == AccessType.NULLABLE_DOT) {
				label = scope.addTempLabel();
				
				Variable tempVar = scope.addTempVar(new TypeRef(core.TYPE_BOOL), source);
				scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.ISNULL, new Object[] {tempVar, instanceVar}));
				scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.JUMPTRUE, new Object[] {tempVar, label}));
			}
			
			if (sub != null && sub.infix == AccessType.DOUBLE_DOT) {
				outputVars = Arrays.asList();
			}
			
			scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.CALL, new Object[] {outputVars, instanceVar, f, inputVars}));
			
			if (sub != null && sub.infix == AccessType.DOUBLE_DOT && !insertInto.isEmpty()) {
				scope.getCodeBlock().ops.add(new Instruction(core.tni, source, OpCode.MOV, new Object[] {instanceVar, insertInto.get(0)}));
			}
			
			if (label != null) {
				scope.getCodeBlock().ops.add(new Instruction(scope.getCodeBlock().tni, source, OpCode.LABEL, new Object[] {label}));
			}
			
			if (sub != null && sub.infix == AccessType.DOUBLE_DOT) {
				return Arrays.asList(instanceVar.type);
			}
		}
		
		// calculate the function's return type
		List<TypeRef> params = f.getParams().stream().filter(p->map.args.containsKey(p)).map(p->p.getType()).collect(Collectors.toList());
		List<TypeRef> args2 = f.getParams().stream().filter(p->map.args.containsKey(p)).map(p->map.args.get(p).type).collect(Collectors.toList());
		
		Map<TemplateType, TypeRef> funcTempMap = TemplateUtils.inferTemplatesFromArguments(core.tni, params, args2, f.getFuncTemplateMap());
		
		Subject finalSub = sub;
		List<TypeRef> retType = f.getRetType().subList(0, outputVars.size()).stream().map(t->TemplateUtils.replaceTemplates(TemplateUtils.replaceTemplates(t, funcTempMap), finalSub == null ? new HashMap<>() : finalSub.typeMap)).collect(Collectors.toList());
		return retType;
	}
}
