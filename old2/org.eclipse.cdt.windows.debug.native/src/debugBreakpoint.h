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
#ifndef DEBUGBREAKPOINT_H_
#define DEBUGBREAKPOINT_H_

void setObject(JNIEnv * env, jobject obj, IDebugBreakpoint2 * bp);

jobject createObject(JNIEnv * env, IDebugBreakpoint2 * bp);

#endif /*DEBUGBREAKPOINT_H_*/