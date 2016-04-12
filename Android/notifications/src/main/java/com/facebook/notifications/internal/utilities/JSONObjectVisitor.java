// Copyright (c) 2016-present, Facebook, Inc. All rights reserved.
//
// You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
// copy, modify, and distribute this software in source code or binary form for use
// in connection with the web services and APIs provided by Facebook.
//
// As with any software that integrates with the Facebook platform, your use of
// this software is subject to the Facebook Developer Principles and Policies
// [http://developers.facebook.com/policy/]. This copyright notice shall be
// included in all copies or substantial portions of the software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.facebook.notifications.internal.utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public abstract class JSONObjectVisitor {
  public static void walk(JSONObject object, JSONObjectVisitor visitor) {
    visitor.visit(object);
  }

  public static void walk(JSONArray array, JSONObjectVisitor visitor) {
    visitor.visit(array);
  }

  protected void visit(JSONObject object) {
    for (Iterator<String> keys = object.keys(); keys.hasNext(); ) {
      String key = keys.next();
      Object value = object.opt(key);

      if (value instanceof JSONObject) {
        visit((JSONObject) value);
      }

      if (value instanceof JSONArray) {
        visit((JSONArray) value);
      }
    }
  }

  protected void visit(JSONArray array) {
    for (int index = 0; index < array.length(); index++) {
      Object value = array.opt(index);

      if (value instanceof JSONObject) {
        visit((JSONObject) value);
      }

      if (value instanceof JSONArray) {
        visit((JSONArray) value);
      }
    }
  }
}
