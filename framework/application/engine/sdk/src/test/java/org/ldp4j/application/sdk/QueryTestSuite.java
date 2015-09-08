package org.ldp4j.application.sdk;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ImmutableQueryParameterTest.class,
	NullQueryParameterTest.class,
	ImmutableQueryTest.class,
	QueryBuilderTest.class
})
public class QueryTestSuite {

}
