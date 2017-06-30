package info.iconmaster.typhon.compiler;

import java.util.Collection;
import java.util.List;
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
import info.iconmaster.typhon.antlr.TyphonParser.DeclContext;
import info.iconmaster.typhon.antlr.TyphonParser.FieldDeclContext;
import info.iconmaster.typhon.language.Field;

/**
 * Tests <tt>{@link TyphonSourceReader}.readField()</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestFieldReader extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new CaseValid("var x;", (fs)->{
			Assert.assertEquals(1, fs.size());
			
			Field f = fs.get(0);
			Assert.assertEquals("x", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNull(f.getRawValue());
		}),new CaseValid("var x = 3;", (fs)->{
			Assert.assertEquals(1, fs.size());
			
			Field f = fs.get(0);
			Assert.assertEquals("x", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNotNull(f.getRawValue());
		}),new CaseValid("var x,y;", (fs)->{
			Assert.assertEquals(2, fs.size());
			
			Field f;
			
			f = fs.get(0);
			Assert.assertEquals("x", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNull(f.getRawValue());
			
			f = fs.get(1);
			Assert.assertEquals("y", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNull(f.getRawValue());
		}),new CaseValid("var x,y = 1;", (fs)->{
			Assert.assertEquals(2, fs.size());
			
			Field f;
			
			f = fs.get(0);
			Assert.assertEquals("x", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNotNull(f.getRawValue());
			
			f = fs.get(1);
			Assert.assertEquals("y", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNull(f.getRawValue());
		}),new CaseValid("var x,y = 1,2;", (fs)->{
			Assert.assertEquals(2, fs.size());
			
			Field f;
			
			f = fs.get(0);
			Assert.assertEquals("x", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNotNull(f.getRawValue());
			
			f = fs.get(1);
			Assert.assertEquals("y", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNotNull(f.getRawValue());
		}),new CaseValid("var x = 3,4;", (fs)->{
			Assert.assertEquals(1, fs.size());
			
			Field f = fs.get(0);
			Assert.assertEquals("x", f.getName());
			Assert.assertNotNull(f.getRawType());
			Assert.assertNotNull(f.getRawValue());
		}));
	}
    
    private static class CaseValid implements Runnable {
    	String input;
    	Consumer<List<Field>> test;
    	
		public CaseValid(String input, Consumer<List<Field>> test) {
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
			Assert.assertTrue("'"+input+"' was not a methodDecl: ", root instanceof FieldDeclContext);
			test.accept(TyphonSourceReader.readField(tni, (FieldDeclContext)root));
		}
    }
}
