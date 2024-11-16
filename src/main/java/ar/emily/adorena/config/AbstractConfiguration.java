package ar.emily.adorena.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "version")
@JsonSubTypes(@JsonSubTypes.Type(ConfigurationV1.class))
public interface AbstractConfiguration {

  ConfigurationV1 asLatest();
}
