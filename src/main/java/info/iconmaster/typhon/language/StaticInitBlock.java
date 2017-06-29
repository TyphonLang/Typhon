package info.iconmaster.typhon.language;

import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.antlr.TyphonParser.StatContext;
import info.iconmaster.typhon.tnil.CodeBlock;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * Static initializer blocks can be either static or instance. They run after field initialization and before constructors.
 * 
 * @author iconmaster
 *
 */
public class StaticInitBlock extends TyphonLanguageEntity {
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
		this.rawCode = rawCode;
	}
}
