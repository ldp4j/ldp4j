package org.ldp4j.reflect;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.ldp4j.reflect.harness.Childclass;
import org.ldp4j.reflect.harness.Marker;
import org.ldp4j.reflect.harness.Superclass;

public class MetaClassTest {

	@Test(expected=NullPointerException.class)
	public void testCreate$null() {
		MetaClass.create(null);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testCreate$notNull() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass,notNullValue());
		assertThat((Class)metaClass.rawType(),sameInstance((Class)Childclass.class));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testSuperclass() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass.superClass(),notNullValue());
		assertThat((Class)metaClass.superClass().rawType(),sameInstance((Class)Childclass.class.getSuperclass()));
	}

	@Test
	public void testInterfaces() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass.interfaces(),notNullValue());
		List<Class<?>> interfaces = Arrays.asList(Childclass.class.getInterfaces());
		for(MetaClass metaInterface:metaClass.interfaces()) {
			assertThat(metaInterface.rawType(),isIn(interfaces));
		}
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testParameterArguments() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		Map<TypeVariable<?>, Type> parameterArguments = metaClass.parameterArguments();
		TypeVariable<?> E = Superclass.class.getTypeParameters()[0];
		TypeVariable<?> T = Marker.class.getTypeParameters()[0];
		Type EVal=String.class;
		Type TVal=Superclass.class;
		assertThat(parameterArguments,notNullValue());
		assertThat(parameterArguments.keySet(),hasSize(2));
		assertThat(parameterArguments.keySet(),hasItem(T));
		assertThat(parameterArguments.keySet(),hasItem(E));
		assertThat(parameterArguments.get(E),sameInstance(EVal));
		Type actual = parameterArguments.get(T);
		assertThat(actual,instanceOf(ParameterizedType.class));
		assertThat((Class)((ParameterizedType)actual).getRawType(),sameInstance((Class)TVal));
//		System.out.println("Parameter arguments:");
//		for(Entry<TypeVariable<?>,Type> entry:parameterArguments.entrySet()) {
//			System.out.printf("- %s : %s%n",entry.getKey(),entry.getValue());
//		}
	}

	@Test
	public void testTypeParameters$notParameterizedType() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass.typeParameters(),notNullValue());
		assertThat(metaClass.typeParameters().length,equalTo(0));
	}

	@Test
	public void testTypeArguments$notParameterizedType() {
		MetaClass metaClass = MetaClass.create(Childclass.class);
		assertThat(metaClass.typeArguments(),notNullValue());
		assertThat(metaClass.typeArguments().length,equalTo(0));
	}

	@Test
	public void testTypeArguments$parameterizedType() {
		MetaClass metaClass = MetaClass.create(Superclass.class);
		TypeVariable<?>[] parameters = metaClass.typeParameters();
		assertThat(metaClass.typeArguments(),notNullValue());
		assertThat(metaClass.typeArguments().length,equalTo(parameters.length));
		for(int i=0;i<parameters.length;i++) {
			assertThat(metaClass.typeArguments()[i],equalTo((Type)parameters[i]));
		}
	}

}
