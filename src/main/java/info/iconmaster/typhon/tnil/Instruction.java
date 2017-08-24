package info.iconmaster.typhon.tnil;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.SourceInfo;

public class Instruction extends TyphonModelEntity {
	public OpCode op;
	public Object[] args;

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
		CALL,
		CALLSTATIC,
		CALLFPTR,
		JUMP,
		JUMPTRUE,
		JUMPFALSE,
		RET,
		ALLOC,
	}
	
	public Instruction(TyphonInput tni, SourceInfo source, OpCode op, Object[] args) {
		super(tni, source);
		this.op = op;
		this.args = args == null ? new Object[0] : args;
	}
	
	public Instruction(TyphonInput tni, OpCode op, Object[] args) {
		super(tni);
		this.op = op;
		this.args = args == null ? new Object[0] : args;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T arg(int i) {
		return (T) args[i];
	}
}
