package com.ibm.dstore.ui.widgets;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*; 
import com.ibm.dstore.ui.IActionLoader; 
import com.ibm.dstore.ui.ConvertUtility;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.connections.*;

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

import java.util.*;
import java.io.*;

import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;

import org.eclipse.jface.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;

import org.eclipse.jface.resource.*; 
import org.eclipse.swt.graphics.*;

import java.lang.reflect.*;

public class TestUI extends ApplicationWindow
{		
  private static DataStore           _dataStore;
  private ClientConnection    _client;

  private Composite           _window;
  private Composite           _canvas;
    private TabFolder         _folder;

  private WindowManager       _manager;
  private static ImageRegistry       _imageRegistry;
  private DataElement         _input;
  private static DomainNotifier      _notifier;

  private MenuManager         _conPopup;
  private ConnectionManager   _connectionManager;

  public class QuitAction extends Action
  {
    public QuitAction(String label)
    {
      super(label);
    }

    public void run()
    {
      disconnect();
      System.exit(0);
    }
  }
  public class DisconnectAction extends Action
  {
    public DisconnectAction(String label)
    {
      super(label);
    }

    public void run()
    {
      disconnect();
    }
  }

  public class ExitAction extends Action
  {
    public ExitAction(String label)
    {
      super(label);
    }

    public void run()
    {
      disconnect();
      _connectionManager.writeConnections();
      System.exit(0);
    }
  }

  public static class NavigationPage implements IDomainListener, IActionLoader
  {
    private TestUI              _ui;
    private TabFolder           _folder;
    private ObjectWindow        _firstViewer;
    private ObjectWindow        _secondViewer;
    private EditorCanvas        _editorCanvas;
    private Composite           _pageCanvas;
    
    private Sash                _hSplitter;
    private Sash                _vSplitter;
    private double              _hSplitRatio;
    private double              _vSplitRatio;

    private DataElement         _root;
      private IOpenAction            _openAction;

    private class MyLayout extends Layout 
    {	
      public MyLayout()
          {
            super();
          }
      
      public Point computeSize(Composite c, int w, int h, boolean force) 
          {
            return new Point(100, 100);
          }
      
      public void layout(Composite composite, boolean force) 
          {      
            Rectangle r= composite.getClientArea();
            
            int sashSize = 5;
            
            int width  = r.width;
            int width1 = (int)(width * _vSplitRatio);
            int width2 = width - width1;
          
            int height = r.height;
            int height1 = (int)(height * _hSplitRatio);
            int height2 = height - height1;
            
            int x1 = r.x;
            
            int y1 = r.y;
            int y2 = y1 + height1;
            int y3 = y2 + sashSize;
            
            
            _firstViewer .setBounds(x1, y1, width1, height1);
            _hSplitter   .setBounds(x1, y2, width1, sashSize); 
            _secondViewer.setBounds(x1, y3, width1, height2);
            _vSplitter   .setBounds(x1 + width1, y1, sashSize, height); 
            _editorCanvas.setBounds(x1 + width1 + sashSize, y1, width2, height);
          }
    }


    public NavigationPage(TabFolder folder, TestUI ui)
        {
          _ui = ui;
          _folder = folder;

          _hSplitRatio = 0.5;
          _vSplitRatio = 0.99;
        }

    public Shell getShell()
    {
      return _pageCanvas.getShell();
      
    }
    
    public void dispose()
    {
      _notifier.removeDomainListener(this);      
    }
    
      public IOpenAction getOpenAction()
      {
	  if (_openAction == null)
	      {
		  _openAction = new OpenEditorAction(null);
	      }
	  return _openAction;
      }

    public CustomAction loadAction(String source, String name)
      {
	  CustomAction newAction = null;
	  try
	      {
		  Object[] args = { name};
		  Class actionClass = Class.forName(source);
		  Constructor constructor = actionClass.getConstructors()[0];
		  newAction = (CustomAction)constructor.newInstance(args);
	      }
	  catch (ClassNotFoundException e)
	      {
	      }
	  catch (InstantiationException e)
	      { 
	      }
	  catch (IllegalAccessException e)
	      {
	      }
	  catch (InvocationTargetException e)
	      {
	      }
	  
        return newAction;
      }

    public CustomAction loadAction(java.util.List objects, DataElement descriptor)
      {
	  return loadAction((DataElement)objects.get(0), descriptor);
      }

    public CustomAction loadAction(DataElement object, DataElement descriptor)
      {
        String name = descriptor.getName();
        String source = descriptor.getSource();
        
        CustomAction newAction = null;
        try
        {         
          Object[] args = {object, name, descriptor, object.getDataStore()};
          Class actionClass = Class.forName(source);
          Constructor constructor = actionClass.getConstructors()[0];
          newAction = (CustomAction)constructor.newInstance(args);
        }
        catch (ClassNotFoundException e)
        {
        }
        catch (InstantiationException e)
        { 
        }
        catch (IllegalAccessException e)
        {
        }
        catch (InvocationTargetException e)
        {
        }

        return newAction;
      }

    public void setInput(DataElement object)
        {
          _root = object;
          _firstViewer.setInput(object);
	  _secondViewer.setInput(object);
	  
	  /***
          TabItem tab = getTabItem();
          tab.setText(object.getName());
	  */
        }
    
    public void resetView()
        {
          _firstViewer.resetView();
          _secondViewer.resetView();
        }

      public boolean listeningTo(DomainEvent ev)
      {
	  return false;
      }

    public void domainChanged(DomainEvent ev)
      {
        DataElement parent = (DataElement)ev.getParent();   

        if (parent == _root)
        {
        }
      }

    public String getSchemaPath()
        {
          return _ui.getSchemaPath();
        }
    
    public double getVSplitRatio()
        {
          return _vSplitRatio;
        }

    public double getHSplitRatio()
        {
          return _hSplitRatio;
        }

    public void setVSplitRatio(double ratio)
      {
        _vSplitRatio = ratio;
        _pageCanvas.layout();
      }

    public void setHSplitRatio(double ratio)
      {
        _hSplitRatio = ratio;
        _pageCanvas.layout();
      }

    
    protected Control getControl (Composite parent)
        {
          _pageCanvas = new Composite(parent, SWT.NONE);

          _firstViewer = new ObjectWindow(_pageCanvas, SWT.NONE, _dataStore, _imageRegistry, this);
          _firstViewer.setWorkbook(_ui);
	  
          _hSplitter = new Sash(_pageCanvas, SWT.HORIZONTAL);
          _hSplitter.addSelectionListener (new SelectionAdapter () 
                                           {
                                             public void widgetSelected (SelectionEvent event) 
                                                 {
                                                   if (event.detail != SWT.DRAG) 
                                                   {
                                                     Rectangle r= _pageCanvas.getClientArea();
                                                     int rH = r.height;
                                                     int eY = event.y;
                                                     
                                                     setHSplitRatio((double)eY / (double)rH); 
                                                     
                                                   }
                                                 }
                                           });
          
          _secondViewer = new ObjectWindow(_pageCanvas, SWT.NONE, _dataStore, _imageRegistry, this);
	  _secondViewer.setWorkbook(_ui);

          _firstViewer.linkTo(_secondViewer);
          
          _vSplitter = new Sash(_pageCanvas, SWT.VERTICAL);
          _vSplitter.addSelectionListener (new SelectionAdapter () 
                                           {
                                             public void widgetSelected (SelectionEvent event) 
                                                 {
                                                   if (event.detail != SWT.DRAG) 
                                                   {
                                                     Rectangle r= _pageCanvas.getClientArea();
                                                     int rW = r.width;
                                                     int eX = event.x;
                                                     
                                                     setVSplitRatio((double)eX / (double)rW); 
                                                   }
                                                 }
                                           });
          
          _editorCanvas = new EditorCanvas(_pageCanvas, SWT.BORDER, this);
          
          _firstViewer.linkTo(_editorCanvas);
          _secondViewer.linkTo(_editorCanvas);
          
          _pageCanvas.setLayout(new MyLayout());
          return _pageCanvas;
        } 
  }
  
  public DomainNotifier getNotifier()
      {
        return _notifier;
      }

  public ImageRegistry getImageRegistry()
      {
        return _imageRegistry;
      }
  
  public DataStore getDataStore()
      {
        return _dataStore;
      }
  
  public ClientConnection getClient()
      {  
        return _client;    
      }
  
  
  public TestUI()
  {
    super(null);
    
    _client     = new ClientConnection("DataStore");
    initialize();
  }

  public TestUI(ClientConnection client, Composite parent)
  {
    super(null);

    _window = parent;
    _client = client;
    initialize();
  }

  private void initialize()
  {
    addMenuBar();

    setBlockOnOpen(true);
    _manager = new WindowManager();
    _manager.add(this);

    _dataStore  = _client.getDataStore();
    _notifier = _dataStore.getDomainNotifier();

    _client.localConnect();
    _notifier.enable(true);
    _notifier.setShell(getShell());

    // for remove connections
    _connectionManager = new ConnectionManager(_dataStore.getRoot(), _notifier);
    _connectionManager.readConnections();

    createMenuBar();
  }

  private void createMenuBar() 
      {
	// Get main menu.
	MenuManager menubar = getMenuBarManager();

	// Create the file menu.
	MenuManager popup = new MenuManager("&Connect", "Connect");
	menubar.add(popup);
	 
	popup.add(new ConnectionManager.NewConnectionAction("&New Connection"));
	popup.add(new Separator());
	popup.add(new ExitAction("&Exit"));
      }

  public String getSchemaPath()
  {
    String path = _dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH);    
    return path; 
  }

  public int open()
  {
      return super.open();
  }

  protected void dispose() 
      {
        _client.disconnect();
      }


  public void closeSections()
  {
    TabFolder folder = _folder;
    for (int i = folder.getItemCount() - 1; i > 0; i--)
      {	
	TabItem item = folder.getItem(i);
	if (item != null)
	  {	   
	    NavigationPage page = (NavigationPage)item.getData();	    
	    page.dispose();   
	  }	
      } 
  }
  
  public NavigationPage openSection(DataElement root)
      {
        NavigationPage page = new NavigationPage(_folder, this);
	
	TabItem item= new TabItem(_folder, SWT.NONE);
	item.setText(root.getName());

	String imageStr = (String)root.getElementProperty(DE.P_IMAGE);
	Image image = ImageDescriptor.createFromFile(null, imageStr).createImage();
       	item.setImage(image);
	item.setData(page);	
	Control control = page.getControl(_folder);	
	item.setControl(control);
        page.setInput(root);       
	control.setVisible(true);
	control.setFocus();
	

        _notifier.addDomainListener(page);
        return page;
      }

  protected Control createContents(Composite parent) 
      {
	  _canvas = parent;
	  _folder= new TabFolder(_canvas, SWT.NONE);	
	  _folder.setLayoutData(new FillLayout());
	  
	  _imageRegistry = new ImageRegistry();
	  openSection(_dataStore.getRoot());
	  	  
        return _canvas;
      }

  public void disconnect()
      {
        _connectionManager.disconnectAll();
        _client.disconnect();        
	_dataStore.flush();	

	closeSections();
	getContents().redraw();
      }
}
