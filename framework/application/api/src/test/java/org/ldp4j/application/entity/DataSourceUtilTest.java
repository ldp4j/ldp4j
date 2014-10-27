package org.ldp4j.application.entity;

import java.net.URI;

import org.junit.Test;
import org.ldp4j.application.domain.RDFS;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DataSourceUtilTest {

	@Test
	public void testToString() {
		DataSource dataSource=DataSource.create();

		URI ind1 = URI.create("http://www.ldp4j.org/ind1/");
		Entity ent1 = new Entity(ind1);
		ent1.addProperty(RDFS.LABEL.as(URI.class), ImmutableLiteral.create("Individual 1 label"));
		ent1.addProperty(RDFS.COMMENT.as(URI.class), ImmutableLiteral.create("Comment 1"));

		Entity ent2 = new Entity(DataSource.class,123);
		ent2.addProperty(RDFS.LABEL.as(URI.class), ImmutableLiteral.create("Individual 2 label"));
		ent2.addProperty(RDFS.COMMENT.as(URI.class), ImmutableLiteral.create("Comment 2"));

		Entity ent3 = new Entity(dataSource);
		ent3.addProperty(RDFS.LABEL.as(URI.class), ImmutableLiteral.create("Individual 3 label"));
		ent3.addProperty(RDFS.COMMENT.as(URI.class), ImmutableLiteral.create("Comment 3"));

		URI linkedTo = URI.create("http://www.example.org/vocab#linkedTo");
		ent2.addProperty(linkedTo, ent3);
		ent1.addProperty(linkedTo, ent2);

		dataSource.add(ent1);

		Property property = ent2.getProperty(linkedTo);

		Value value = property.iterator().next();
		assertThat(value,is(instanceOf(Entity.class)));
		Entity sEnt3 = (Entity)value;
		assertThat(sEnt3.identity(),equalTo(ent3.identity()));
		assertThat(sEnt3.identifier(),equalTo(ent3.identifier()));
		assertThat(sEnt3.dataSource(),equalTo(ent3.dataSource()));

		System.out.println(DataSourceUtil.toString(dataSource));
		ent3.addProperty(linkedTo, ent1);
		sEnt3.addProperty(linkedTo, ent2);
		System.out.println(DataSourceUtil.toString(dataSource));
	}

}
