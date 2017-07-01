package info.iconmaster.typhon.tnil;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * A block of code consisting of a list of TNIL operations to execute.
 * 
 * @author iconmaster
 *
 */
public class CodeBlock extends TyphonModelEntity {

	public CodeBlock(TyphonInput input, SourceInfo source) {
		super(input, source);
	}

	public CodeBlock(TyphonInput input) {
		super(input);
	}
}
