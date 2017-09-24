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
import info.iconmaster.typhon.antlr.TyphonParser.ConstructorDeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.DeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.MethodDeclContext;
import info.iconmaster.typhon.model.Constructor;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Parameter;
import info.iconmaster.typhon.model.TyphonModelReader;
import info.iconmaster.typhon.model.Constructor.ConstructorParameter;
import info.iconmaster.typhon.types.TemplateType;

/**
 * Tests <tt>{@link TyphonModelReader}.readFunction()</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestFunctionReader extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new CaseValid("void f() {}", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(0, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
		}),new CaseValid("void f() => ()", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(0, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.EXPR, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
		}),new CaseValid("int f() {}", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(0, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(1, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
		}),new CaseValid("(int,int) f() {}", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(0, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(2, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
		}),new CaseValid("void f(var x) {}", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(1, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			Parameter p = f.getParams().get(0);
			Assert.assertEquals("x", p.getName());
		}),new CaseValid("void f(var x, var y, var z) {}", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(3, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			Parameter p;
			
			p = f.getParams().get(0);
			Assert.assertEquals("x", p.getName());
			p = f.getParams().get(1);
			Assert.assertEquals("y", p.getName());
			p = f.getParams().get(2);
			Assert.assertEquals("z", p.getName());
		}),new CaseValid("void f<T>() {}", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(0, f.getParams().size());
			Assert.assertEquals(1, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			TemplateType t = f.getTemplate().get(0);
			Assert.assertEquals("T", t.getName());
		}),new CaseValid("void f<K,V>() {}", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(0, f.getParams().size());
			Assert.assertEquals(2, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			TemplateType t;
			
			t = f.getTemplate().get(0);
			Assert.assertEquals("K", t.getName());
			t = f.getTemplate().get(1);
			Assert.assertEquals("V", t.getName());
		}),new CaseValid("void f<T:a=b>() {}", (f)->{
			Assert.assertEquals("f", f.getName());
			Assert.assertEquals(0, f.getParams().size());
			Assert.assertEquals(1, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			TemplateType t = f.getTemplate().get(0);
			Assert.assertEquals("T", t.getName());
			
			Assert.assertNotNull(t.getRawBaseType());
			Assert.assertEquals("a", t.getRawBaseType().getText());
			Assert.assertNotNull(t.getRawDefaultValue());
			Assert.assertEquals("b", t.getRawDefaultValue().getText());
		}),new CaseConstructorValid("new() {}", (f)->{
			Assert.assertEquals("new", f.getName());
			Assert.assertEquals(0, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
		}),new CaseConstructorValid("new(var a) {}", (f)->{
			Assert.assertEquals("new", f.getName());
			Assert.assertEquals(1, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			ConstructorParameter p = f.getConstParams().get(0);
			Assert.assertEquals("a", p.getName());
			Assert.assertFalse(p.isField());
		}),new CaseConstructorValid("new(this.a) {}", (f)->{
			Assert.assertEquals("new", f.getName());
			Assert.assertEquals(1, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			ConstructorParameter p = f.getConstParams().get(0);
			Assert.assertEquals("a", p.getName());
			Assert.assertTrue(p.isField());
		}),new CaseConstructorValid("new(var a, this.b, var c) {}", (f)->{
			Assert.assertEquals("new", f.getName());
			Assert.assertEquals(3, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			ConstructorParameter p;
			
			p = f.getConstParams().get(0);
			Assert.assertEquals("a", p.getName());
			Assert.assertFalse(p.isField());
			
			p = f.getConstParams().get(1);
			Assert.assertEquals("b", p.getName());
			Assert.assertTrue(p.isField());
			
			p = f.getConstParams().get(2);
			Assert.assertEquals("c", p.getName());
			Assert.assertFalse(p.isField());
		}),new CaseConstructorValid("new(var @a a = 1, @b this.b = 2, var @c c = 3) {}", (f)->{
			Assert.assertEquals("new", f.getName());
			Assert.assertEquals(3, f.getParams().size());
			Assert.assertEquals(0, f.getTemplate().size());
			Assert.assertEquals(0, f.getRawRetType().size());
			
			Assert.assertEquals(Function.Form.BLOCK, f.getForm());
			Assert.assertEquals(0, f.getRawCode().size());
			
			ConstructorParameter p;
			
			p = f.getConstParams().get(0);
			Assert.assertEquals("a", p.getName());
			Assert.assertFalse(p.isField());
			
			p = f.getConstParams().get(1);
			Assert.assertEquals("b", p.getName());
			Assert.assertTrue(p.isField());
			
			p = f.getConstParams().get(2);
			Assert.assertEquals("c", p.getName());
			Assert.assertFalse(p.isField());
		}));
	}
    
    private static class CaseValid implements Runnable {
    	String input;
    	Consumer<Function> test;
    	
		public CaseValid(String input, Consumer<Function> test) {
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
			Assert.assertTrue("'"+input+"' was not a methodDecl: ", root instanceof MethodDeclContext);
			test.accept(TyphonModelReader.readFunction(tni, (MethodDeclContext)root));
		}
    }
    
    private static class CaseConstructorValid implements Runnable {
    	String input;
    	Consumer<Constructor> test;
    	
		public CaseConstructorValid(String input, Consumer<Constructor> test) {
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
			Assert.assertTrue("'"+input+"' was not a constrcutorDecl: ", root instanceof ConstructorDeclContext);
			test.accept(TyphonModelReader.readConstructor(tni, (ConstructorDeclContext)root));
		}
    }
}
