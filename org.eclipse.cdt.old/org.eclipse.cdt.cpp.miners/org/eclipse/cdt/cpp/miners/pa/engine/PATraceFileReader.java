package org.eclipse.cdt.cpp.miners.pa.engine;

import java.io.*;

/**
 * A PAException is thrown when the PA engine encounters an error.
 */
public class PATraceFileReader implements ITraceReader {

 String _fileName;
 private BufferedReader _reader;
 
 /**
  * Create a new PA trace file reader from a given file name
  */
 public PATraceFileReader(String fileName) throws PAException {

   _fileName = fileName;
   try {
    _reader = new BufferedReader(new FileReader(fileName));
   }
   catch (FileNotFoundException e) {
    throw new PAException("Cannot find the trace file: " + fileName);
   }
   
 }
 
 /**
  * Create a new PA trace file reader from a given java file object
  */
 public PATraceFileReader(File file) throws PAException {

   _fileName = file.getAbsolutePath();
   try {
    _reader = new BufferedReader(new FileReader(file));
   }
   catch (FileNotFoundException e) {
    throw new PAException("Cannot find the trace file: " + file.getAbsolutePath());
   }
   
 }
 
 /**
  * Read in a text line from the file stream
  */
 public String readLine() throws PAException {
  
  String line = null;
  try {
   line = _reader.readLine();
  }
  catch (IOException e) {
   throw new PAException("Error reading the input file: " + _fileName);
  }
  
  return line;
 }
 
 /**
  * close the reader
  */
 public void close() {
 
  try { _reader.close(); } catch (IOException e) {}
  
 }
 
}