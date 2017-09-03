package info.iconmaster.typhon.compiler;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.compiler.Instruction.OpCode;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelReader;
import info.iconmaster.typhon.types.TyphonTypeResolver;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestCompiler extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new TestCase("var x; void f() {int y = x as? int;}", (code)->{
			Assert.assertEquals(0, code.tni.errors.size());
		}));
	}
    
    private static class TestCase implements Runnable {
    	String input;
    	Consumer<CodeBlock> test;
    	
		public TestCase(String input, Consumer<CodeBlock> test) {
			this.input = input;
			this.test = test;
		}
		
		@Override
		public void run() {
			TyphonInput tni = new TyphonInput();
			Package p = TyphonModelReader.parseString(tni, input);
			TyphonLinker.link(p);
			TyphonTypeResolver.resolve(p);
			TyphonCompiler.compile(p);
			
			// the test is based on a function called 'f', in this package or a subpackage.
			
			if (p.getFunctionsWithName("f").isEmpty()) {
				for (Package pp : p.getSubpackges()) {
					if (!pp.getFunctionsWithName("f").isEmpty()) {
						test.accept(pp.getFunctionsWithName("f").get(0).getCode());
					}
				}
			} else {
				test.accept(p.getFunctionsWithName("f").get(0).getCode());
			}
		}
    }
}
