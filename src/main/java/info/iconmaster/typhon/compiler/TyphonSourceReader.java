package info.iconmaster.typhon.compiler;

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
import info.iconmaster.typhon.antlr.TyphonParser.DeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.ExprsContext;
import info.iconmaster.typhon.antlr.TyphonParser.MethodDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.MultiTypesContext;
import info.iconmaster.typhon.antlr.TyphonParser.PackageDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.ParamDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.RootContext;
import info.iconmaster.typhon.antlr.TyphonParser.SimplePackageDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.SingleTypesContext;
import info.iconmaster.typhon.antlr.TyphonParser.TemplateDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypeContext;
import info.iconmaster.typhon.antlr.TyphonParser.TypesContext;
import info.iconmaster.typhon.antlr.TyphonParser.VoidTypesContext;
import info.iconmaster.typhon.errors.SyntaxError;
import info.iconmaster.typhon.language.Annotation;
import info.iconmaster.typhon.language.Argument;
import info.iconmaster.typhon.language.Function;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.language.Parameter;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.util.Box;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This class handles the translation of declarations into the respective language entities.
 * Use this as the first step for translating Typhon text into Typhon code.
 * 
 * @author iconmaster
 *
 */
public class TyphonSourceReader {
	private TyphonSourceReader() {}
	
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
			return readPackage(tni, new SourceInfo(root), "", tni.corePackage, root.tnDecls);
		} catch (ParseCancellationException e) {
			return new Package(new SourceInfo(file.getPath(), 0, (int) file.length()-1), "", tni.corePackage);
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
			return readPackage(tni, new SourceInfo(0, input.length()-1), "", tni.corePackage, parser.root().tnDecls);
		} catch (ParseCancellationException e) {
			return new Package(new SourceInfo(0, input.length()-1), "", tni.corePackage);
		}
	}
	
	/**
	 * Translates an ANTLR rule for a package into a Typhon package.
	 * 
	 * @param tni
	 * @param source Where in the source code a package occurs.
	 * @param name The name of the package. Cannot be null.
	 * @param parent The parent package. Cannot be null. Use <tt>tni.corePackage</tt> when you want a base-level package.
	 * @param decls The ANTLR rules for the declarations in this package.
	 * @return The package representing the ANTLR rules given as input.
	 */
	public static Package readPackage(TyphonInput tni, SourceInfo source, String name, Package parent, List<DeclContext> decls) {
		Package result = new Package(source == null? parent.source : source, name, parent);
		
		Box<Integer> declIndex = new Box<>(0);
		Box<Boolean> doneVisiting = new Box<>(false);
		TyphonBaseVisitor<Void> visitor = new TyphonBaseVisitor<Void>() {
			@Override
			public Void visitPackageDecl(PackageDeclContext decl) {
				List<String> names = decl.tnName.tnName.stream().map((name)->name.getText()).collect(Collectors.toCollection(()->new ArrayList<>()));
				String lastName = names.remove(names.size()-1);
				Package base = result;
				for (String name : names) {
					base = readPackage(tni, new SourceInfo(decl), name, base, new ArrayList<>());
				}
				
				Package p = readPackage(tni, new SourceInfo(decl), lastName, base, decl.tnDecls);
				p.getAnnots().addAll(readAnnots(tni, decl.tnAnnots));
				return null;
			}
			
			@Override
			public Void visitSimplePackageDecl(SimplePackageDeclContext decl) {
				List<String> names = decl.tnName.tnName.stream().map((name)->name.getText()).collect(Collectors.toCollection(()->new ArrayList<>()));
				String lastName = names.remove(names.size()-1);
				Package base = result;
				for (String name : names) {
					base = readPackage(tni, new SourceInfo(decl), name, base, new ArrayList<>());
				}
				
				List<DeclContext> remainingDecls = decls.subList(declIndex.data+1, decls.size());
				Package p = readPackage(tni, new SourceInfo(decl), lastName, base, remainingDecls);
				p.getAnnots().addAll(readAnnots(tni, decl.tnAnnots));
				doneVisiting.data = true;
				return null;
			}
			
			@Override
			public Void visitMethodDecl(MethodDeclContext ctx) {
				Function f = readFunction(tni, ctx);
				result.addFunction(f);
				
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
			if (rule.tnArgs != null)
			for (ArgDeclContext argRule : rule.tnArgs.tnArgs) {
				Argument arg = new Argument(tni, new SourceInfo(argRule));
				
				if (argRule.tnKey != null) arg.setLabel(argRule.tnKey.getText());
				arg.setRawData(argRule.tnValue);
				
				annot.getArgs().add(arg);
			}
			
			return annot;
		}).collect(Collectors.toCollection(()->new ArrayList<>()));
	}
	
	public static Function readFunction(TyphonInput tni, MethodDeclContext rule) {
		Function f = new Function(tni, new SourceInfo(rule), rule.tnName.getText());
		
		if (rule.tnFunc.tnArgs != null) {
			f.getParams().addAll(readParams(tni, rule.tnFunc.tnArgs.tnArgs));
		}
		if (rule.tnFunc.tnTemplate != null) {
			f.getTemplate().addAll(readTemplateParams(tni, rule.tnFunc.tnTemplate.tnArgs));
		}
		
		if (rule.tnFunc.tnBlockForm != null) {
			f.setRawData(readTypes(rule.tnRetType), Function.Form.BLOCK, rule.tnFunc.tnBlockForm.tnBlock);
		} else if (rule.tnFunc.tnExprForm != null) {
			f.setRawData(readTypes(rule.tnRetType), Function.Form.EXPR, rule.tnFunc.tnExprForm.tnExprs);
		}
		
		f.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
		return f;
	}
	
	public static List<Parameter> readParams(TyphonInput tni, List<ParamDeclContext> rules) {
		return rules.stream().map((rule)->{
			Parameter p = new Parameter(tni, new SourceInfo(rule));
			
			p.setName(rule.tnName.getText());
			p.setRawData(rule.tnType, rule.tnDefaultValue);
			
			p.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
			return p;
		}).collect(Collectors.toCollection(()->new ArrayList<>()));
	}
	
	public static List<TemplateType> readTemplateParams(TyphonInput tni, List<TemplateDeclContext> rules) {
		return rules.stream().map((rule)->{
			TemplateType t = new TemplateType(tni, new SourceInfo(rule), rule.tnName.getText());
			
			t.setRawData(rule.tnBaseType, rule.tnDefaultType);
			
			t.getAnnots().addAll(readAnnots(tni, rule.tnAnnots));
			return t;
		}).collect(Collectors.toCollection(()->new ArrayList<>()));
	}
	
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
}
