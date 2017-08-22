package info.iconmaster.typhon.model;

import java.util.Collection;
import java.util.function.Consumer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.antlr.TyphonLexer;
import info.iconmaster.typhon.antlr.TyphonParser;
import info.iconmaster.typhon.antlr.TyphonParser.ClassDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.DeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.EnumDeclContext;
import info.iconmaster.typhon.types.EnumType;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.UserType;

/**
 * Tests <tt>{@link TyphonModelReader}.readClass()</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestClassReader extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new CaseValid("class x {}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			
			Assert.assertEquals(0, t.getTypePackage().getTypes().size());
		}),new CaseValid("class x<t> {}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(1, t.getTemplates().size());
			
			Assert.assertEquals(1, t.getTypePackage().getTypes().size());
			Type tt = t.getTypePackage().getType("t");
			Assert.assertNotNull(tt);
		}),new CaseValid("class x : y {}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(1, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			
			Assert.assertEquals(0, t.getTypePackage().getTypes().size());
		}),new CaseValid("class x : y,z {}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(2, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			
			Assert.assertEquals(0, t.getTypePackage().getTypes().size());
		}),new CaseValid("class x<a,b> : y<a>,z<b> {}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(2, t.getRawParentTypes().size());
			Assert.assertEquals(2, t.getTemplates().size());
			
			Assert.assertEquals(2, t.getTypePackage().getTypes().size());
			Type tt = t.getTypePackage().getType("a");
			Assert.assertNotNull(tt);
			tt = t.getTypePackage().getType("b");
			Assert.assertNotNull(tt);
		}),new CaseValid("class x {class y {}}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			
			Assert.assertEquals(1, t.getTypePackage().getTypes().size());
		}),new CaseEnumValid("enum x {}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			Assert.assertEquals(0, t.getChoices().size());
		}),new CaseEnumValid("enum x : y {}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(1, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			Assert.assertEquals(0, t.getChoices().size());
		}),new CaseEnumValid("enum x : y,z {}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(2, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			Assert.assertEquals(0, t.getChoices().size());
		}),new CaseEnumValid("enum x {A}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			Assert.assertEquals(1, t.getChoices().size());
		}),new CaseEnumValid("enum x {A,B,C}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			Assert.assertEquals(3, t.getChoices().size());
		}),new CaseEnumValid("enum x {A,B,C; var x;}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			Assert.assertEquals(3, t.getChoices().size());
			
			Assert.assertEquals(1, t.getTypePackage().getFields().size());
		}),new CaseEnumValid("enum x {A,B,C var x;}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			Assert.assertEquals(3, t.getChoices().size());
			
			Assert.assertEquals(1, t.getTypePackage().getFields().size());
		}),new CaseEnumValid("enum x {A(a),B(b),C(c) var x;}", (t)->{
			Assert.assertEquals("x", t.getName());
			Assert.assertEquals(0, t.getAnnots().size());
			Assert.assertEquals(0, t.getRawParentTypes().size());
			Assert.assertEquals(0, t.getTemplates().size());
			Assert.assertEquals(3, t.getChoices().size());
			
			Assert.assertEquals(1, t.getTypePackage().getFields().size());
		}));
	}
    
    private static class CaseValid implements Runnable {
    	String input;
    	Consumer<UserType> test;
    	
		public CaseValid(String input, Consumer<UserType> test) {
			this.input = input;
			this.test = test;
		}
		
		@Override
		public void run() {
			TyphonInput tni = new TyphonInput();
			
			TyphonLexer lexer = new TyphonLexer(new ANTLRInputStream(input));
			TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					Assert.fail("parse of '"+input+"' failed: "+msg);
				}
			});
			
			DeclContext root = parser.decl();
			Assert.assertTrue("'"+input+"' was not a classDecl: ", root instanceof ClassDeclContext);
			test.accept(TyphonModelReader.readClass(tni, (ClassDeclContext)root));
		}
    }
    
    private static class CaseEnumValid implements Runnable {
    	String input;
    	Consumer<EnumType> test;
    	
		public CaseEnumValid(String input, Consumer<EnumType> test) {
			this.input = input;
			this.test = test;
		}
		
		@Override
		public void run() {
			TyphonInput tni = new TyphonInput();
			
			TyphonLexer lexer = new TyphonLexer(new ANTLRInputStream(input));
			TyphonParser parser = new TyphonParser(new CommonTokenStream(lexer));
			parser.removeErrorListeners();
			parser.addErrorListener(new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					Assert.fail("parse of '"+input+"' failed: "+msg);
				}
			});
			
			DeclContext root = parser.decl();
			Assert.assertTrue("'"+input+"' was not a enumDecl: ", root instanceof EnumDeclContext);
			test.accept(TyphonModelReader.readEnum(tni, (EnumDeclContext)root));
		}
    }
}
