package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class WinDebugValue extends DebugElement implements IValue {

	private String valueText;
	
	public WinDebugValue(WinDebugVariable variable, String valueText) {
		super(variable.getDebugTarget());
		this.valueText = valueText;
	}
	
	@Override
	public String getReferenceTypeName() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValueString() throws DebugException {
		return valueText;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAllocated() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

}
