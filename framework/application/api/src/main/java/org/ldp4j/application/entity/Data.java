package org.ldp4j.application.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentMap;

import org.ldp4j.application.config.Configuration;
import org.ldp4j.application.config.ConfigurationException;
import org.ldp4j.application.config.util.Configurator;
import org.ldp4j.application.entity.spi.DataService;
import org.ldp4j.application.entity.spi.DataSourceFactory;
import org.ldp4j.application.entity.spi.IdentityFactory;

import com.google.common.collect.Maps;

public final class Data {

	private static final class FailingDataServiceProxy implements InvocationHandler, DataService {

		private final Class<? extends DataService> serviceClass;

		private FailingDataServiceProxy(Class<? extends DataService> serviceClass) {
			this.serviceClass = serviceClass;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if(method.getDeclaringClass()==Object.class) {
				return method.invoke(this, args);
			} else if(method.getDeclaringClass()==DataService.class) {
				return method.invoke(this, args);
			}
			throw new IllegalStateException("Could not load data service '"+this.serviceClass.getName()+"'");
		}

		@Override
		public String getVersion() {
			return null;
		}

		@Override
		public int hashCode() {
			return this.serviceClass.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			boolean result=obj==this;
			if(!result && obj!=null && obj.getClass()==getClass()) {
				FailingDataServiceProxy that=(FailingDataServiceProxy)obj;
				result=this.serviceClass==that.serviceClass;
			}
			return result;
		}

		@Override
		public String toString() {
			return "FailingDataServiceProxy {"+this.serviceClass.getName()+"}";
		}



	}

	private static final ConcurrentMap<Class<? extends DataService>,DataService> registry=Maps.newConcurrentMap();

	private Data() {
		// Prevent instantiation
	}

	public static DataSource createDataSource() {
		return Data.getService(DataSourceFactory.class).createDataSource();
	}

	public static Identity createIdentity() {
		return Data.getService(IdentityFactory.class).createIdentity();
	}

	public static <T> Identity createIdentity(Key<T> key) {
		checkNotNull(key,"Key cannot be null");
		return Data.getService(IdentityFactory.class).createManagedIdentity(key);
	}

	public static <T, V> Identity createIdentity(Class<T> owner, V nativeId) {
		checkNotNull(owner,"Key owner cannot be null");
		checkNotNull(nativeId,"Key native identifier cannot be null");
		return createIdentity(Key.create(owner, nativeId));
	}

	public static Identity createIdentity(URI location) {
		checkNotNull(location,"Location cannot be null");
		return Data.getService(IdentityFactory.class).createExternalIdentity(location);
	}

	public static <T> Identity createIdentity(Key<T> parent, URI path) {
		checkNotNull(parent,"Parent key cannot be null");
		checkNotNull(path,"Path cannot be null");
		return Data.getService(IdentityFactory.class).createRelativeIdentity(parent, path);
	}

	public static <T, V> Identity createIdentity(Class<T> owner, V nativeId, URI path) {
		checkNotNull(owner,"Key owner cannot be null");
		checkNotNull(nativeId,"Key native identifier cannot be null");
		return createIdentity(Key.create(owner, nativeId),path);
	}

	static <T extends DataService> void registerService(final T service) {
		registry.putIfAbsent(service.getClass(), service);
	}

	static <T extends DataService> T getService(final Class<? extends T> serviceClass) {
		DataService dataService = registry.get(serviceClass);
		if(dataService!=null) {
			return serviceClass.cast(dataService);
		}
		for(Entry<Class<? extends DataService>, DataService> entry:registry.entrySet()) {
			if(serviceClass.isAssignableFrom(entry.getKey())) {
				return serviceClass.cast(entry.getValue());
			}
		}
		ServiceLoader<? extends T> loader=ServiceLoader.load(serviceClass);
		for(T service:loader) {
			registerService(service);
			return service;
		}
		if(serviceClass.isInterface()) {
			Object proxy=
				Proxy.
					newProxyInstance(
						Thread.currentThread().
							getContextClassLoader(),
						new Class[]{serviceClass},
						new FailingDataServiceProxy(serviceClass)
					);
			return serviceClass.cast(proxy);
		}
		throw new IllegalStateException("Cannot load data service '"+serviceClass.getName()+"'");
	}

	static <T extends DataService> T getService(final Class<? extends T> serviceClass, Configuration config) throws ConfigurationException {
		T service = getService(serviceClass);
		Configurator.configure(service, config);
		return service;
	}

}