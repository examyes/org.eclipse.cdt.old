package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugSymbolGroup;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

public class WinDebugVariable extends DebugElement implements IVariable {

	private final String name;
	private final WinDebugValue value;
	
	WinDebugVariable(WinDebugStackFrame frame, IDebugSymbolGroup symbolGroup, int index) throws HRESULTException {
		super(frame.getDebugTarget());
		name = symbolGroup.getSymbolName(index);
		value = new WinDebugValue(this, symbolGroup.getSymbolValueText(index));
	}
	
	@Override
	public String getName() throws DebugException {
		return name;
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IValue getValue() throws DebugException {
		return value;
	}

	@Override
	public boolean hasValueChanged() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(String expression) throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setValue(IValue value) throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsValueModification() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean verifyValue(String expression) throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean verifyValue(IValue value) throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

}
