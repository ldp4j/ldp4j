package org.ldp4j.application.entity;

public class IdentityVisitor {

	public void visitLocal(Local<?> identity) {
		// To be overriden by implementors
	}

	public void visitManaged(Managed<?> identity) {
		// To be overriden by implementors
	}

	public void visitExternal(External identity) {
		// To be overriden by implementors
	}

}
