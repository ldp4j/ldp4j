package org.ldp4j.application.entity.spi;

import org.ldp4j.application.entity.DataSource;

public interface DataSourceFactory<T extends DataSource> extends DataService {

	T createDataSource();

}
