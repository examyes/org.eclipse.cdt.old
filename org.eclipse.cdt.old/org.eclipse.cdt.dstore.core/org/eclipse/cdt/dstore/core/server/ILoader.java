package org.eclipse.cdt.dstore.core.server;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public interface ILoader
{
    public Miner loadMiner(String name);
}
