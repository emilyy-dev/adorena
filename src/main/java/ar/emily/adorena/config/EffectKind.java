package ar.emily.adorena.config;

import ar.emily.adorena.jackson.KebabCaseEnumNamingStrategy;
import com.fasterxml.jackson.databind.annotation.EnumNaming;

@EnumNaming(KebabCaseEnumNamingStrategy.class)
public enum EffectKind {
  GROW, SHRINK, NONE
}
