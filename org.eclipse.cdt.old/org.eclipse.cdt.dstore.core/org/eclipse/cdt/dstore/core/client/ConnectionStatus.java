package org.eclipse.cdt.dstore.core.client;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import java.lang.*;
import java.util.*;

public class ConnectionStatus
{
    private boolean    _connected;
    private Throwable  _exception;
    private String     _message;
    private String     _ticket;

    public ConnectionStatus(boolean connected)
    {
        _connected = connected;
    }
    
    public ConnectionStatus(boolean connected, Throwable e)
    {
        _connected = connected;
        _exception = e;        
        _message = e.toString();
    }
    
    public ConnectionStatus(boolean connected, String msg)
    {
        _connected = connected;
        _message = msg;
    }

    public void setConnected(boolean flag)
    {
	_connected = flag;
    }
    
    public void setMessage(String message)
    {
	_message = message;
    }

    public void setTicket(String ticket)
    {
	_ticket = ticket;
    }
    
    public boolean isConnected()
    {
        return _connected;
    }
    
    public String getMessage()
    {
        return _message;
    }
    
    public String getTicket()
    {
	return _ticket;
    }
}




