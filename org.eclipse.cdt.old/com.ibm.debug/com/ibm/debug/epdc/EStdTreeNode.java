package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdTreeNode.java, java-epdc, eclipse-dev, 20011129
// Version 1.11.1.3 (last modified 11/29/01 14:15:32)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.Vector;

/**
 * EStdTreeNode structure.  Currently this only supports one node trees
 */
public class EStdTreeNode extends EPDC_Base {

   /**
    * Create a new EStdTreeNode object
    */
   public EStdTreeNode(int nodeID, EStdGenericNode nodeData) {
      _nodeID = nodeID;
      _nodeData = nodeData;
      _offsetThisNode =0;
      _offsetParent = 0;
      _offsetPrevSibling = 0;
      _offsetNextSibling = 0;
      _level = (short) 0;
      _numChild = (short) 1;
      _children = new Vector();
   }

   public EStdTreeNode(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     _nodeID = dataInputStream.readInt();
     _level = dataInputStream.readShort();
     _numChild = dataInputStream.readShort();

     short numberOfChildren;
     if ((numberOfChildren = dataInputStream.readShort()) > 0)
         _children = new Vector(numberOfChildren);

     dataInputStream.readShort();   // reserved
     _offsetParent = dataInputStream.readInt();

     // read the first child and all of its siblings
     int firstChildOffset;
     EStdTreeNode childNode;

     if ((firstChildOffset = dataInputStream.readInt()) != 0)
     {
         childNode = new EStdTreeNode(packetBuffer,
                                  new OffsetDataInputStream(packetBuffer,
                                                            firstChildOffset));
         _children.addElement(childNode);

         int nextSiblingOffset;
         while ((nextSiblingOffset = childNode.getNextSiblingOffset()) != 0)
         {
                childNode = new EStdTreeNode(packetBuffer,
                                     new OffsetDataInputStream(packetBuffer,
                                                            nextSiblingOffset));
                _children.addElement(childNode);
         }
     }

     dataInputStream.readInt();   // reading LastChild

     _offsetPrevSibling = dataInputStream.readInt();
     _offsetNextSibling = dataInputStream.readInt();

     dataInputStream.readInt();   // reading offset to workSlot
     int offset = dataInputStream.readInt();   // reading offset to nodeData

     _nodeData = EStdGenericNode.decodeEStdGenericNodeStream(packetBuffer,
                              new OffsetDataInputStream(packetBuffer, offset));
   }

   public EStdGenericNode getTreeNodeData()
   {
      return _nodeData;
   }

   public int getID()
   {
     return _nodeID;
   }

   public Vector children()
   {
     if ((_children == null) || (_children.size() == 0))
         return null;

     return _children;
   }

   /**
    * Add a child node
    */
   public void addChildNode(EStdTreeNode childNode, int childNum) {
      _children.addElement(childNode);
      childNode.setLevel((short) (_level + 1) );
      childNode.setNumChild((short) childNum);
   }

   /**
    * Set the offset of this tree node -- this must be done to calculate the offsets of
    * child nodes and to be able to set the children nodes' offsets to their parent
    */
   protected void setOffsetThisNode(int offsetThisNode) {
      _offsetThisNode  = offsetThisNode;
   }

   /**
    * Set the offset to the parent node
    */
   protected void setOffsetParent(int offsetParent) {
      _offsetParent = offsetParent;
   }

   /**
    * Set the offset to the previous sibling
    */
   protected void setOffsetPrevSibling(int offsetPrevSibling) {
      _offsetPrevSibling = offsetPrevSibling;
   }

   /**
    * Set the offset to the next sibling
    */
   protected void setOffsetNextSibling(int offsetNextSibling) {
      _offsetNextSibling = offsetNextSibling;
   }

   /**
    * Set the level of this node.  The root node is at level 0.
    */
   protected void setLevel(short level) {
      _level = level;
   }

   /**
    * Set the child number of this node
    */
   protected void setNumChild(short numChild) {
      _numChild = numChild;
   }


   /**
    * Return the child number
    */

   public int getNumChild()
   {
      return _numChild;
   }


   /**
    * Get the offset of the next sibling
    */
   protected int getNextSiblingOffset()
   {
     return _offsetNextSibling;
   }

   /**
    * Return length of fixed component
    */
   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Static function that returns length of fixed component
    */
   protected int _fixedLen() {
      return _fixed_length;
   }

   /**
    * Return length of variable component.  This includes the node data and the fixed
    * and variable components of all of this node's children.
    */
   protected int varLen() {
      // dtermine node data size
      int total = _nodeData.fixedLen() + _nodeData.varLen();

      // recursively add size of all children
      for (int i=0; i<_children.size(); i++) {
         EStdTreeNode child = (EStdTreeNode) _children.elementAt(i);
         total += child.fixedLen() + child.varLen();
      }
      return total;
   }

   /**
    * Output class to data streams according to EPDC protocol.
    * The tree is streamed by doing a preorder traversal.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      EStdTreeNode child = null;

      int total = _fixed_length;

      // create two variable data streams, like in ERepInitializeDE and other packets
      int offset1 = baseOffset;
      int offset2 = baseOffset + _nodeData.fixedLen();
      ByteArrayOutputStream varBos1 = new ByteArrayOutputStream();
      ByteArrayOutputStream varBos2 = new ByteArrayOutputStream();
      DataOutputStream varDos1 = new DataOutputStream(varBos1);
      DataOutputStream varDos2 = new DataOutputStream(varBos2);

      // write out node information

      writeInt(fixedData, _nodeID);
      writeShort(fixedData, _level);         // number of nodes from root
      writeShort(fixedData, _numChild);      // nth sibling node
      writeShort(fixedData, (short) _children.size());      // number of immediate children
      writeShort(fixedData, (short) 0);      // reserved

      // calculate offsets of children
      int offsetFirstChild = 0, offsetLastChild = 0;
      if (_children.size() > 0) {
         offsetFirstChild = offset2 + _nodeData.varLen();

         offsetLastChild = offsetFirstChild;
         for (int i=0; i<_children.size()-1; i++) {
            child = (EStdTreeNode) _children.elementAt(i);
            offsetLastChild += child.fixedLen() + child.varLen();
         }
      }

      // now write out offset information
      writeOffset(fixedData, _offsetParent);             // to parent node
      writeOffset(fixedData, offsetFirstChild);          // to 1st child node
      writeOffset(fixedData, offsetLastChild);           // to last child node
      writeOffset(fixedData, _offsetPrevSibling);        // to previous sibling
      writeOffset(fixedData, _offsetNextSibling);        // to next sibling
      writeOffset(fixedData, 0);             // to workspace for back/front end

      writeOffset(fixedData, offset1);
      total += _nodeData.toDataStreams(varDos1, varDos2, offset2);

      varData.write(varBos1.toByteArray());
      varData.write(varBos2.toByteArray());

      // now, we need to write out the children of this node
      int childOffset = offsetFirstChild;
      int childVariableOffset, childPrevOffset, childNextOffset;
      childVariableOffset = childPrevOffset = childNextOffset = 0;

      for (int i=0; i<_children.size(); i++) {
         child = (EStdTreeNode) _children.elementAt(i);
         childVariableOffset = childOffset + child.fixedLen();

         // set the offset to the child's next sibling by adding the child's variable component
         // length to the child's variable component offest.  If the child is the last child
         // then the next sibling's offset should be 0
         childNextOffset = (i == _children.size()-1) ? 0 : childVariableOffset + child.varLen();

         child.setOffsetThisNode(childOffset);
         child.setOffsetParent(_offsetThisNode);
         child.setOffsetPrevSibling(childPrevOffset);
         child.setOffsetNextSibling(childNextOffset);

         // create a stream for the child's variable component
         ByteArrayOutputStream childVarBos = new ByteArrayOutputStream();
         DataOutputStream childVarDos = new DataOutputStream(childVarBos);

         // now write out the child (and recursively its children) to streams.  The child's fixed component
         // stream is varData, and it's variable component stream is childVarDos
         total += child.toDataStreams(varData, childVarDos, childVariableOffset);

         // append child's variable stream to this node's variable stream
         varData.write(childVarBos.toByteArray());

         childPrevOffset = childOffset;
         childOffset = childNextOffset;
      }

      return total;
   }

   // data fields
   private int _nodeID;
   private short _level;                  // level of this node
   private short _numChild;               // which child this node is
   private EStdGenericNode _nodeData;
   private Vector _children;           // vector of children of this node
   private int _offsetThisNode;        // this node's offset
   private int _offsetParent;          // offset of parent sibling
   private int _offsetPrevSibling;     // offset of previous silbing
   private int _offsetNextSibling;     // offset of next sibling

   private static final int _fixed_length = 40;
}
