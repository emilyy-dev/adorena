package ar.emily.adorena.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "version")
@JsonSubTypes(@JsonSubTypes.Type(ConfigurationV1.class))
public interface AbstractConfiguration {

  // TODO: separate to a Converter instead?
  // TODO: if config ever needs migrating, figure out how to emit comments to the generated yaml
  ConfigurationV1 asLatest();
}
