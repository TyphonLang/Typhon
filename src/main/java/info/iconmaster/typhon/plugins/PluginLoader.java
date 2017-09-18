package info.iconmaster.typhon.plugins;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

public class PluginLoader {
	private PluginLoader() {}
	
	public static Set<Class<?>> plugins = new HashSet<>();
	
	public static void loadPlugins() throws TyphonPluginException {
		// destroy any existing plugins, just in case we're reloading the plugins list
		runHook(TyphonPlugin.OnUnload.class);
		plugins.clear();
		
		// detect any plugins on the classpath
		Set<Class<?>> pluginClasses = new Reflections().getTypesAnnotatedWith(TyphonPlugin.class);
		plugins.addAll(pluginClasses);
		
		// call the loading events
		runHook(TyphonPlugin.OnLoad.class);
	}
	
	public static String getPluginName(Class<?> plugin) {
		for (Annotation a : plugin.getAnnotations()) {
			if (a instanceof TyphonPlugin) {
				return ((TyphonPlugin) a).name();
			}
		}
		
		return null;
	}
	
	public static Map<Class<?>, Object> runHook(Class<? extends Annotation> annotation, Object... args) throws TyphonPluginException {
		Map<Class<?>, Object> result = new HashMap<>();
		
		for (Class<?> plugin : plugins) {
			Set<Method> hooks = new Reflections(new MethodAnnotationsScanner(), plugin).getMethodsAnnotatedWith(annotation);
			
			if (!hooks.isEmpty()) {
				if (hooks.size() > 1) {
					throw new TyphonPluginException(plugin, "plugins can only have one method annotated with @"+annotation.getSimpleName());
				}
				
				Method hook = hooks.iterator().next();
				
				try {
					result.put(plugin, hook.invoke(null, args));
				} catch (Exception e) {
					throw new TyphonPluginException(plugin, "An error occured during event @"+annotation.getSimpleName()+": "+e.getMessage());
				}
			}
		}
		
		return result;
	}
}
