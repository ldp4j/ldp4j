package org.ldp4j.application.data;

import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;

public final class PropertyHelper {

	private Property property;

	public PropertyHelper(Property property) {
		this.property = property;
	}

	public <T> T firstValue(final Class<? extends T> aClazz) {
		LiteralValueExtractor<T> extractor =
			new LiteralValueExtractor<T>(new LiteralAdapter<T>(aClazz));
		for(Value value:property) {
			value.accept(extractor);
			if(extractor.isAvailable()) {
				break;
			}
		}
		return extractor.getValue();
	}

	public <T, S extends Individual<T,S>> T firstIndividual(final Class<? extends S> clazz) {
		IndividualExtractor<T,S> extractor=new IndividualExtractor<T,S>(clazz);
		for(Value value:this.property) {
			value.accept(extractor);
			if(extractor.isAvailable()) {
				break;
			}
		}
		return extractor.getValue().id();
	}

}