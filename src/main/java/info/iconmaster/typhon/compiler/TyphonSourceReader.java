package info.iconmaster.typhon.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import info.iconmaster.typhon.antlr.TyphonParser.DeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.PackageDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.RootContext;
import info.iconmaster.typhon.antlr.TyphonParser.SimplePackageDeclContext;
import info.iconmaster.typhon.errors.SyntaxError;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.util.Box;
import info.iconmaster.typhon.util.SourceInfo;

public class TyphonSourceReader {
	private TyphonSourceReader() {}
	
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
			return readPackage(tni, new SourceInfo(root), null, null, root.tnDecls);
		} catch (ParseCancellationException e) {
			return new Package(tni.corePackage, new SourceInfo(file.getPath(), 0, (int) file.length()-1));
		}
	}
	
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
		
		return readPackage(tni, new SourceInfo(0, input.length()-1), null, tni.corePackage, parser.root().tnDecls);
	}
	
	public static Package readPackage(TyphonInput tni, SourceInfo source, String name, Package parent, List<DeclContext> decls) {
		Package result = new Package(parent, source == null? parent.source : source);
		result.name = name;
		
		// TODO: actually parse packageNames fully
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
				
				readPackage(tni, new SourceInfo(decl), lastName, base, decl.tnDecls);
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
				readPackage(tni, new SourceInfo(decl), lastName, base, remainingDecls);
				doneVisiting.data = true;
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
}
