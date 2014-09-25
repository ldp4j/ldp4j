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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-application:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-application-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.vocabulary;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.ldp4j.application.vocabulary.Vocabulary;


/**
 * A base vocabulary implementation for creating immutable vocabularies that
 * defines a collection of {@code ImmutableTerm}s. <br/>
 * Purpose specific vocabularies should be implemented as follows:
 * 
 * <pre>
 * private static class CustomVocabulary extends
 * 		AbstractImmutableVocabulary&lt;ImmutableTerm&gt; {
 * 
 * 	private static final String NAMESPACE = &quot;http://www.example.org/vocab&quot;;
 * 	private static final String PREFERRED_PREFIX = &quot;test&quot;;
 * 
 * 	private static final String TERM_THREE = &quot;termThree&quot;;
 * 	private static final String TERM_TWO = &quot;termTwo&quot;;
 * 	private static final String TERM_ONE = &quot;termOne&quot;;
 * 
 * 	public CustomVocabulary() {
 * 		super(ImmutableTerm.class, NAMESPACE, PREFERRED_PREFIX);
 * 	}
 * 
 * 	private static final CustomVocabulary VOCABULARY;
 * 	private static final Term ONE;
 * 	private static final Term TWO;
 * 	private static final Term THREE;
 * 
 * 	static {
 * 		VOCABULARY = new CustomVocabulary();
 * 		ONE = new ImmutableTerm(VOCABULARY, TERM_ONE);
 * 		TWO = new ImmutableTerm(VOCABULARY, TERM_TWO);
 * 		THREE = new ImmutableTerm(VOCABULARY, TERM_THREE);
 * 		VOCABULARY.initialize();
 * 	}
 * }
 * </pre>
 * 
 * Term related operations will fail with a {@code IllegalStateException} if
 * they are used before the vocabulary is initialized properly.
 * 
 * @version 1.0
 * @since 1.0.0
 * @author Miguel Esteban Guti&eacute;rrez
 * @see ImmutableTerm
 */
public abstract class AbstractImmutableVocabulary<T extends ImmutableTerm> implements Vocabulary<T> {

	private enum Status {
		INITIALIZING("is already initialized"), 
		INITIALIZED("has not been initialized properly"),
		;
		private final String explanation;

		Status(String explanation) {
			this.explanation = explanation;
		}
		
		String getFailure(String namespace) {
			return "Vocabulary '"+namespace+"' "+explanation;
		}
		
	}
	
	private final UUID id=UUID.randomUUID();
	
	private final String namespace;
	private final String prefix;
	private final Class<T> termClass;

	private final Map<String,Integer> nameOrdinal=new HashMap<String,Integer>();
	private final SortedMap<Integer,T> terms=new TreeMap<Integer,T>();

	private int ordinal=0;

	private Status status=Status.INITIALIZING;

	public AbstractImmutableVocabulary(Class<T> clazz, String namespace, String prefix) {
		this.termClass = clazz;
		this.namespace = namespace;
		this.prefix = prefix;
	}
	
	private void checkStatus(Status status) {
		if(!this.status.equals(status)) {
			throw new IllegalStateException(status.getFailure(namespace));
		}
	}

	private void checkInitializationPrecondition(boolean precondition, String message, Object... args) {
		if(!precondition) {
			String failureHeader= 
				String.format(
					"Vocabulary '%s' (%s) initialization failure: ",
					namespace,
					getClass().getCanonicalName());
			throw new IllegalArgumentException(failureHeader.concat(String.format(message,args)));
		}
	}

	/**
	 * Get the unique identifier of the vocabulary.
	 * @return The identifier of the vocabulary.
	 * @see java.util.UUID
	 */
	public final UUID getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getNamespace() {
		return namespace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getPreferredPrefix() {
		return prefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final T[] terms() {
		checkStatus(Status.INITIALIZED);
		@SuppressWarnings("unchecked")
		T[] array=(T[])Array.newInstance(termClass, ordinal);		
		return terms.values().toArray(array);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override 
	public final int size() {
		checkStatus(Status.INITIALIZED);
		return ordinal;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final T fromName(String name) {
		checkStatus(Status.INITIALIZED);
		if(name==null) {
			throw new IllegalArgumentException("Object 'name' cannot be null");
		}
		Integer termOrdinal = nameOrdinal.get(name);
		if(termOrdinal!=null) {
			return terms.get(termOrdinal);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T fromOrdinal(int ordinal) {
		checkStatus(Status.INITIALIZED);
		if(ordinal<0 || this.ordinal<=ordinal) {
			throw new IndexOutOfBoundsException("No term available with ordinal '"+ordinal+"'");
		}
		return terms.get(ordinal);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final <V> T fromValue(V value) {
		checkStatus(Status.INITIALIZED);
		try {
			@SuppressWarnings("unchecked")
			TypeAdapter<T,V> adapter = (TypeAdapter<T, V>) TypeAdapter.createAdapter(termClass,value.getClass());
			for(T candidate:terms.values()) {
				if(value.equals(adapter.adapt(candidate))) {
					return candidate;
				}
			}
			return null;
		} catch (CannotAdaptClassesException e) {
			throw new UnsupportedOperationException("Class '"+termClass.getCanonicalName()+" cannot be transformed to '"+value.getClass().getCanonicalName()+"'",e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Iterator<T> iterator() {
		checkStatus(Status.INITIALIZED);
		final Iterator<? extends T> iterator = terms.values().iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
	
			@Override
			public T next() {
				return iterator.next();
			}
	
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
		};
	}

	/**
	 * Allow the reservation of term names during the initialization of the
	 * vocabulary.
	 * 
	 * @param name
	 *            then name of the term
	 * @return a number that represents the position in which the term was
	 *         reserved
	 * @throws IllegalArgumentException
	 *             if the specified name is not valid or it has been already
	 *             reserved.
	 * @throws IllegalStateException
	 *             if the vocabulary has been already initialized.
	 */
	protected final int reserveTermName(String name) {
		checkStatus(Status.INITIALIZING);
		checkInitializationPrecondition(TermUtils.isValidTermName(name), "Object '%s' is not a valid term name",name);
		checkInitializationPrecondition(!nameOrdinal.containsKey(name),"Term '%s' has been already reserved",name);
		nameOrdinal.put(name, ordinal);
		return ordinal++;
	}

	/**
	 * Upon reservation, the method enables registering the properly built
	 * immutable term instance.
	 * 
	 * @param term
	 *            the term to be registered.
	 * @throws IllegalArgumentException
	 *             if the specified term instance cannot be registered, either
	 *             because the ordinal is invalid or because the another term
	 *             with the same name has already been registered.
	 * @throws IllegalStateException
	 *             if the vocabulary has been already initialized.
	 */
	protected final <S extends ImmutableTerm> void registerTerm(S term) {
		checkStatus(Status.INITIALIZING);
		checkInitializationPrecondition(nameOrdinal.containsKey(term.name()),"Term '%s' has not been reserved",term.name());
		checkInitializationPrecondition(term.ordinal()>=0 && term.ordinal()<this.ordinal,"invalid ordinal '%d' for reserved name '%s'",term.ordinal(),term.name());
		terms.put(term.ordinal(),termClass.cast(term));
	}

	/**
	 * Complete the initialization of the vocabulary. Beyond this point the
	 * vocabulary can be put to use.
	 * 
	 * @throws IllegalStateException
	 *             if the vocabulary has been already initialized or not all the
	 *             reserved names have been registered.
	 */
	protected final void initialize() {
		checkStatus(Status.INITIALIZING);
		if(!(terms.size()==nameOrdinal.size())) {
			throw new IllegalStateException(String.format(
				"Vocabulary '%s' (%s) initialization failure: not all reserved names have been registered",
				namespace,
				getClass().getCanonicalName()));
		} 
		this.status=Status.INITIALIZED;
	}

}
