package info.iconmaster.typhon.errors;

import info.iconmaster.typhon.model.TyphonModelEntity;
import info.iconmaster.typhon.util.TemplateUtils;

/**
 * This is an error for when {@link TemplateUtils} finds that there are more template arguments than parameters.
 * 
 * @author iconmaster
 *
 */
public class TemplateNumberError extends TyphonError {
	public TyphonModelEntity toMap;
	public String toMapDesc;
	
	public TemplateNumberError(TyphonModelEntity toMap, String toMapDesc) {
		super(toMap.source);
		this.toMap = toMap;
		this.toMapDesc = toMapDesc;
	}
	
	@Override
	public String getMessage() {
		return "incorrect template arguments to "+toMapDesc+": too many template arguments";
	}
}
