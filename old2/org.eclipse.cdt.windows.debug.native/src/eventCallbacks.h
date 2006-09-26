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
#ifndef EVENTCALLBACKS_H_
#define EVENTCALLBACKS_H_

class EventCallbacks : public DebugBaseEventCallbacksWide {
public:
	EventCallbacks();
	virtual ~EventCallbacks();
};

#endif /*EVENTCALLBACKS_H_*/
