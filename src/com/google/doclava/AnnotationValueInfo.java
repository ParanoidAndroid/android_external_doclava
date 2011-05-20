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

package com.google.doclava;

import java.util.ArrayList;

public class AnnotationValueInfo implements Resolvable {
  private Object mValue;
  private MethodInfo mElement;
  private ArrayList<Resolution> mResolutions;

  public AnnotationValueInfo() {
  }

  public AnnotationValueInfo(MethodInfo element) {
      mElement = element;
    }

  public void init(Object value) {
    mValue = value;
  }

  public MethodInfo element() {
    return mElement;
  }

  public void setElement(MethodInfo element) {
      mElement = element;
  }

  public Object value() {
    return mValue;
  }

  public String valueString() {
    Object v = mValue;
    if (v instanceof TypeInfo) {
      return ((TypeInfo) v).fullName();
    } else if (v instanceof FieldInfo) {
      StringBuilder str = new StringBuilder();
      FieldInfo f = (FieldInfo) v;
      str.append(f.containingClass().qualifiedName());
      str.append('.');
      str.append(f.name());
      return str.toString();
    } else if (v instanceof AnnotationInstanceInfo) {
      return v.toString();
    } else if (v instanceof ArrayList<?>) {
      StringBuilder str = new StringBuilder();

      @SuppressWarnings("unchecked")
      ArrayList<AnnotationValueInfo> values = (ArrayList<AnnotationValueInfo>) v;

      str.append("{");
      for (AnnotationValueInfo info : values) {
          str.append(info.valueString());
          if (info != values.get(values.size()-1)) {
            str.append(",");
          }
      }
      str.append("}");
      return str.toString();
    } else {
      return FieldInfo.constantLiteralValue(v);
    }
  }

  public void addResolution(Resolution resolution) {
      if (mResolutions == null) {
          mResolutions = new ArrayList<Resolution>();
      }

      mResolutions.add(resolution);
  }

  public void printResolutions() {
      System.out.println("Resolutions for Annotation Value:");
      for (Resolution r : mResolutions) {
          System.out.println(r);
      }
  }
}
