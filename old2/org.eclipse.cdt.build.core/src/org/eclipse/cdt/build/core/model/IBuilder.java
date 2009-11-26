/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.model;

/**
 * @author Doug Schaefer
 * 
 * A builder is a system that managed the build. Traditionally this has been a program like 'make'.
 * But there are many such builders, including the CDT internal builder.
 * 
 * This is modeled explicitly to allow users to select the builder to use for a given configuration.
 */
public interface IBuilder {

}
