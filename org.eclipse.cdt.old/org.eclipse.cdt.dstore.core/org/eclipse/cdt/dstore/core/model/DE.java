package org.eclipse.cdt.dstore.core.model;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


/**
 * DE is a container of <code>DataElement</code> constants.  These constants
 * are used to identify <code>DataElement</code> attributes.
 * 
 * <li>
 * Attributes beginning with "P_" indicate <I>property</I> attribute identifiers.
 * </li>
 * <li>
 * Attributes beginning with "T_" indicate <code>DataElement</code> <I>type</I> attributes.
 * </li>
 * <li>
 * Attributes beginning with "A_" indicate <code>DataElement</code> indexs into <I>attributes</I>.
 * </li>
 *
 */
public class DE 	     
{
    /*
     * The nested data (children) property identifier of a <code>DataElement</code>.
     */
    public static final String P_CHILDREN = "children";

    /*
     * The image property identifier of a <code>DataElement</code>.  This is the same
     * as the value property identifier
     */
    public static final String P_LABEL = "label";

    /*
     * The notifier property identifier of a <code>DataElement</code>.
     */
    public static final String P_NOTIFIER = "notifier";

    /*
     * The <code>DataStore</code> property identifier of a <code>DataElement</code>.
     */
    public static final String P_DATASTORE       = "dataStore";

    /*
     * The source name property identifier of a <code>DataElement</code>.  This is the
     * name of a source location if one exists.
     */
    public static final String P_SOURCE_NAME     = "source";

    /*
     * The source file property identifier of a <code>DataElement</code>.
     */
    public static final String P_SOURCE          = "sourcefile";

    /*
     * The source location property identifier of a <code>DataElement</code>.
     */
    public static final String P_SOURCE_LOCATION = "sourcelocation";

    public static final String P_SOURCE_LOCATION_COLUMN = "sourcelocationcolumn";

    /*
     * The nested data (children) property identifier of a <code>DataElement</code>.  Same as <code>P_CHILDREN</code>.
     */
    public static final String P_NESTED          = "nested";

    /*
     * The buffer property identifier of a <code>DataElement</code>.  
     */
    public static final String P_BUFFER          = "buffer";

    /*
     * The type property identifier of a <code>DataElement</code>.  
     */
    public static final String P_TYPE            = "type";

    /*
     * The id property identifier of a <code>DataElement</code>.  
     */
    public static final String P_ID              = "id";

    /*
     * The name property identifier of a <code>DataElement</code>.  
     */
    public static final String P_NAME            = "name";

    /*
     * The value property identifier of a <code>DataElement</code>.  
     */
    public static final String P_VALUE           = "value";

    /*
     * The <I>is reference?</I> property identifier of a <code>DataElement</code>.  
     */
    public static final String P_ISREF           = "isRef";

    /*
     * The visibility property identifier of a <code>DataElement</code>.  
     */
    public static final String P_DEPTH           = "depth";

    /*
     * The attributes property identifier of a <code>DataElement</code>.  
     */
    public static final String P_ATTRIBUTES      = "attribute";

    /*
     * The file property identifier of a <code>DataElement</code>.  
     */
    public static final String P_FILE            = "file";

    /*
     * Reference type.  
     */
    public static final String T_REFERENCE          = "reference";

    /*
     * Command type.  
     */
    public static final String T_COMMAND            = "command";

    /*
     * UI Command Descriptor type.  
     */
    public static final String T_UI_COMMAND_DESCRIPTOR  = "ui_commanddescriptor";

    /*
     * Object Descriptor type.  
     */
    public static final String T_OBJECT_DESCRIPTOR  = "objectdescriptor";

    /*
     * Command Descriptor type.  
     */
    public static final String T_COMMAND_DESCRIPTOR = "commanddescriptor";

    /*
     * Relation Descriptor type.  
     */
    public static final String T_RELATION_DESCRIPTOR = "relationdescriptor";

    /*
     * Abstract Object Descriptor type.  
     */
    public static final String T_ABSTRACT_OBJECT_DESCRIPTOR = "abstractobjectdescriptor";

    /*
     * Abstract Command Descriptor type.  
     */
    public static final String T_ABSTRACT_COMMAND_DESCRIPTOR = "abstractcommanddescriptor";

    /*
     * Abstract Relation Descriptor type.  
     */
    public static final String T_ABSTRACT_RELATION_DESCRIPTOR = "abstractrelationdescriptor";


    /*
     * Type attribute index.  
     */
    public static final int    A_TYPE       = 0;

    /*
     * ID attribute index.  
     */
    public static final int    A_ID         = 1;

    /*
     * Name attribute index.  
     */
    public static final int    A_NAME       = 2;

    /*
     * Value attribute index.  
     */
    public static final int    A_VALUE      = 3;

    /*
     * Source attribute index.  
     */
    public static final int    A_SOURCE     = 4;

    /*
     * IsRef attribute index.  
     */
    public static final int    A_ISREF      = 5;

    /*
     * Visibility attribute index.  
     */
    public static final int    A_DEPTH      = 6;

    /*
     * Size attribute index.  
     */
    public static final int    A_SIZE       = 7;
}
