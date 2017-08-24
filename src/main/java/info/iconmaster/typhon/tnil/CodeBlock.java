package info.iconmaster.typhon.tnil;

import java.util.ArrayList;
import java.util.List;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.compiler.Variable;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * A block of code consisting of a list of TNIL operations to execute.
 * 
 * @author iconmaster
 *
 */
public class CodeBlock extends TyphonModelEntity {
	public MemberAccess lookup;
	
	public List<Instruction> ops = new ArrayList<>();
	public List<TypeRef> returnType = new ArrayList<>();
	public List<Integer> returnVars = new ArrayList<>();
	public int slotsUsed = 0;
	public List<Variable> vars = new ArrayList<>();
	
	public CodeBlock(TyphonInput input, SourceInfo source, MemberAccess lookup) {
		super(input, source);
		this.lookup = lookup;
	}

	public CodeBlock(TyphonInput input, MemberAccess lookup) {
		super(input);
		this.lookup = lookup;
	}
}
