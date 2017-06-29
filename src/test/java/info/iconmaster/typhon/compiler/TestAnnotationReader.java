package info.iconmaster.typhon.compiler;

import java.util.Arrays;
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
import info.iconmaster.typhon.antlr.TyphonParser.AnnotationContext;
import info.iconmaster.typhon.language.Annotation;

/**
 * Tests <tt>{@link TyphonSourceReader}.readAnnotations()</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestAnnotationReader extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new CaseValid("@a", (a)->{
			Assert.assertEquals("a", a.getRawDefinition().getText());
		}),new CaseValid("@a()", (a)->{
			Assert.assertEquals("a", a.getRawDefinition().getText());
		}),new CaseValid("@a(b,c)", (a)->{
			Assert.assertEquals("a", a.getRawDefinition().getText());
			
			Assert.assertEquals(2, a.getArgs().size());
		}),new CaseValid("@a(@x b,@y c)", (a)->{
			Assert.assertEquals("a", a.getRawDefinition().getText());
			
			Assert.assertEquals(2, a.getArgs().size());
		}));
	}
    
    private static class CaseValid implements Runnable {
    	String input;
    	Consumer<Annotation> test;
    	
		public CaseValid(String input, Consumer<Annotation> test) {
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
			
			AnnotationContext root = parser.annotation();
			List<Annotation> annots = TyphonSourceReader.readAnnots(tni, Arrays.asList(root));
			Assert.assertEquals(1, annots.size());
			test.accept(annots.get(0));
		}
    }
}
