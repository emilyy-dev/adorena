package ar.emily.adorena.config;

public interface BasicEffectSettings {

  EffectKind effect();
  int maximumTimes();
  long cooldownTicks();
}
