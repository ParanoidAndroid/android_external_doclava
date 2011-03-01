/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.doclava.apicheck;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;

import com.google.doclava.Errors;
import com.google.doclava.Errors.ErrorMessage;

public class ApiCheck {
  // parse out and consume the -whatever command line flags
  private static ArrayList<String[]> parseFlags(ArrayList<String> allArgs) {
    ArrayList<String[]> ret = new ArrayList<String[]>();

    int i;
    for (i = 0; i < allArgs.size(); i++) {
      // flags with one value attached
      String flag = allArgs.get(i);
      if (flag.equals("-error") || flag.equals("-warning") || flag.equals("-hide")) {
        String[] arg = new String[2];
        arg[0] = flag;
        arg[1] = allArgs.get(++i);
        ret.add(arg);
      } else {
        // we've consumed all of the -whatever args, so we're done
        break;
      }
    }

    // i now points to the first non-flag arg; strip what came before
    for (; i > 0; i--) {
      allArgs.remove(0);
    }
    return ret;
  }

  public static void main(String[] originalArgs) {
    ApiCheck acheck = new ApiCheck();
    Report report = acheck.checkApi(originalArgs);
   
    Errors.printErrors(report.errors());
    System.exit(report.code);
  }
  
  /**
   * Compares two api xml files for consistency.
   */
  public Report checkApi(String[] originalArgs) {
    // translate to an ArrayList<String> for munging
    ArrayList<String> args = new ArrayList<String>(originalArgs.length);
    for (String a : originalArgs) {
      args.add(a);
    }

    ArrayList<String[]> flags = ApiCheck.parseFlags(args);
    for (String[] a : flags) {
      if (a[0].equals("-error") || a[0].equals("-warning") || a[0].equals("-hide")) {
        try {
          int level = -1;
          if (a[0].equals("-error")) {
            level = Errors.ERROR;
          } else if (a[0].equals("-warning")) {
            level = Errors.WARNING;
          } else if (a[0].equals("-hide")) {
            level = Errors.HIDDEN;
          }
          Errors.setErrorLevel(Integer.parseInt(a[1]), level);
        } catch (NumberFormatException e) {
          System.err.println("Bad argument: " + a[0] + " " + a[1]);
          return new Report(2, Errors.getErrors());
        }
      }
    }

    ApiInfo oldApi;
    ApiInfo newApi;
    
    try {
      oldApi = parseApi(args.get(0));
      newApi = parseApi(args.get(1));
    } catch (ApiParseException e) {
      e.printStackTrace();
      System.err.println("Error parsing API");
      return new Report(1, Errors.getErrors());
    }

    // only run the consistency check if we haven't had XML parse errors
    if (!Errors.hadError) {
      oldApi.isConsistent(newApi);
    }

    return new Report(Errors.hadError ? 1 : 0, Errors.getErrors());
  }

  public ApiInfo parseApi(String xmlFile) throws ApiParseException {
    FileInputStream fileStream = null;
    try {
      fileStream = new FileInputStream(xmlFile);
      return XmlApiFile.parseApi(fileStream);
    } catch (IOException e) {
      throw new ApiParseException("Could not open file for parsing: " + xmlFile, e);
    } finally {
      if (fileStream != null) {
        try {
          fileStream.close();
        } catch (IOException ignored) {}
      }
    }
  }
  
  public ApiInfo parseApi(URL xmlURL) throws ApiParseException {
    InputStream xmlStream = null;
    try {
      xmlStream = xmlURL.openStream();
      return XmlApiFile.parseApi(xmlStream);
    } catch (IOException e) {
      throw new ApiParseException("Could not open stream for parsing: " + xmlURL,e);
    } finally {
      if (xmlStream != null) {
        try {
          xmlStream.close();
        } catch (IOException ignored) {}
      }
    }
  }
  
  
  public class Report {
    private int code;
    private Set<ErrorMessage> errors;
    
    private Report(int code, Set<ErrorMessage> errors) {
      this.code = code;
      this.errors = errors;
    }
    
    public int code() {
      return code;
    }
    
    public Set<ErrorMessage> errors() {
      return errors;
    }
  }
}
