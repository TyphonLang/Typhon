package info.iconmaster.typhon.compiler;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.SourceInfo;

/**
 * This is a single TnIL instruction.
 * 
 * @author iconmaster
 *
 */
public class Instruction extends TyphonModelEntity {
	/**
	 * The opcode of the instruction.
	 */
	public OpCode op;
	
	/**
	 * The arguments of the instruction. Can be of any type.
	 */
	public Object[] args;

	/**
	 * The opcode determines what the instruction will do when run, and determines the format of the arguments.
	 * 
	 * @author iconmaster
	 *
	 */
	public static enum OpCode {
		MOV,
		MOVBYTE,
		MOVSHORT,
		MOVINT,
		MOVLONG,
		MOVFLOAT,
		MOVDOUBLE,
		MOVTRUE,
		MOVFALSE,
		MOVCHAR,
		MOVSTR,
		MOVNULL,
		CALL,
		CALLSTATIC,
		CALLFPTR,
		LABEL,
		JUMP,
		JUMPIF,
		RET,
		ALLOC,
		RAWEQ,
		NOT,
		INSTANCEOF,
		ISNULL,
	}
	
	/**
	 * Create a new instruction.
	 * 
	 * @param tni
	 * @param source
	 * @param op
	 * @param args
	 */
	public Instruction(TyphonInput tni, SourceInfo source, OpCode op, Object[] args) {
		super(tni, source);
		this.op = op;
		this.args = args == null ? new Object[0] : args;
	}
	
	/**
	 * Create a new instruction.
	 * 
	 * @param tni
	 * @param op
	 * @param args
	 */
	public Instruction(TyphonInput tni, OpCode op, Object[] args) {
		super(tni);
		this.op = op;
		this.args = args == null ? new Object[0] : args;
	}
	
	/**
	 * Gets an argument of a certain type.
	 * @param i The argument number.
	 * @return The argument, of type T.
	 */
	@SuppressWarnings("unchecked")
	public <T> T arg(int i) {
		return (T) args[i];
	}
}
