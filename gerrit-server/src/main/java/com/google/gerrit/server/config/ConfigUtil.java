// Copyright (C) 2009 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.server.config;

import static org.eclipse.jgit.util.StringUtils.equalsIgnoreCase;

import org.eclipse.jgit.lib.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

public class ConfigUtil {
  /**
   * Parse a Java enumeration from the configuration.
   *
   * @param <T> type of the enumeration object.
   * @param config the configuration file to read.
   * @param section section the key is in.
   * @param subsection subsection the key is in, or null if not in a subsection.
   * @param setting name of the setting to read.
   * @param defaultValue default value to return if the setting was not set.
   *        Must not be null as the enumeration values are derived from this.
   * @return the selected enumeration value, or {@code defaultValue}.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Enum<?>> T getEnum(final Config config,
      final String section, final String subsection, final String setting,
      final T defaultValue) {
    final T[] all;
    try {
      all = (T[]) defaultValue.getClass().getMethod("values").invoke(null);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Cannot obtain enumeration values", e);
    } catch (SecurityException e) {
      throw new IllegalArgumentException("Cannot obtain enumeration values", e);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Cannot obtain enumeration values", e);
    } catch (InvocationTargetException e) {
      throw new IllegalArgumentException("Cannot obtain enumeration values", e);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("Cannot obtain enumeration values", e);
    }
    return getEnum(config, section, subsection, setting, all, defaultValue);
  }

  /**
   * Parse a Java enumeration from the configuration.
   *
   * @param <T> type of the enumeration object.
   * @param config the configuration file to read.
   * @param section section the key is in.
   * @param subsection subsection the key is in, or null if not in a subsection.
   * @param setting name of the setting to read.
   * @param all all possible values in the enumeration which should be
   *        recognized. This should be {@code EnumType.values()}.
   * @param defaultValue default value to return if the setting was not set.
   *        This value may be null.
   * @return the selected enumeration value, or {@code defaultValue}.
   */
  public static <T extends Enum<?>> T getEnum(final Config config,
      final String section, final String subsection, final String setting,
      final T[] all, final T defaultValue) {
    final String valueString = config.getString(section, subsection, setting);
    if (valueString == null) {
      return defaultValue;
    }

    String n = valueString.replace(' ', '_');
    for (final T e : all) {
      if (equalsIgnoreCase(e.name(), n)) {
        return e;
      }
    }

    final StringBuilder r = new StringBuilder();
    r.append("Value \"");
    r.append(valueString);
    r.append("\" not recognized in ");
    r.append(section);
    if (subsection != null) {
      r.append(".");
      r.append(subsection);
    }
    r.append(".");
    r.append(setting);
    r.append("; supported values are: ");
    for (final T e : all) {
      r.append(e.name());
      r.append(" ");
    }
    throw new IllegalArgumentException(r.toString().trim());
  }

  /**
   * Parse a numerical time unit, such as "1 minute", from the configuration.
   *
   * @param config the configuration file to read.
   * @param section section the key is in.
   * @param subsection subsection the key is in, or null if not in a subsection.
   * @param setting name of the setting to read.
   * @param defaultValue default value to return if no value was set in the
   *        configuration file.
   * @param wantUnit the units of {@code defaultValue} and the return value, as
   *        well as the units to assume if the value does not contain an
   *        indication of the units.
   * @return the setting, or {@code defaultValue} if not set, expressed in
   *         {@code units}.
   */
  public static long getTimeUnit(final Config config, final String section,
      final String subsection, final String setting, final long defaultValue,
      final TimeUnit wantUnit) {
    final String valueString = config.getString(section, subsection, setting);
    if (valueString == null) {
      return defaultValue;
    }

    String s = valueString.trim();
    if (s.length() == 0) {
      return defaultValue;
    }

    final String unitName;
    final int sp = s.indexOf(' ');
    if (sp > 0) {
      unitName = s.substring(sp + 1).trim();
      s = s.substring(0, sp);
    } else {
      final char last = s.charAt(s.length() - 1);
      if ('0' <= last && last <= '9') {
        unitName = "";
      } else {
        unitName = String.valueOf(last);
        s = s.substring(0, s.length() - 1).trim();
      }
    }
    if (s.length() == 0) {
      return defaultValue;
    }

    TimeUnit inputUnit;
    int inputMul;

    if ("".equals(unitName)) {
      inputUnit = wantUnit;
      inputMul = 1;

    } else if (match(unitName, "s", "sec", "second", "seconds")) {
      inputUnit = TimeUnit.SECONDS;
      inputMul = 1;

    } else if (match(unitName, "m", "min", "minute", "minutes")) {
      inputUnit = TimeUnit.MINUTES;
      inputMul = 1;

    } else if (match(unitName, "h", "hr", "hour", "hours")) {
      inputUnit = TimeUnit.HOURS;
      inputMul = 1;

    } else if (match(unitName, "d", "day", "days")) {
      inputUnit = TimeUnit.DAYS;
      inputMul = 1;

    } else if (match(unitName, "w", "week", "weeks")) {
      inputUnit = TimeUnit.DAYS;
      inputMul = 7;

    } else if (match(unitName, "mon", "month", "months")) {
      inputUnit = TimeUnit.DAYS;
      inputMul = 30;

    } else if (match(unitName, "y", "year", "years")) {
      inputUnit = TimeUnit.DAYS;
      inputMul = 365;

    } else {
      throw notTimeUnit(section, subsection, setting, valueString);
    }

    try {
      return wantUnit.convert(Long.parseLong(s) * inputMul, inputUnit);
    } catch (NumberFormatException nfe) {
      throw notTimeUnit(section, subsection, setting, valueString);
    }
  }

  private static boolean match(final String a, final String... cases) {
    for (final String b : cases) {
      if (equalsIgnoreCase(a, b)) {
        return true;
      }
    }
    return false;
  }

  private static IllegalArgumentException notTimeUnit(final String section,
      final String subsection, final String setting, final String valueString) {
    return new IllegalArgumentException("Invalid time unit value: " + section
        + (subsection != null ? "." + subsection : "") + "." + setting + " = "
        + valueString);
  }

  private ConfigUtil() {
  }
}