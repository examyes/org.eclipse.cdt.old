package org.eclipse.cdt.dstore.hosts;


/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import org.eclipse.jface.action.*;


public class HostsSchemaExtender implements ISchemaExtender
{
    private ExternalLoader _loader;
    private HostsPlugin    _plugin;

    public HostsSchemaExtender(ExternalLoader loader)
    {
	_loader = loader ;
	_plugin = HostsPlugin.getInstance();
    }

    public HostsSchemaExtender()
    {
	_loader = new ExternalLoader(HostsPlugin.getDefault().getDescriptor().getPluginClassLoader(), 
				     "org.eclipse.cdt.dstore.hosts.*");
	_plugin = HostsPlugin.getInstance();
    }

    public ExternalLoader getExternalLoader()
    {
	return _loader;
    }

    public void extendSchema(DataElement schemaRoot)
    {
	DataStore   dataStore = schemaRoot.getDataStore();
	DataElement fileD     = dataStore.find(schemaRoot, DE.A_NAME, "file", 1);
	DataElement dirD      = dataStore.find(schemaRoot, DE.A_NAME, "directory", 1);
	DataElement fsD       = dataStore.find(schemaRoot, DE.A_NAME, "Filesystem Objects", 1);
	DataElement rootD     = dataStore.find(schemaRoot, DE.A_NAME, "root", 1);
	DataElement hostD     = dataStore.find(schemaRoot, DE.A_NAME, "host", 1);
		
	DataElement connect = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						     dataStore.getLocalizedString("model.Connect_to"), 
						     "org.eclipse.cdt.dstore.hosts.actions.HostConnectAction");
        connect.setAttribute(DE.A_VALUE, "C_CONNECT");
	
	DataElement disconnect = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
							dataStore.getLocalizedString("model.Disconnect_from"), 
							"org.eclipse.cdt.dstore.ui.connections.DisconnectAction");	 
        disconnect.setAttribute(DE.A_VALUE, "C_DISCONNECT");
	
	DataElement editConnection = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						  _plugin.getLocalizedString("model.Edit_Connection"), 
						  "org.eclipse.cdt.dstore.ui.connections.EditConnectionAction");	 
        editConnection.setAttribute(DE.A_VALUE, "C_EDIT");


	DataElement removeConnection = dataStore.createObject(rootD, DE.T_UI_COMMAND_DESCRIPTOR, 
						    dataStore.getLocalizedString("model.Delete_Connection"), 
						    "org.eclipse.cdt.dstore.ui.connections.DeleteAction");	 
        removeConnection.setAttribute(DE.A_VALUE, "C_DELETE");	

	DataElement fileTransfer = dataStore.createObject(fsD, DE.T_UI_COMMAND_DESCRIPTOR,
							  _plugin.getLocalizedString("model.File_Transfer"), 
							  "org.eclipse.cdt.dstore.hosts.actions.FileTransferAction");
	
	DataElement findFiles = dataStore.createObject(fsD, DE.T_UI_COMMAND_DESCRIPTOR,
						       _plugin.getLocalizedString("model.Find_Files"), 
						       "org.eclipse.cdt.dstore.hosts.actions.FindFileAction");
	findFiles.setAttribute(DE.A_VALUE, "C_FIND_FILES_ACTION");

	/*
	  DataElement dictionarySearch = dataStore.createObject(hostD, DE.T_UI_COMMAND_DESCRIPTOR,
							      _plugin.getLocalizedString("model.Dictionary_Search"), 
							      "org.eclipse.cdt.dstore.hosts.actions.SearchDictionaryAction");
	dictionarySearch.setAttribute(DE.A_VALUE, "C_DICTIONARY_SEARCH_ACTION");
	*/

	DataElement newD = dataStore.find(fsD, DE.A_VALUE, "C_NEW", 1);
	DataElement newFile = dataStore.createObject(newD, DE.T_UI_COMMAND_DESCRIPTOR,
						     _plugin.getLocalizedString("model.New_File"),
						     "org.eclipse.cdt.dstore.hosts.actions.NewFile");
	
	DataElement renameResource = dataStore.createObject(fileD, DE.T_UI_COMMAND_DESCRIPTOR,
							    _plugin.getLocalizedString("model.Rename_Resource"),
							    "org.eclipse.cdt.dstore.hosts.actions.RenameResource");

	renameResource.setAttribute(DE.A_VALUE, "RENAME");

	DataElement deleteResource = dataStore.createObject(fileD, DE.T_UI_COMMAND_DESCRIPTOR,
							    _plugin.getLocalizedString("model.Delete_Resource"),
							    "org.eclipse.cdt.dstore.hosts.actions.DeleteResource");

	deleteResource.setAttribute(DE.A_VALUE, "DELETE");

    }
}
