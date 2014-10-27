package org.ldp4j.application.entity.spi;

import org.ldp4j.application.data.Name;

public interface NameGenerator<T> {

	Name<T> nextName();

}