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
    	
    	TypeRef mapA2B = new TypeRef(p.TYPE_MAP, new TemplateArgument(ta), new TemplateArgument(tb));
    	TypeRef mapFloat2Int = new TypeRef(p.TYPE_MAP, new TemplateArgument("V", p.TYPE_INT), new TemplateArgument("K", p.TYPE_FLOAT));
    	
		return TyphonTest.makeData(
				new TestCase(tni, new TypeRef[] {new TypeRef(ta)}, new TypeRef[] {new TypeRef(p.TYPE_INT)}, new HashMap<TemplateType, TypeRef>() {{put(ta, new TypeRef(p.TYPE_ANY));}}, (map)->{
					Assert.assertNotNull(map.get(ta));
					Assert.assertEquals(p.TYPE_INT, map.get(ta).getType());
				}),new TestCase(tni, new TypeRef[] {new TypeRef(ta), new TypeRef(ta)}, new TypeRef[] {new TypeRef(p.TYPE_INT), new TypeRef(p.TYPE_FLOAT)}, new HashMap<TemplateType, TypeRef>() {{put(ta, new TypeRef(p.TYPE_ANY));}}, (map)->{
					Assert.assertNotNull(map.get(ta));
					Assert.assertEquals(p.TYPE_NUMBER, map.get(ta).getType());
				}),new TestCase(tni, new TypeRef[] {new TypeRef(tb), new TypeRef(tb)}, new TypeRef[] {new TypeRef(p.TYPE_INT), new TypeRef(p.TYPE_STRING)}, new HashMap<TemplateType, TypeRef>() {{put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				}),new TestCase(tni, new TypeRef[] {new TypeRef(tb), new TypeRef(tb)}, new TypeRef[] {new TypeRef(p.TYPE_STRING), new TypeRef(p.TYPE_INT)}, new HashMap<TemplateType, TypeRef>() {{put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				}),new TestCase(tni, new TypeRef[] {new TypeRef(tb), new TypeRef(tb)}, new TypeRef[] {new TypeRef(p.TYPE_STRING), new TypeRef(p.TYPE_INT)}, new HashMap<TemplateType, TypeRef>() {{put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				}),new TestCase(tni, new TypeRef[] {new TypeRef(tb), new TypeRef(tb)}, new TypeRef[] {new TypeRef(p.TYPE_STRING), new TypeRef(p.TYPE_INT)}, new HashMap<TemplateType, TypeRef>() {{put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				}),new TestCase(tni, new TypeRef[] {listOfA}, new TypeRef[] {listOfInt}, new HashMap<TemplateType, TypeRef>() {{put(ta, new TypeRef(p.TYPE_ANY));}}, (map)->{
					Assert.assertNotNull(map.get(ta));
					Assert.assertEquals(p.TYPE_INT, map.get(ta).getType());
				}),new TestCase(tni, new TypeRef[] {mapA2B}, new TypeRef[] {mapFloat2Int}, new HashMap<TemplateType, TypeRef>() {{put(ta, new TypeRef(p.TYPE_ANY)); put(tb, new TypeRef(p.TYPE_NUMBER));}}, (map)->{
					Assert.assertNotNull(map.get(ta));
					Assert.assertEquals(p.TYPE_FLOAT, map.get(ta).getType());
					Assert.assertNotNull(map.get(tb));
					Assert.assertEquals(p.TYPE_INT, map.get(tb).getType());
				})
		);
	}
    
    private static class TestCase implements Runnable {
    	TypeRef[] params;
    	TypeRef[] args;
    	Map<TemplateType, TypeRef> defaults;
    	Consumer<Map<TemplateType, TypeRef>> f;
    	TyphonInput tni;
    	
		public TestCase(TyphonInput tni, TypeRef[] params, TypeRef[] args, Map<TemplateType, TypeRef> defaults, Consumer<Map<TemplateType, TypeRef>> f) {
			this.params = params;
			this.args = args;
			this.defaults = defaults;
			this.f = f;
			this.tni = tni;
		}
		
		@Override
		public void run() {
			f.accept(TemplateUtils.inferTemplatesFromArguments(tni, Arrays.asList(params), Arrays.asList(args), defaults));
		}
    }
}
