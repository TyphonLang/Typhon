package info.iconmaster.typhon.compiler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonBaseVisitor;
import info.iconmaster.typhon.antlr.TyphonLexer;
import info.iconmaster.typhon.antlr.TyphonParser;
import info.iconmaster.typhon.antlr.TyphonParser.DeclContext;
import info.iconmaster.typhon.language.Package;

public class TyphonSourceReader {
	private TyphonSourceReader() {}
	
	public static Package parseFile(TyphonInput tni, File file) {
		try {
			TyphonLexer lexer = new TyphonLexer(new ANTLRFileStream(file.getName()));
			TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
			return readPackage(tni, null, null, parser.root().tnDecls);
		} catch (IOException e) {
			// TODO: handle errors
		}
		return new Package();
	}
	
	public static Package parseString(TyphonInput tni, String input) {
		TyphonLexer lexer = new TyphonLexer(new ANTLRInputStream(input));
		TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
		return readPackage(tni, null, tni.corePackage, parser.root().tnDecls);
	}
	
	public static Package readPackage(TyphonInput tni, String name, Package parent, List<DeclContext> decls) {
		Package result = new Package();
		
		result.name = name; result.parent = parent;
		
		TyphonBaseVisitor<Void> visitor = new TyphonBaseVisitor<Void>() {
			
		};
		
		for (DeclContext decl : decls) {
			visitor.visit(decl);
		}
		
		return result;
	}
}
