package ar.emily.adorena.jackson;

import com.fasterxml.jackson.databind.EnumNamingStrategy;

import java.util.Locale;

public final class KebabCaseEnumNamingStrategy implements EnumNamingStrategy {

  @Override
  public String convertEnumToExternalName(final String enumName) {
    return enumName.toLowerCase(Locale.ROOT).replace('_', '-');
  }
}
