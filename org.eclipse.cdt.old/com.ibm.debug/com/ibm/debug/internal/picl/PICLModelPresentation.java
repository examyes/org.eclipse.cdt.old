package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLModelPresentation.java, eclipse, eclipse-dev, 20011128
// Version 1.35 (last modified 11/28/01 15:58:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.model.AddressBreakpoint;
import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.DebuggeeException;
import com.ibm.debug.model.EntryBreakpoint;
import com.ibm.debug.model.LineBreakpoint;
import com.ibm.debug.model.Location;
import com.ibm.debug.model.LocationBreakpoint;
import com.ibm.debug.model.ViewInformation;
import java.io.IOException;
import java.util.HashMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugConstants;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;
/**
 * @see IDebugModelPresentation
 */
public class PICLModelPresentation
    extends LabelProvider
    implements IDebugModelPresentation {
    protected boolean fShowQualified = false;
    protected boolean fShowTypes = false;

    private static final String PREFIX = "picl_label_provider.";
    private static final String TERMINATED = "terminated";
    private static final String DISCONNECTED = "disconnected";
    private static final String LINE = "line";
    private static final String ENTRY = "entry";
    private static final String LOAD = "load";
    private static final String WATCH = "watch";
    private static final String ADDRESS = "address";
    private static final String HITCOUNT = "hitCount";
    private static final String ERRORVALUE = "errorvalue";
    private static final String UNKNOWN = "unknown";

    protected static final String fgStringName = "java.lang.String";

    /**
     * Returns a label for the item
     */
    public String getText(Object item) {
        if (item instanceof PICLVariable) {
            PICLVariable var = (PICLVariable) item;
            PICLValue val = (PICLValue) var.getValue();
            String value = "";
            if (!var.hasChildren()) {
                try {
                    value = " = " + val.getValueString();
                } catch (Exception e) {
                    value = " = " + PICLUtils.getResourceString(PREFIX + ERRORVALUE);
                }
            }

            return var.getLabel(fShowQualified, fShowTypes) + value;

            //			if (varLabel != null) {
            //				boolean isString= false;
            //				int spaceIndex= varLabel.lastIndexOf(' ');
            //				StringBuffer buff= new StringBuffer();
            //				String typeName= var.getReferenceTypeName();
            //				isString= typeName.equals(fgStringName);
            //				if (fShowTypes && spaceIndex == -1) {
            //					if (!fShowQualified) {
            //						int index= typeName.lastIndexOf('.');
            //						if (index != -1) {
            //							typeName= typeName.substring(index + 1);
            //						}
            //					}
            //					if (typeName.length() > 0) {
            //						buff.append(typeName);
            //						buff.append(' ');
            //					}
            //				}
            //
            //				if (spaceIndex != -1 && !fShowTypes) {
            //					varLabel= varLabel.substring(spaceIndex + 1);
            //				}
            //				buff.append(varLabel);
            //
            //				String valueString= var.getValueString(fShowQualified);
            //				String valueString;
            //				try {
            //					valueString= var.getValue().getValueString();
            //				} catch(DebugException de) {
            //					return "error on value";
            //				}
            //				if (valueString != null && (isString || valueString.length() > 0)) {
            //					buff.append("= ");
            //					if (isString) {
            //						buff.append('"');
            //					}
            //					buff.append(valueString);
            //					if (isString) {
            //						buff.append('"');
            //					}
            //				}
            //
            //				return buff.toString();
            //			}
        } else
            if (item instanceof PICLDebugElement) {
                String label = ((PICLDebugElement) item).getLabel(fShowQualified);
                /*if (item instanceof IDisconnect) {
                	if (((IDisconnect) item).isDisconnected()) {
                		label= DebugUIUtils.getResourceString(PREFIX + DISCONNECTED) + label;
                		return label;
                	}
                }
                if (item instanceof ITerminate) {
                	if (((ITerminate) item).isTerminated()) {
                		label= DebugUIUtils.getResourceString(PREFIX + TERMINATED) + label;
                		return label;
                	}
                }*/
                return label;
            } else
                if (item instanceof IMarker) {
                    IMarker m = (IMarker) item;
                    
                    // check if it is a breakpoint of our type
               		String markerModelId= DebugPlugin.getDefault().getBreakpointManager().getModelIdentifier(m);
					if (!markerModelId.equals(IPICLDebugConstants.PICL_MODEL_IDENTIFIER)) 
						return null;
                    
                    String markerType = null;
 	                try {
                    	markerType = m.getType();

                        StringBuffer label = new StringBuffer();

                        IResource file = m.getResource();
                        String fileName = null;
                        if (file instanceof IFile) {
                        	fileName = file.getLocation().lastSegment();
                        }
                        if (file instanceof IProject) {
    						if (markerType == IPICLDebugConstants.PICL_LINE_BREAKPOINT) 
								fileName = m.getAttribute(IPICLDebugConstants.SOURCE_FILE_NAME, fileName);
                        }
				
			            if (markerType.equals(IPICLDebugConstants.PICL_LINE_BREAKPOINT)) {
                            label.append(PICLUtils.getResourceString(PREFIX + LINE));
                            label.append(" [");
                            if (fileName != null)
                            	label.append(fileName + ':');
                            label.append(DebugPlugin.getDefault().getBreakpointManager().getLineNumber(m));
                            label.append(']');
                        } else
	                        if (markerType.equals(IPICLDebugConstants.PICL_ENTRY_BREAKPOINT)) {
	                            label.append(PICLUtils.getResourceString(PREFIX + ENTRY));
	                            label.append(" [");
	                            if (fileName != null)
	                            	label.append(fileName + ':');
	                            label.append(m.getAttribute(IPICLDebugConstants.FUNCTION_NAME, "N/A"));
	                            label.append(']');
	                        } else
		                        if (markerType.equals(IPICLDebugConstants.PICL_ADDRESS_BREAKPOINT)) {
		                            label.append(PICLUtils.getResourceString(PREFIX + ADDRESS));
		                            label.append(" [");
		                            label.append(m.getAttribute(IPICLDebugConstants.ADDRESS_EXPRESSION, "N/A"));
		                            label.append(']');
		                        } else
			                        if (markerType.equals(IPICLDebugConstants.PICL_LOAD_BREAKPOINT)) {
			                            label.append(PICLUtils.getResourceString(PREFIX + LOAD));
			                            label.append(" [");
			                            label.append(m.getAttribute(IPICLDebugConstants.MODULE_NAME, "N/A"));
			                            label.append(']');
			                        } else
				                        if (markerType.equals(IPICLDebugConstants.PICL_WATCH_BREAKPOINT)) {
				                            label.append(PICLUtils.getResourceString(PREFIX + WATCH));
				                            label.append(" [");
				                            label.append(m.getAttribute(IPICLDebugConstants.ADDRESS_EXPRESSION, "N/A"));
				                            label.append(']');
				                        }
                        	
                      //  int hitCount = m.getAttribute(IPICLDebugConstants.INSTALL_COUNT, 0);
                      //  label.append(" [");
                      //  label.append(PICLUtils.getResourceString(PREFIX + HITCOUNT));
                      //  label.append(' ');
                      //  label.append(hitCount);
                      //  label.append(']');
                        return label.toString();
                    } catch (CoreException e) {
                        PICLUtils.logError(e);
                    }

                } else
                    if (item instanceof DebuggeeException) {
                        return ((DebuggeeException) item).name();
                    } else
                        if (item instanceof ILauncher) {
                            return ((ILauncher)item).getLabel();
                        } else
                            if (item instanceof String) {
                                return (String) item;
                            }
        return PICLUtils.getResourceString(PREFIX + UNKNOWN);
    }

    /**
     * Maps a Java element to an appropriate image.
     */
    public Image getImage(Object item) {
        if (item instanceof IMarker) {
            IMarker breakpoint = (IMarker) item;
            IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
            if (!manager.isEnabled(breakpoint)) {
                return DebugPluginImages.getImage(
                    IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);
            } else
              //  if (PICLDebugTarget.isBreakpointActive(breakpoint)) {
			  //		return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_ACTIVE_BREAKPOINT);
              //  }
            return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
        }
		if (item instanceof PICLVariable) {
			if (!((PICLVariable)item).isEnabled()) {
				return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_VARIABLE_DISABLED);
			} else if (((PICLVariable)item).hasChanged(false)) {
				return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_VARIABLE_CHANGED);
			}
			return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_VARIABLE);
		}
		
		if(item instanceof PICLRegister)
		{
			if (((PICLRegister)item).hasChanged(false)) {
				return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_REGISTER_CHANGED);
			}
			return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_REGISTER);
		}

		if(item instanceof PICLRegisterGroup)
		{
			return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_REGISTER_GROUP);
		}

		if(item instanceof PICLModule)
		{
			return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_MODULE);
		}

		if(item instanceof PICLPart)
		{
			return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_PART);
		}

		if(item instanceof PICLFile)
		{
			if (((PICLFile)item).hasSource())
				return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_FILE);
			else
				return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_NO_FILE);
		}

		if(item instanceof PICLFunction)
		{
			return PICLUtils.getImage(IPICLDebugConstants.PICL_ICON_FUNCTION);
		}


        return null;
    }

    /**
     * @see IDebugModelPresentation
     */
    public IEditorInput getEditorInput(Object item) {
        /*IJavaElement je= null; */
        if (item instanceof IMarker) {
        	IMarker marker = (IMarker) item;
        	IResource resource = marker.getResource();
            if (resource instanceof IFile)
				return new FileEditorInput((IFile) resource);
			if (resource instanceof IProject) {
				// Marker on an IProject probably means a marker in an EngineSuppliedView.                      
				// Need engine to supply the view content to us.                                                
	                                                                                                            
				// Find the PICLDebugTarget to build the view                                                   
				// If the ViewFile is not available from the current debug target then give up                  
				IDebugTarget target = PICLDebugPlugin.determineCurrentDebugTarget();                            
				if(target instanceof PICLDebugTarget) {                                                         
	  		 		PICLDebugTarget PICLTarget = (PICLDebugTarget)target;                                       
	  		 		Breakpoint bp = PICLTarget.getBreakpoint(marker);                                           
	  		 		if (bp != null && bp instanceof LocationBreakpoint) {                                       
	  		 			LocationBreakpoint locBp = (LocationBreakpoint) bp;                                     
	  		 			ViewInformation viewInfo = null;                                                        
	  		 			if (locBp instanceof AddressBreakpoint)                                                 
	  		 				viewInfo = PICLTarget.getDebugEngine().getDisassemblyViewInformation();             
	  		 			else if (locBp instanceof LineBreakpoint)                                               
	  		 				viewInfo = PICLTarget.getDebugEngine().getSourceViewInformation();                  
	  		 			else if (locBp instanceof EntryBreakpoint)                                              
	  		 			    // Not sure what to pick here                                                       
	  		 				viewInfo = PICLTarget.getDebugEngine().getSourceViewInformation();             
	  		 			if (viewInfo == null)                                                                   
	  		 				return null;                                                                        
	  		 			Location loc = null;                                                                    
	  		 			try {                                                                                   
	  		 				loc = locBp.getLocationWithinView(viewInfo);                                        
	  		 			} catch (IOException e) {                                                               
	  		 				return null;                                                                        
	  		 			}                                                                                       
	  		 			                                                                                        
	  		 			if (loc != null) {                                                                      
							EngineSuppliedViewEditorInput engineEI =
									 new EngineSuppliedViewEditorInput(loc, PICLTarget);    
							engineEI.setProject(resource.getProject());
							// Update the line number in the marker to match the generated view                 
							try {                                                                               
								marker.setAttribute(IMarker.LINE_NUMBER, loc.lineNumber());	                    
							} catch (CoreException e) {                                                         
							}                                                                                   
							return engineEI;                                                                    
						}                                                                                       
	  		 		}                                                                                           
	  		 	}                                                                                               
				return null;
			}
        } /* else
        	if (item instanceof IJavaElement) {
        		je= (IJavaElement) item;
        	}
        if (je instanceof IMember) {
        	IMember m= (IMember) je;
        	if (m.isBinary()) {
        		return m.getClassFile();
        	} else {
        		try {
        			return m.getUnderlyingResource();
        		} catch (JavaModelException e) {
        			return null;
        		}
        	}
        } else {
        	return je;
        }*/
        if (item instanceof IFile)
            return new FileEditorInput((IFile) item);

        if (item instanceof EngineSuppliedViewEditorInput)
            return (EngineSuppliedViewEditorInput) item;

        return null;
    }
    /**
    * @see IDebugModelPresentaion
    */
	public String getEditorId(IEditorInput input, Object inputObject) {
		
		String editorId = null;
		IEditorDescriptor desc = null;
		IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();

		if (input instanceof IFileEditorInput) {
			if (inputObject instanceof IFile) {
				IFile file = (IFile) inputObject;
				desc = reg.getDefaultEditor(file);
				if (desc != null)
					return desc.getId();
			}
			else if (inputObject instanceof IMarker) {
				IResource resource = ((IMarker)inputObject).getResource();
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					desc = reg.getDefaultEditor(file);
					if (desc != null)
						return desc.getId();
				}
			}
		}

		else if (input instanceof EngineSuppliedViewEditorInput) {
			editorId = "com.ibm.debug.pdt.editor.DebuggerEditor";
			desc = (IEditorDescriptor) reg.findEditor(editorId);
		}
        if (desc == null) {
            /* Don't know which editor to use so fall back to default text editor */
            editorId = "org.eclipse.ui.DefaultTextEditor";
        }

        return editorId;
    }
    /**
     * @see IDebugModelPresentation
     */
    public void setAttribute(String id, Object value) {
        if (value == null) {
            return;
        }
        if (id.equals(IDebugModelPresentation.DISPLAY_QUALIFIED_NAMES))
            fShowQualified = ((Boolean) value).booleanValue();
        if (id.equals(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES))
            fShowTypes = ((Boolean) value).booleanValue();

        return;
    }

}
