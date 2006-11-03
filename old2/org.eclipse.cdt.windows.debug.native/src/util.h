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
#ifndef UTIL_H_
#define UTIL_H_

wchar_t * getString(JNIEnv * env, jstring string);

// Throws a NullPointerException if the ptr is NULL
void checkNull(JNIEnv * env, void * ptr);

// Set the string in a IDebugString object
void setObject(JNIEnv * env, jobject obj, wchar_t * string);

// Set the long in a IDebugLong object
void setObject(JNIEnv * env, jobject obj, jlong l);

// Set the int in a IDebugInt object
void setObject(JNIEnv * env, jobject obj, jint i);

// Set the int array in a IDebugIntArray object
void setObject(JNIEnv * env, jobject obj, jint * i, int count);

// Create a DebugStackFrame object
jobject createObject(JNIEnv * env, DEBUG_STACK_FRAME & frame);

#endif /*UTIL_H_*/
