package ar.emily.adorena;

import ar.emily.adorena.config.ReloadableConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;

public final class PluginConstants {

  public static final String STYLIZED_NAME;
  public static final String VERSION;

  static {
    try {
      final var info = ReloadableConfiguration.YAML_MAPPER.readValue(Info.class.getResource("/plugin.yml"), Info.class);
      STYLIZED_NAME = info.prefix;
      VERSION = info.version;
    } catch (final IOException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record Info(String prefix, String version) {
  }
}
