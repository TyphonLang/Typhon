package info.iconmaster.typhon.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonBaseVisitor;
import info.iconmaster.typhon.antlr.TyphonLexer;
import info.iconmaster.typhon.antlr.TyphonParser;
import info.iconmaster.typhon.antlr.TyphonParser.AnnotationContext;
import info.iconmaster.typhon.antlr.TyphonParser.ArgDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.ClassDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.ConstructorDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.ConstructorParamContext;
import info.iconmaster.typhon.antlr.TyphonParser.ConstructorParamThisContext;
import info.iconmaster.typhon.antlr.TyphonParser.ConstructorParamTypedContext;
import info.iconmaster.typhon.antlr.TyphonParser.DeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.EnumDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.EnumValueDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.FieldDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.GlobalAnnotDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.GlobalAnnotationContext;
import info.iconmaster.typhon.antlr.TyphonParser.ImportDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.MethodDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.MultiTypesContext;
import info.iconmaster.typhon.antlr.TyphonParser.PackageDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParamDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParamNameContext;
import info.iconmaster.typhon.antlr.TyphonParser.RootContext;
import info.iconmaster.typhon.antlr.TyphonParser.SimplePackageDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.SingleTypesContext;
import info.iconmaster.typhon.antlr.TyphonParser.StaticInitDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.TemplateDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypesContext;
import info.iconmaster.typhon.antlr.TyphonParser.VoidTypesContext;
import info.iconmaster.typhon.errors.SyntaxError;
import info.iconmaster.typhon.model.Constructor.ConstructorParameter;
import info.iconmaster.typhon.model.Import.PackageImport;
import info.iconmaster.typhon.model.Import.RawImport;
import info.iconmaster.typhon.plugins.PluginLoader;
import info.iconmaster.typhon.plugins.TyphonPlugin;
import info.iconmaster.typhon.types.EnumType;
import info.iconmaster.typhon.types.EnumType.EnumChoice;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.UserType;
import info.iconmaster.typhon.util.Box;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This class handles the translation of declarations into the respective language entities.
 * Use this as the first step for translating Typhon text into Typhon code.
 * 
 * @author iconmaster
 *
 */
public class TyphonModelReader {
	private TyphonModelReader() {}
	
	/**
	 * Reads a source file, and translates it into a Typhon package.
	 * 
	 * @param tni
	 * @param file
	 * @return The package the source file encodes.
	 * @throws IOException If the file cannot be read.
	 */
	public static Package parseFile(TyphonInput tni, File file) throws IOException {
		try {
			TyphonLexer lexer = new TyphonLexer(new ANTLRFileStream(file.getPath()));
			TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
			
			parser.setErrorHandler(new BailErrorStrategy());
			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					SourceInfo source;
					if (offendingSymbol instanceof Token) {
						Token token = ((Token)offendingSymbol);
						source = new SourceInfo(token);
					} else {
						source = new SourceInfo(file.getPath(), -1, -1);
					}
					
					tni.errors.add(new SyntaxError(source, msg));
				}
			});
			
			RootContext root = parser.root();
			Package p = new Package(new SourceInfo(root), null, tni.corePackage);
			p.setRawData();
			return readPackage(p, root.tnDecls);
		} catch (ParseCancellationException e) {
			return new Package(new SourceInfo(file.getPath(), 0, (int) file.length()-1), null, tni.corePackage);
		}
	}
	
	/**
	 * Reads a source string, and translates it into a Typhon package.
	 * 
	 * @param tni
	 * @param input
	 * @return The package the input encodes.
	 */
	public static Package parseString(TyphonInput tni, String input) {
		TyphonLexer lexer = new TyphonLexer(new ANTLRInputStream(input));
		TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
		
		parser.setErrorHandler(new BailErrorStrategy());
		parser.removeErrorListeners();
		parser.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
				SourceInfo source;
				if (offendingSymbol instanceof Token) {
					Token token = ((Token)offendingSymbol);
					source = new SourceInfo(token);
				} else {
					source = new SourceInfo(-1, -1);
				}
				
				tni.errors.add(new SyntaxError(source, msg));
			}
		});
		
		try {
			Package p = new Package(new SourceInfo(0, input.length()-1), null, tni.corePackage);
			p.setRawData();
			return readPackage(p, parser.root().tnDecls);
		} catch (ParseCancellationException e) {
			return new Package(new SourceInfo(0, input.length()-1), null, tni.corePackage);
		}
	}
	
	/**
	 * Adds declarations to a Typhon package based on ANTLR input.
	 * 
	 * @param result The package we want to populate with declarations.
	 * @param decls The ANTLR rules for the declarations in this package.
	 * @return The package representing the ANTLR rules given as input.
	 */
	public static Package readPackage(Package result, List<DeclContext> decls) {
		Box<Integer> declIndex = new Box<>(0);
		Box<Boolean> doneVisiting = new Box<>(false);
		ArrayList<Annotation> globalAnnots = new ArrayList<>();
		TyphonBaseVisitor<Void> visitor = new TyphonBaseVisitor<Void>() {
			@Override
			public Void visitPackageDecl(PackageDeclContext decl) {
				List<String> names = decl.tnName.tnName.stream().map((name)->name.getText()).collect(Collectors.toCollection(()->new ArrayList<>()));
				String lastName = names.remove(names.size()-1);
				Package base = result;
				for (String name : names) {
					base = readPackage(new Package(new SourceInfo(decl), name, base), new ArrayList<>());
					base.setRawData();
				}
				
				Package p = readPackage(new Package(new SourceInfo(decl), lastName, base), decl.tnDecls);
				p.setRawData();
				p.getAnnots().addAll(readAnnots(result.tni, decl.tnAnnots));
				p.getAnnots().addAll(globalAnnots);
				return null;
			}
			
			@Override
			public Void visitSimplePackageDecl(SimplePackageDeclContext decl) {
				List<String> names = decl.tnName.tnName.stream().map((name)->name.getText()).collect(Collectors.toCollection(()->new ArrayList<>()));
				String lastName = names.remove(names.size()-1);
				Package base = result;
				for (String name : names) {
					base = readPackage(new Package(new SourceInfo(decl), name, base), new ArrayList<>());
					base.setRawData();
				}
				
				List<DeclContext> remainingDecls = decls.subList(declIndex.data+1, decls.size());
				Package p = readPackage(new Package(new SourceInfo(decl), lastName, base), remainingDecls);
				p.setRawData();
				p.getAnnots().addAll(readAnnots(result.tni, decl.tnAnnots));
				p.getAnnots().addAll(globalAnnots);
				doneVisiting.data = true;
				return null;
			}
			
			@Override
			public Void visitMethodDecl(MethodDeclContext ctx) {
				Function f = readFunction(result.tni, ctx);
				result.addFunction(f);
				f.getAnnots().addAll(globalAnnots);
				return null;
			}
			
			@Override
			public Void visitFieldDecl(FieldDeclContext ctx) {
				for (Field f : readField(result.tni, ctx)) {
					result.addField(f);
					f.getAnnots().addAll(globalAnnots);
				}
				
				return null;
			}
			
			@Override
			public Void visitImportDecl(ImportDeclContext ctx) {
				Import i = readImport(result.tni, ctx);
				result.addImport(i);
				i.getAnnots().addAll(globalAnnots);
				return null;
			}
			
			@Override
			public Void visitStaticInitDecl(StaticInitDeclContext ctx) {
				StaticInitBlock b = readStaticInitBlock(result.tni, ctx);
				result.addStaticInitBlock(b);
				b.getAnnots().addAll(globalAnnots);
				return null;
			}
			
			@Override
			public Void visitClassDecl(ClassDeclContext ctx) {
				UserType t = readClass(result.tni, ctx);
				result.addType(t);
				t.getAnnots().addAll(globalAnnots);
				return null;
			}
			
			@Override
			public Void visitConstructorDecl(ConstructorDeclContext ctx) {
				Constructor f = readConstructor(result.tni, ctx);
				result.addFunction(f);
				f.getAnnots().addAll(globalAnnots);
				return null;
			}
			
			@Override
			public Void visitGlobalAnnotDecl(GlobalAnnotDeclContext ctx) {
				Annotation a = readGlobalAnnot(result.tni, ctx.tnGlobalAnnot);
				// TODO: Should global annotations be annotatable? If so, how can normal annotations be annotated?
				// a.getAnnots().addAll(readAnnots(result.tni, ctx.tnAnnots));
				globalAnnots.add(a);
				return null;
			}
			
			@Override
			public Void visitEnumDecl(EnumDeclContext ctx) {
				EnumType t = readEnum(result.tni, ctx);
				result.addType(t);
				t.getAnnots().addAll(globalAnnots);
				return null;
			}
		};
		
		for (DeclContext decl : decls) {
			visitor.visit(decl);
			if (doneVisiting.data) break;
			declIndex.data++;
		}
		
		return result;
	}
	
	/**
	 * Translates ANTLR rules for annotations into Typhon annotations.
	 * 
	 * @param tni
	 * @param rules The list of annotation rules. Cannot be null.
	 * @return A list representing the ANTLR annotations given as input.
	 */
	public static List<Annotation> readAnnots(TyphonInput tni, List<AnnotationContext> rules) {
		return rules.stream().map((rule)->{
			Annotation annot = new Annotation(tni, new SourceInfo(rule));
			
			annot.setRawData(rule.tnName);
			if (rule.tnArgs != null) annot.getArgs().addAll(readArgs(tni, rule.tnArgs.tnArgs));
			
			return annot;
		}).collect(Collectors.toCollection(()->new ArrayList<>()));
	}
	
	/**
	 * Translates ANTLR rules for global annotations into Typhon annotations.
	 * 
	 * @param tni
	 * @param rule The annotation rule. Cannot be null.
	 * @return The annotation representing the ANTLR rule given as input.
	 */
	public static Annotation readGlobalAnnot(TyphonInput tni, GlobalAnnotationContext rule) {
		Annotation annot = new Annotation(tni, new SourceInfo(rule));
		
		annot.setRawData(rule.tnName);
		if (rule.tnArgs != null)
		for (ArgDeclContext argRule : rule.tnArgs.tnArgs) {
			Argument arg = new Argument(tni, new SourceInfo(argRule));
			
			if (argRule.tnKey != null) arg.setLabel(argRule.tnKey.getText());
			arg.setRawData(argRule.tnValue);
			
			annot.getArgs().add(arg);
		}
		
		return annot;
	}
	
	/**
	 * Translates an ANTLR rule for a function into a Typhon function.
	 * 
	 * @param tni
	 * @param rule The method declaration. Cannot be null.
	 * @return The function the input represents.
	 */
	public static Function readFunction(TyphonInput tni, MethodDeclContext rule) {
		Function f = new Function(tni, new SourceInfo(rule), rule.tnName.getText());
		
		if (rule.tnArgs != null) {
			f.getParams().addAll(readParams(tni, rule.tnArgs.tnArgs));
		}
		if (rule.tnTemplate != null) {
			f.getTemplate().addAll(readTemplateParams(tni, rule.tnTemplate.tnArgs));
		}
		
		if (rule.tnExprForm == null && rule.tnStubForm == null) {
			f.setRawData(readTypes(rule.tnRetType), Function.Form.BLOCK, rule.tnBlockForm);
		} else if (rule.tnExprForm != null) {
			f.setRawData(readTypes(rule.tnRetType), Function.Form.EXPR, rule.tnExprForm.tnExprs);
		} else {
			f.setRawData(readTypes(rule.tnRetType), Function.Form.STUB, null);
		}
		
		f.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
		return f;
	}
	
	/**
	 * Translates an ANTLR rule for a field into Typhon fields.
	 * 
	 * @param tni
	 * @param rule The field declaration. Cannot be null.
	 * @return The fields the input represents.
	 */
	public static List<Field> readField(TyphonInput tni, FieldDeclContext rule) {
		ArrayList<Field> a = new ArrayList<>();
		
		int i = 0;
		for (ParamNameContext name : rule.tnNames) {
			Field f = new Field(tni, new SourceInfo(rule), name.tnName.getText());
			
			if (i < rule.tnValues.size()) {
				f.setRawData(rule.tnType, rule.tnValues.get(i));
			} else {
				f.setRawData(rule.tnType, null);
			}
			f.getAnnots().addAll(readAnnots(tni, name.tnAnnots));
			
			a.add(f);
			i++;
		}
		
		return a;
	}
	
	public static Import readImport(TyphonInput tni, ImportDeclContext rule) {
		if (rule.tnRawName == null) {
			PackageImport i = new PackageImport(tni, new SourceInfo(rule));
			i.getPackageName().addAll(rule.tnName.tnName.stream().map((name)->name.getText()).collect(Collectors.toList()));
			if (rule.tnAlias != null) i.getAliasName().addAll(rule.tnAlias.tnName.stream().map((name)->name.getText()).collect(Collectors.toList()));
			i.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
			return i;
		} else {
			RawImport i = new RawImport(tni, new SourceInfo(rule));
			i.setImportData(rule.tnRawName.getText().substring(1, rule.tnRawName.getText().length()-1));
			if (rule.tnAlias != null) i.getAliasName().addAll(rule.tnAlias.tnName.stream().map((name)->name.getText()).collect(Collectors.toList()));
			i.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
			return i;
		}
	}
	
	/**
	 * Translates ANTLR rules for parameters into Typhon parameters.
	 * 
	 * @param tni
	 * @param rules The parameters. Cannot be null.
	 * @return The list of parameters the input represents.
	 */
	public static List<Parameter> readParams(TyphonInput tni, List<ParamDeclContext> rules) {
		return rules.stream().map((rule)->{
			Parameter p = new Parameter(tni, new SourceInfo(rule));
			
			p.setName(rule.tnName.getText());
			p.setRawData(rule.tnType, rule.tnDefaultValue);
			
			p.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
			p.setRawData();
			return p;
		}).collect(Collectors.toCollection(()->new ArrayList<>()));
	}
	
	/**
	 * Translates ANTLR rules for template parameters into Typhon template parameters.
	 * 
	 * @param tni
	 * @param rules The parameters. Cannot be null.
	 * @return The list of parameters the input represents.
	 */
	public static List<TemplateType> readTemplateParams(TyphonInput tni, List<TemplateDeclContext> rules) {
		return rules.stream().map((rule)->{
			TemplateType t = new TemplateType(tni, new SourceInfo(rule), rule.tnName.getText());
			
			t.setRawData(rule.tnBaseType, rule.tnDefaultType);
			t.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
			
			return t;
		}).collect(Collectors.toCollection(()->new ArrayList<>()));
	}
	
	/**
	 * Translates ANTLR rules for arguments into Typhon arguments.
	 * 
	 * @param tni
	 * @param rules The arguments. Cannot be null.
	 * @return The list of arguments the input represents.
	 */
	public static List<Argument> readArgs(TyphonInput tni, List<ArgDeclContext> rules) {
		return rules.stream().map((rule)->{
			Argument arg = new Argument(tni, new SourceInfo(rule));
			
			if (rule.tnKey != null) arg.setLabel(rule.tnKey.getText());
			arg.setRawData(rule.tnValue);
			
			return arg;
		}).collect(Collectors.toCollection(()->new ArrayList<>()));
	}
	
	/**
	 * Parses a return type specification.
	 * Used to ensure 'void' is equivalent to '()', among other things.
	 * 
	 * @param rule The return types.
	 * @return The list of actual return types.
	 */
	public static List<TypeContext> readTypes(TypesContext rule) {
		if (rule instanceof SingleTypesContext) {
			return Arrays.asList(((SingleTypesContext)rule).tnType);
		} else if (rule instanceof MultiTypesContext) {
			return ((MultiTypesContext)rule).tnTypes;
		} else if (rule instanceof VoidTypesContext) {
			return new ArrayList<>();
		} else {
			throw new IllegalArgumentException("Unknown subclass of TypesContext");
		}
	}
	
	/**
	 * Translates ANTLR rules for a static init block into a Typhon static init block.
	 * 
	 * @param tni
	 * @param rule The block. Cannot be null.
	 * @return The block the input represents.
	 */
	public static StaticInitBlock readStaticInitBlock(TyphonInput tni, StaticInitDeclContext rule) {
		StaticInitBlock block = new StaticInitBlock(tni, new SourceInfo(rule));
		
		block.setRawData(rule.tnBlock);
		block.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
		
		return block;
	}
	
	/**
	 * Translates ANTLR rules for a class into a Typhon type.
	 * 
	 * @param tni
	 * @param rule The class declaration. Cannot be null.
	 * @return The type.
	 */
	public static UserType readClass(TyphonInput tni, ClassDeclContext rule) {
		UserType t = new UserType(tni, new SourceInfo(rule), rule.tnName.getText());
		
		if (rule.tnTemplate != null) t.getTemplates().addAll(readTemplateParams(tni, rule.tnTemplate.tnArgs));
		t.setRawData(rule.tnExtends);
		t.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
		
		readPackage(t.getTypePackage(), rule.tnDecls);
		
		if (!t.getTypePackage().getFunctions().stream().anyMatch(f->f instanceof Constructor && f.getFieldOf() == t)) {
			// add a default constructor
			Constructor c = new Constructor(tni);
			t.getTypePackage().addFunction(c);
			PluginLoader.runHook(TyphonPlugin.OnInitDefaultConstructor.class, c);
		}
		
		return t;
	}
	
	/**
	 * Translates ANTLR rules for a constructor into a Typhon function.
	 * 
	 * @param tni
	 * @param rule The constructor. Cannot be null.
	 * @return The function.
	 */
	public static Constructor readConstructor(TyphonInput tni, ConstructorDeclContext rule) {
		Constructor f = new Constructor(tni, new SourceInfo(rule));
		
		for (ConstructorParamContext argRule : rule.tnArgs) {
			ConstructorParameter p = new ConstructorParameter(tni, new SourceInfo(argRule));
			if (argRule instanceof ConstructorParamThisContext) {
				p.isField(true);
				p.setName(((ConstructorParamThisContext)argRule).tnName.getText());
				p.getAnnots().addAll(readAnnots(tni, ((ConstructorParamThisContext)argRule).tnAnnots));
				p.setRawData(null, ((ConstructorParamThisContext)argRule).tnDefaultValue);
			} else if (argRule instanceof ConstructorParamTypedContext) {
				p.isField(false);
				p.setName(((ConstructorParamTypedContext)argRule).tnName.getText());
				p.getAnnots().addAll(readAnnots(tni, ((ConstructorParamTypedContext)argRule).tnAnnots));
				p.setRawData(((ConstructorParamTypedContext)argRule).tnType, ((ConstructorParamTypedContext)argRule).tnDefaultValue);
			} else {
				throw new IllegalArgumentException("Unknown subclass of ConstructorParamContext");
			}
			f.getConstParams().add(p);
		}
		
		if (rule.tnExprForm == null && rule.tnStubForm == null) {
			f.setRawData(Function.Form.BLOCK, rule.tnBlockForm);
		} else if (rule.tnExprForm != null) {
			f.setRawData(Function.Form.EXPR, rule.tnExprForm.tnExprs);
		} else {
			f.setRawData(Function.Form.STUB, null);
		}
		
		f.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
		return f;
	}
	
	/**
	 * Translates ANTLR rules for an enum class into a Typhon type.
	 * 
	 * @param tni
	 * @param rule The enum class declaration. Cannot be null.
	 * @return The type.
	 */
	public static EnumType readEnum(TyphonInput tni, EnumDeclContext rule) {
		EnumType t = new EnumType(tni, new SourceInfo(rule), rule.tnName.getText());
		
		t.setRawData(rule.tnExtends);
		t.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
		readPackage(t.getTypePackage(), rule.tnDecls);
		
		for (EnumValueDeclContext choiceRule : rule.tnValues) {
			EnumChoice choice = new EnumChoice(tni, new SourceInfo(choiceRule), choiceRule.tnName.getText(), t);
			
			if (choiceRule.tnArgs != null) choice.getArgs().addAll(readArgs(tni, choiceRule.tnArgs.tnArgs));
			choice.getAnnots().addAll(readAnnots(tni, choiceRule.tnAnnots));
			
			t.getChoices().add(choice);
		}
		
		return t;
	}
}
