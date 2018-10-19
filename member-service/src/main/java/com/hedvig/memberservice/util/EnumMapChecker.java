package com.hedvig.memberservice.util;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class EnumMapChecker {
  public static <T extends Enum<T>> void ensureMapContainsAllEnumVals(EnumMap<T, ?> m, Class<T> enumClazz) {
    List<T> missingValues = Arrays.stream(enumClazz.getEnumConstants())
      .filter(e -> !m.containsKey(e))
      .collect(Collectors.toList());

    if (!missingValues.isEmpty()) {
      throw new RuntimeException("EnumMap missing values: " + missingValues);
    }
  }
}
