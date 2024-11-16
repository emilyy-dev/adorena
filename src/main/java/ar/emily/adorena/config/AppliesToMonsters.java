package ar.emily.adorena.config;

import ar.emily.adorena.jackson.KebabCaseEnumNamingStrategy;
import com.fasterxml.jackson.databind.annotation.EnumNaming;

@EnumNaming(KebabCaseEnumNamingStrategy.class)
public enum AppliesToMonsters {
  ALWAYS,
  PLAYER_KILLS_ONLY,
  NEVER,
}
