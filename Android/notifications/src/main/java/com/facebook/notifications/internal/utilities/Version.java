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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Simple version class
 */
public class Version implements Comparable<Version> {
  private final int major;
  private final int minor;
  private final int patch;

  public Version(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  @Nullable
  public static Version parse(String input) {
    Scanner scanner = new Scanner(input);
    scanner.useDelimiter("\\.");

    int major = Integer.MIN_VALUE, minor = Integer.MIN_VALUE, patch = 0;

    try {
      major = scanner.nextInt();
      minor = scanner.nextInt();
      patch = scanner.nextInt();
    } catch (NoSuchElementException ex) {
      if (major == Integer.MIN_VALUE && minor == Integer.MIN_VALUE) {
        return null;
      }
    }

    return new Version(major, minor, patch);
  }

  public int getPatch() {
    return patch;
  }

  public int getMinor() {
    return minor;
  }

  public int getMajor() {
    return major;
  }

  @Override
  public String toString() {
    return "Version{" + major + "." + minor + "." + patch + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Version version = (Version) o;

    return
      major == version.major &&
        minor == version.minor &&
        patch == version.patch;
  }

  @Override
  public int hashCode() {
    int result = major;
    result = 31 * result + minor;
    result = 31 * result + patch;
    return result;
  }

  @Override
  public int compareTo(@NonNull Version another) {
    int major = this.major - another.major;
    int minor = this.minor - another.minor;
    int patch = this.patch - another.patch;

    return major != 0 ? major :
      minor != 0 ? minor :
        patch;
  }
}
