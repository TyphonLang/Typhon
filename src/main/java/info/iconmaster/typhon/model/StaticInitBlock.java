package info.iconmaster.typhon.model;

import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.compiler.CodeBlock;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * Static initializer blocks can be either static or instance. They run after field initialization and before constructors.
 * 
 * @author iconmaster
 *
 */
public class StaticInitBlock extends TyphonModelEntity {
	/**
	 * The code that runs on initialization.
	 */
	private CodeBlock code;
	
	/**
	 * The ANTLR rule representing the code of this block.
	 */
	private List<StatContext> rawCode;
	
	public StaticInitBlock(TyphonInput tni) {
		super(tni);
	}
	
	public StaticInitBlock(TyphonInput tni, SourceInfo source) {
		super(tni, source);
	}

	/**
	 * @return The code that runs on initialization.
	 */
	public CodeBlock getCode() {
		return code;
	}

	/**
	 * @param code The new code that runs on initialization.
	 */
	public void setCode(CodeBlock code) {
		this.code = code;
	}

	/**
	 * @return The ANTLR rule representing the code of this block.
	 */
	public List<StatContext> getRawCode() {
		return rawCode;
	}

	/**
	 * Sets the raw ANTLR data for this function.
	 * 
	 * @param rawCode The ANTLR rule representing the code of this block.
	 */
	public void setRawData(List<StatContext> rawCode) {
		super.setRawData();
		this.rawCode = rawCode;
	}
	
	/**
	 * The package this block belongs to.
	 */
	private Package parent;

	/**
	 * @return The package this block belongs to.
	 */
	public Package getParent() {
		return parent;
	}

	/**
	 * NOTE: Don't call this, call <tt>{@link Package}.addStaticInitBlock()</tt> instead.
	 * 
	 * @param parent The new package this block belongs to.
	 */
	public void setParent(Package parent) {
		this.parent = parent;
	}
	
	@Override
	public String prettyPrint() {
		StringBuilder sb = new StringBuilder();
		
		String path = getParent().getPathString();
		if (!path.isEmpty()) {
			sb.append(path);
			sb.append('.');
		}
		sb.append(getParent().getName());
		sb.append(".(static init block)");
		
		return sb.toString();
	}
}
