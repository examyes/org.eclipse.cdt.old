package com.ibm.dstore.core.client;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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




