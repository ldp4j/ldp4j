package org.ldp4j.server.tckf;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.ManagedIndividual;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Property;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.ext.InvalidContentException;
import org.ldp4j.application.session.ResourceSnapshot;

final class TCKFHelper {

	static final URI READ_ONLY_PROPERTY = URI.create("http://www.example.org/vocab#creationDate");

	private TCKFHelper() {
	}

	static void enforceConsistency(ResourceSnapshot resource, DataSet content, DataSet dataSet) throws InvalidContentException {
		ManagedIndividualId id = ManagedIndividualId.createId(resource.name(),TCKFResourceHandler.ID);
		ManagedIndividual stateIndividual = 
			dataSet.
				individual(
					id, 
					ManagedIndividual.class);
		ManagedIndividual inIndividual = 
			content.
				individual(
					id, 
					ManagedIndividual.class);
		Property stateProperty=
			stateIndividual.property(READ_ONLY_PROPERTY);
		Property inProperty=
			inIndividual.property(READ_ONLY_PROPERTY);

		if(stateProperty==null && inProperty==null) {
			return;
		}
		if(stateProperty==null && inProperty!=null) {
			throw new InvalidContentException("Added values to property '"+READ_ONLY_PROPERTY+"'");
		}
		if(stateProperty!=null && inProperty==null) {
			throw new InvalidContentException("Removed all values from property '"+READ_ONLY_PROPERTY+"'");
		}

		for(Value value:inProperty) {
			boolean newAdded=false;
			for(Value c:stateProperty) {
				if(c.equals(value)) {
					newAdded=true;
					break;
				}
			}
			if(newAdded) {
				throw new InvalidContentException("New value '"+value+"' for property '"+READ_ONLY_PROPERTY+"' has been added");
			}
		}
		for(Value value:stateProperty) {
			boolean deleted=true;
			for(Value c:inProperty) {
				if(c.equals(value)) {
					deleted=false;
					break;
				}
			}
			if(deleted) {
				throw new InvalidContentException("Value '"+value+"' for property '"+READ_ONLY_PROPERTY+"' has been deleted");
			}
		}
	}

	
}
