//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

//import com.ibm.debug.gdbPicl.*;
public class GdbStackFrame
{
    class LocalVariable
    {   //int slot;
        String name;
        String signature = "";
        boolean methodArgument;
        LocalVariable()
        {
        }
    }
    String fileName;
    String methodName;
    String frameAddress;
    int pc;
    int pcAbsolute;
    int lineno;
    int moduleID = -1;;
    LocalVariable localVariables[];
    String methodSignature;

    public String toString()
    {  return fileName+"."+methodName+":"+lineno;
    }
    private int idFrame;
    private int thread;
    private int iFrame;

    GdbStackFrame(int frameID, int iThread, int frameNumber)
    {   idFrame = frameID;
        thread = iThread;
        iFrame = frameNumber;
    }

    GdbStackFrame(int frameID, int iThread, int frameNumber, String file_Name, String method_Name, String stack_Address, String line_no, int iModuleID )
    {   idFrame = frameID;
        thread = iThread;
        iFrame = frameNumber;
        int i = 0;
        try{ i = Integer.parseInt(line_no); }
        catch(java.lang.NumberFormatException exc) { i=0; }
        fileName = file_Name;
        methodName = method_Name;
        frameAddress = stack_Address;
        lineno = i;
        moduleID = iModuleID;
    }

    public synchronized LocalVariable getLocalVariable(String string)
        throws Exception
    {
        System.out.print("getLocalVariable: name="+string );
        System.out.print("   stackframe=" );
        System.out.print(localVariables.length + " local vars");
        for (int i = 0; i < localVariables.length; i++)
        {
            System.out.print("   trying "+localVariables[i].name );
            if (localVariables[i].name.equals(string))
            {
                return localVariables[i];
                //RemoteValue remoteValue = agent.getStackValue(thread.getId(), iFrame, localVariables[i].slot, localVariables[i].signature.charAt(0));
                //return new RemoteStackVariable(this, localVariables[i].slot, string, localVariables[i].signature, localVariables[i].methodArgument, remoteValue);
            }
        }
        return null;
    }

    public synchronized LocalVariable[] getLocalVariables(GdbDebugSession debugSession)
        throws Exception
    {
        System.out.print("getLocalVariables:");
        System.out.print("   stackframe=" );

        String [] _gdbLocals = debugSession._getGdbLocals.getLocals(thread);

        System.out.print(localVariables.length + " local vars" );
        LocalVariable aremoteStackVariable[] = new LocalVariable[localVariables.length];
        for (int i = 0; i < aremoteStackVariable.length; i++)
        {
            //RemoteValue remoteValue = agent.getStackValue(thread.getId(), iFrame, localVariables[i].slot, localVariables[i].signature.charAt(0));
            //aremoteStackVariable[i] = new RemoteStackVariable(this, localVariables[i].slot, localVariables[i].name, localVariables[i].signature, localVariables[i].methodArgument, remoteValue);
            aremoteStackVariable[i] = localVariables[i];
        }
        return aremoteStackVariable;
    }

    public synchronized int getLineNumber()
    {   
       return lineno;
    }

    public synchronized int getModuleID()
    {   
       return moduleID;
    }

    public synchronized String getFileName()
    {   if(fileName!=null && !fileName.equals(""))
            return fileName;
        else
            return "?FileName?";
    }

    public synchronized String getFrameAddress()
    {   if(frameAddress!=null && !frameAddress.equals(""))
            return frameAddress;
        else
            return "?Address?";
    }

    public synchronized String getMethodName()
    {   return methodName;
    }

    public synchronized String getMethodSignature()
    {   return methodSignature;
    }

    public synchronized int getPC()
    {   return pc;
    }

    void setVariable(int i, int j) throws Exception
    {   //agent.setStackValue(thread.getId(), iFrame, i, j);
    }
    void setVariable(int i, boolean flag) throws Exception
    {   //agent.setStackValue(thread.getId(), iFrame, i, flag);
    }
    void setVariable(int i, char ch) throws Exception
    {   //agent.setStackValue(thread.getId(), iFrame, i, ch);
    }
    void setVariable(int i, long j) throws Exception
    {
        //agent.setStackValue(thread.getId(), iFrame, i, j);
    }
    void setVariable(int i, float f) throws Exception
    {   //agent.setStackValue(thread.getId(), iFrame, i, f);
    }
    void setVariable(int i, double d) throws Exception
    {   //agent.setStackValue(thread.getId(), iFrame, i, d);
    }
}
