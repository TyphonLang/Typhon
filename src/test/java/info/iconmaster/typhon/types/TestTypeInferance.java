package info.iconmaster.typhon.types;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.runners.Parameterized;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.TyphonTest;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.CorePackage;
import info.iconmaster.typhon.model.TemplateArgument;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * Tests <tt>{@link TyphonLinker}</tt>.
 * 
 * @author iconmaster
 *
 */
public class TestTypeInferance extends TyphonTest {
	@Parameterized.Parameters
    public static Collection<Object[]> data() {
    	TyphonInput tni = new TyphonInput();
    	CorePackage p = tni.corePackage;
    	
    	TemplateType ta = new TemplateType(tni, "A");
    	TemplateType tb = new TemplateType(tni, "B"); tb.setBaseType(new TypeRef(p.TYPE_NUMBER));
    	
    	TypeRef listOfA = new TypeRef(p.TYPE_LIST, new TemplateArgument(ta));
    	TypeRef listOfInt = new TypeRef(p.TYPE_LIST, new TemplateArgument(p.TYPE_INT));
    	
		return TyphonTest.makeData(
				new TestCase(new TypeRef[] {new TypeRef(ta)}, new TypeRef[] {new TypeRef(p.TYPE_INT)}, new HashMap<TemplateType, TypeRef>() {{put(ta, new TypeRef(p.TYPE_ANY));}}, (map)->{
					Assert.assertNotNull(map.get(ta));
					Assert.assertEquals(p.TYPE_INT, map.get(ta).getType());
				}),new TestCase(new TypeRef[] {new TypeRef(ta), new TypeRef(ta)}, new TypeRef[] {new TypeRef(p.TYPE_INT), new TypeRef(p.TYPE_FLOAT)}, new HashMap<TemplateType, TypeRef>() {{put(ta, new TypeRef(p.TYPE_ANY));}}, (map)->{
					Assert.assertNotNull(map.get(ta));
					Assert.assertEquals(p.TYPE_ANY, map.get(ta).getType());
				}),new TestCase(new TypeRef[] {new TypeRef(tb), new TypeRef(tb)}, new TypeRef[] {new TypeRef(p.TYPE_INT), new TypeRef(p.TYPE_STRING)}, new HashMap<TemplateType, TypeRef>() {{put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				}),new TestCase(new TypeRef[] {new TypeRef(tb), new TypeRef(tb)}, new TypeRef[] {new TypeRef(p.TYPE_STRING), new TypeRef(p.TYPE_INT)}, new HashMap<TemplateType, TypeRef>() {{put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				}),new TestCase(new TypeRef[] {new TypeRef(tb), new TypeRef(tb)}, new TypeRef[] {new TypeRef(p.TYPE_STRING), new TypeRef(p.TYPE_INT)}, new HashMap<TemplateType, TypeRef>() {{put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				}),new TestCase(new TypeRef[] {new TypeRef(tb), new TypeRef(tb)}, new TypeRef[] {new TypeRef(p.TYPE_STRING), new TypeRef(p.TYPE_INT)}, new HashMap<TemplateType, TypeRef>() {{put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				}),new TestCase(new TypeRef[] {listOfA}, new TypeRef[] {listOfInt}, new HashMap<TemplateType, TypeRef>() {{put(ta, new TypeRef(p.TYPE_ANY));}}, (map)->{
					Assert.assertNotNull(map.get(ta));
					Assert.assertEquals(p.TYPE_INT, map.get(ta).getType());
				})
		);
	}
    
    private static class TestCase implements Runnable {
    	TypeRef[] params;
    	TypeRef[] args;
    	Map<TemplateType, TypeRef> defaults;
    	Consumer<Map<TemplateType, TypeRef>> f;
    	
		public TestCase(TypeRef[] params, TypeRef[] args, Map<TemplateType, TypeRef> defaults, Consumer<Map<TemplateType, TypeRef>> f) {
			this.params = params;
			this.args = args;
			this.defaults = defaults;
			this.f = f;
		}
		
		@Override
		public void run() {
			f.accept(TemplateUtils.inferMapFromArguments(Arrays.asList(params), Arrays.asList(args), defaults));
		}
    }
}
