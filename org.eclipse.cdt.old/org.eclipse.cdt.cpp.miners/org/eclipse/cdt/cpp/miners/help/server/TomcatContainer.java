package org.eclipse.cdt.cpp.miners.help.server;

import org.apache.catalina.*;
import org.apache.catalina.startup.*;
import org.apache.catalina.realm.*;
import org.apache.catalina.logger.*;

import java.io.*;

public class TomcatContainer
{
    public static TomcatContainer _container = null;
    private Embedded _embedded;
    private Engine _engine;
    private Host _host;
    private Context cdthelp;

    private String _path;
    //    private int _port=8080;

    private boolean isRunning = false;
    String _pluginPath=null;

    private TomcatContainer()
    {	
	String a_plugin_path = System.getProperty("A_PLUGIN_PATH");
	if (a_plugin_path == null)
	    {
		//running locally with an eclipse environment
		_pluginPath = System.getProperty("user.dir")+"/plugins/";
	    }
	else
	    {
		//run from server/daemon.
		_pluginPath = a_plugin_path;
	    }

	//String tomcatPath = "org.eclipse.tomcat_4.0.3";
	String tomcatPath = System.getProperty("CDTHELP_TOMCATDIR");
	if(tomcatPath==null)
	    tomcatPath = "org.eclipse.tomcat_4.0.3";  
	
	File logdir;
	if (a_plugin_path == null)
	    {
		logdir= new File(System.getProperty("user.dir")+"/workspace/.metadata/.plugins/org.eclipse.cdt.cpp.miners/tomcatlog");
		if(!logdir.exists())
		    {
			logdir.mkdirs();
		    }
	    }
	else
	    {
		logdir = new File(_pluginPath+"/.metadata/tomcatlog");
		    if(!logdir.exists())
		    {
			logdir.mkdirs();
		    }		
	    }

	Logger logger;	
	if(logdir.exists())
	    {
	        logger = new FileLogger();
		try{
		    ((FileLogger)logger).setDirectory(logdir.getCanonicalPath());
		}catch(IOException e){
		    logger = new SystemOutLogger();
		}		
	    }
	else
	    {
	        logger = new SystemOutLogger();				
	    }

	_embedded = new Embedded(logger, new MemoryRealm());
	_embedded.setDebug(0); // no debug
	_embedded.setLogger(logger);	
	
	_path = _pluginPath +tomcatPath;
	
	System.setProperty("catalina.home", _path);
	System.setProperty("catalina.base", _path);
    }

    public static TomcatContainer getDefault()
    {
	if(_container==null)
	    {
		_container= new TomcatContainer();
	    }
	return _container;
    }

    public void start(int port)
    {	
	if(isRunning)	
	    {
		return;
	    }
	
	try
	    {
		_embedded.start();
	    }
	catch(LifecycleException e)
	    {
		e.printStackTrace();
	    }
	
	_engine =_embedded.createEngine();
	_engine.setDefaultHost("localhost");

        _host = _embedded.createHost("localhost", _path + "/webapps");
        _engine.addChild(_host);

        Context root = _embedded.createContext("", _path + "/webapps/ROOT");
        _host.addChild(root);

	cdthelp =_embedded.createContext("/cdthelp",
					 _pluginPath+"org.eclipse.cdt.cpp.miners/help/helpwebapp");
	_host.addChild(cdthelp);

	_embedded.addEngine(_engine);
        
        Connector connector = _embedded.createConnector(null, port, false);
        _embedded.addConnector(connector);
	
	isRunning = true;
    }

    public void stop()
    {
	if(!isRunning)
	    {
		return;
	    }

	_embedded.removeContext(cdthelp);

        //remove engine also removes connector.
        _embedded.removeEngine(_engine);

        try 
	    {
		_embedded.stop();
		isRunning = false;
	    }
	catch (LifecycleException e) 
	    {
		e.printStackTrace();
	    }
     	
    }
    /*
    public void setPort(int port)
    {
	if(isRunning)
	    {
		stop();
		_port = port;
		start();
	    }
	else
	    {
		_port = port;
	    }
    }
    */
}
