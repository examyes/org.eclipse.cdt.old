package com.ibm.dstore.core.server;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public interface ILoader
{
    public Miner loadMiner(String name);
}
