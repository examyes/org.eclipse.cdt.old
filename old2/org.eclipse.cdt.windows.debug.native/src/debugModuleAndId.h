/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/
#ifndef DEBUGMODULEANDID_H_
#define DEBUGMODULEANDID_H_

jobject createObject(JNIEnv * env, DEBUG_MODULE_AND_ID & mid);

void getObject(JNIEnv * env, jobject obj, DEBUG_MODULE_AND_ID & mid);

#endif /*DEBUGMODULEANDID_H_*/
