/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.ldp4j.framework:ldp4j-client-api:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-client-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.client;



/**
 * A {@code DeletionResult} represents the response of a <i>Linked Data Platform
 * Resource</i> for a successful DELETE request. It specifies whether or not the
 * deletion was {@link #isEnacted() enacted} and the response
 * {@link #getMessage() message} returned by the resource.
 * 
 * @author Miguel Esteban Gutiérrez
 * @since 1.0.0
 * @version 1.0
 */
public final class DeletionResult {

	/** Deletion enactment flag. */
	private final boolean enacted;
	
	/** The response message, if any */
	private final String message;


	/**
	 * Instantiates a new deletion result.
	 *
	 * @param enacted the enacted
	 * @param message the message
	 */
	private DeletionResult(boolean enacted, String message) {
		this.enacted = enacted;
		this.message = message;
	}
	

	/**
	 * Checks if the deletion was enacted.
	 *
	 * @return {@code true}, if the deletion was enacted. {@code false} otherwise.
	 */
	public boolean isEnacted() {
		return enacted;
	}
	
	/**
	 * Gets the response message.
	 *
	 * @return the response message, or {@code null} if no message was included in the response.
	 */
	public String getMessage() {
		return message;
	}
	
	
	/** 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DeletionResult [enacted=" + enacted + ", message=" + message + "]";
	}

	/** 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 19;
		int result = 17;
		result = prime * result + (enacted ? 1231 : 1237);
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DeletionResult)) {
			return false;
		}
		DeletionResult other = (DeletionResult) obj;
		if (enacted != other.enacted) {
			return false;
		}
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		return true;
	}


	/**
	 * Create a new {@link DeletionResultBuilder} for helping in the creation of a deletion result.
	 * 
	 * @return A new {@code DeletionResultBuilder}.
	 * @see DeletionResult.DeletionResultBuilder
	 */
	public static DeletionResultBuilder newBuilder() {
		return new DeletionResultBuilder();
	}


	/**
	 * The a builder class simplifying the deletion result creation process.
	 * 
	 * @author Miguel Esteban Gutiérrez
	 * @since 1.0.0
	 * @version 1.0
	 */
	public static final class DeletionResultBuilder {
		
		/** The enactment flag. */
		private boolean enacted=true;
		
		/** The response message. */
		private String message=null;
		
		/**
		 * Instantiates a new deletion result builder.
		 */
		private DeletionResultBuilder() { 
		}
		
		/**
		 * Use the specified <i>enactment flag</i> when
		 * creating the customized {@code DeletionResult}.
		 * 
		 * @param location
		 *            the location of the service.
		 * @return the deletion result builder.
		 * @see DeletionResult#isEnacted()
		 */
		public DeletionResultBuilder enacted(boolean enacted) {
			this.enacted=enacted;
			return this;
		}
		
		/**
		 * Use the specified <i>response message</i> when
		 * creating the customized {@code DeletionResult}.
		 * 
		 * @param message
		 *            the response message.
		 * @return the deletion result builder.
		 * @see DeletionResult#getMessage()
		 */
		public DeletionResultBuilder withMessage(String message) {
			this.message=message;
			return this;
		}
		
		/**
		 * Builds the customized deletion result using the provided enactment flag and response message.
		 *
		 * @return the deletion result
		 * @see DeletionResult
		 */
		public DeletionResult build() {
			return new DeletionResult(enacted,message);
		}
	}

}