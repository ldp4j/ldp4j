package org.ldp4j.application.data;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class IndividualReferenceBuilderTest {

	@Test
	public void testRelative() {
		IndividualReference<?, ?> reference = IndividualReferenceBuilder.
			newReference().
				toRelativeIndividual().
					atLocation("..").
					ofIndividualManagedBy("template").
					named(23);
		assertThat(reference.ref(),instanceOf(ManagedIndividualId.class));
		System.out.println(reference.ref());
	}

}
