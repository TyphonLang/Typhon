package info.iconmaster.typhon.compiler;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * A block of code consisting of a list of TNIL operations to execute.
 * 
 * @author iconmaster
 *
 */
public class CodeBlock extends TyphonModelEntity {
	/**
	 * Where this code block occurs, scope-wise.
	 */
	public MemberAccess lookup;
	
	/**
	 * The instructions that compose this block.
	 */
	public List<Instruction> ops = new ArrayList<>();
	
	/**
	 * The local variables present in this code block.
	 */
	public List<Variable> vars = new ArrayList<>();
	
	/**
	 * The instance the code block is executing on. 'this', essentially. May be null if we're in a static context.
	 */
	public Variable instance;
	
	/**
	 * The labels present.
	 */
	public List<Label> labels = new ArrayList<>();
	
	/**
	 * Create a new code block.
	 * 
	 * @param input
	 * @param source
	 * @param lookup Where this code block occurs, scope-wise.
	 */
	public CodeBlock(TyphonInput input, SourceInfo source, MemberAccess lookup) {
		super(input, source);
		this.lookup = lookup;
	}

	/**
	 * Create a new code block.
	 * 
	 * @param input
	 * @param source
	 * @param lookup Where this code block occurs, scope-wise.
	 */
	public CodeBlock(TyphonInput input, MemberAccess lookup) {
		super(input);
		this.lookup = lookup;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CodeBlock(this = ");
		sb.append(instance).append(") {\n");
		for (Instruction i : ops) {
			sb.append('\t').append(i).append('\n');
		}
		sb.append("}\n");
		return sb.toString();
	}
}
