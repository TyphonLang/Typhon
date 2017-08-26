package info.iconmaster.typhon.types;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelReader;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestTypeResolver extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
		return TyphonTest.makeData(new TestCase("int x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.tni.corePackage.TYPE_INT, p.getField("x").getType().getType());
			Assert.assertEquals(0, p.getField("x").getType().getTemplateArgs().size());
		}),new TestCase("int f(int x);", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.tni.corePackage.TYPE_INT, p.getFunctions().get(0).getRetType().get(0).getType());
			Assert.assertEquals(0, p.getFunctions().get(0).getRetType().get(0).getTemplateArgs().size());
			Assert.assertEquals(p.tni.corePackage.TYPE_INT, p.getFunctions().get(0).getParams().get(0).getType().getType());
			Assert.assertEquals(0, p.getFunctions().get(0).getParams().get(0).getType().getTemplateArgs().size());
		}),new TestCase("class x : int {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(1, ((UserType)p.getType("x")).getParentTypes().size());
			Assert.assertEquals(p.tni.corePackage.TYPE_INT, ((UserType)p.getType("x")).getParentTypes().get(0).getType());
			Assert.assertEquals(0, ((UserType)p.getType("x")).getParentTypes().get(0).getTemplateArgs().size());
		}),new TestCase("class a {} a x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.getType("a"), p.getField("x").getType().getType());
			Assert.assertEquals(0, p.getField("x").getType().getTemplateArgs().size());
		}),new TestCase("package p {class a {}} p.a x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.getSubpackagesWithName("p").get(0).getType("a"), p.getField("x").getType().getType());
			Assert.assertEquals(0, p.getField("x").getType().getTemplateArgs().size());
		}),new TestCase("package p {class a {} p.a x;}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.getSubpackagesWithName("p").get(0).getType("a"), p.getSubpackagesWithName("p").get(0).getField("x").getType().getType());
			Assert.assertEquals(0, p.getSubpackagesWithName("p").get(0).getField("x").getType().getTemplateArgs().size());
		}),new TestCase("class p {class a {} p.a x;}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.getType("p").getTypePackage().getType("a"), p.getType("p").getTypePackage().getField("x").getType().getType());
			Assert.assertEquals(0, p.getType("p").getTypePackage().getField("x").getType().getTemplateArgs().size());
		}),new TestCase("package p {} package p {class a {}} p.a x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("package p {package q {class a {}}} import p; q.a x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("()->() x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertTrue(p.getField("x").getType().getType() instanceof FunctionType);
			Assert.assertEquals(0, ((FunctionType)p.getField("x").getType().getType()).getArgTypes().size());
			Assert.assertEquals(0, ((FunctionType)p.getField("x").getType().getType()).getRetTypes().size());
			Assert.assertEquals(0, ((FunctionType)p.getField("x").getType().getType()).getTemplate().size());
		}),new TestCase("<T>(int)->(int) x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertTrue(p.getField("x").getType().getType() instanceof FunctionType);
			Assert.assertEquals(1, ((FunctionType)p.getField("x").getType().getType()).getArgTypes().size());
			Assert.assertEquals(1, ((FunctionType)p.getField("x").getType().getType()).getRetTypes().size());
			Assert.assertEquals(1, ((FunctionType)p.getField("x").getType().getType()).getTemplate().size());
		}),new TestCase("<T>(T)->(T) x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertTrue(p.getField("x").getType().getType() instanceof FunctionType);
			Assert.assertEquals(1, ((FunctionType)p.getField("x").getType().getType()).getArgTypes().size());
			Assert.assertEquals(1, ((FunctionType)p.getField("x").getType().getType()).getRetTypes().size());
			Assert.assertEquals(1, ((FunctionType)p.getField("x").getType().getType()).getTemplate().size());
		}),new TestCase("[int] x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.tni.corePackage.TYPE_LIST, p.getField("x").getType().getType());
			Assert.assertEquals(1, p.getField("x").getType().getTemplateArgs().size());
			Assert.assertEquals(p.tni.corePackage.TYPE_INT, p.getField("x").getType().getTemplateArgs().get(0).getValue().getType());
		}),new TestCase("{int:var} x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.tni.corePackage.TYPE_MAP, p.getField("x").getType().getType());
			Assert.assertEquals(2, p.getField("x").getType().getTemplateArgs().size());
			Assert.assertEquals(p.tni.corePackage.TYPE_INT, p.getField("x").getType().getTemplateArgs().get(0).getValue().getType());
			Assert.assertEquals(p.tni.corePackage.TYPE_ANY, p.getField("x").getType().getTemplateArgs().get(1).getValue().getType());
		}),new TestCase("var x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.tni.corePackage.TYPE_ANY, p.getField("x").getType().getType());
			Assert.assertTrue(p.getField("x").getType().isVar());
		}),new TestCase("const int x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.tni.corePackage.TYPE_INT, p.getField("x").getType().getType());
			Assert.assertTrue(p.getField("x").getType().isConst());
		}),new TestCase("class x<T> : List<T> {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.getType("x").getTypePackage().getType("T"), ((UserType)p.getType("x")).getParentTypes().get(0).getTemplateArgs().get(0).getValue().getType());
		}),new TestCase("class x<T> {class y<T> {T z;}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.getType("x").getTypePackage().getType("y").getTypePackage().getType("T"), p.getType("x").getTypePackage().getType("y").getTypePackage().getField("z").getType().getType());
		}),new TestCase("class x<T> {class y<T> {x.T z;}}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.getType("x").getTypePackage().getType("T"), p.getType("x").getTypePackage().getType("y").getTypePackage().getField("z").getType().getType());
		}),new TestCase("class x {class y {}} class z<T : x> {T.y t;}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
			
			Assert.assertEquals(p.getType("x").getTypePackage().getType("y"), p.getType("z").getTypePackage().getField("t").getType().getType());
		}),new TestCase("class x<T> : List<T,T> {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class x<A:int> {} class y : x<float> {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class x<A:int> {} class y : x<B:int> {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class x<T : int = float> {}", (p)->{
			Assert.assertEquals(1, p.tni.errors.size());
		}),new TestCase("class x<A:short,B:int,C:long> {} class y : x<B:int,short,long> {}", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a<T> {} class b {class c {}} a<b>.T.c x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a<T> {} class b {class c {}} a<b>.T.c x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a<A> {} class b<B> {} class c {class C {}} a<b<c>>.A.B.C x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}),new TestCase("class a<A> {} class b {class B {}} a<a<b>>.A.A.B x;", (p)->{
			Assert.assertEquals(0, p.tni.errors.size());
		}));
	}
    
    private static class TestCase implements Runnable {
    	String input;
    	Consumer<Package> test;
    	
		public TestCase(String input, Consumer<Package> test) {
			this.input = input;
			this.test = test;
		}
		
		@Override
		public void run() {
			TyphonInput tni = new TyphonInput();
			Package p = TyphonModelReader.parseString(tni, input);
			TyphonLinker.link(p);
			TyphonTypeResolver.resolve(p);
			test.accept(p);
		}
    }
}
