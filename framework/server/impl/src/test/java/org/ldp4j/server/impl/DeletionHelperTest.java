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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-impl:1.0.0-SNAPSHOT
 *   Bundle      : ldp4j-server-impl-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.server.Format;
import org.ldp4j.server.IContent;
import org.ldp4j.server.IResource;
import org.ldp4j.server.core.Deletable;
import org.ldp4j.server.core.Delete;
import org.ldp4j.server.core.DeletionException;
import org.ldp4j.server.core.DeletionResult;
import org.ldp4j.server.core.ILinkedDataPlatformResourceHandler;
import org.ldp4j.server.impl.DeletionHelper;
import org.ldp4j.server.testing.stubs.WorkingResource;


public class DeletionHelperTest {

	private DeletionResult ok, no_content, accepted, failure;

	@Before
	public void setUp() {
		ok = DeletionResult.newBuilder().enacted(true).withMessage("DELETED").build();
		no_content = DeletionResult.newBuilder().enacted(true).build();
		accepted = DeletionResult.newBuilder().enacted(false).build();
		failure = null;
	}

	private static class AbstractHandler implements ILinkedDataPlatformResourceHandler {

		@Override
		public String getContainerId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IResource getResource(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IResource updateResource(String resourceId, IContent content,
				Format format) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<String> getResourceList() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private static class InvalidParameter extends AbstractHandler {

		@Delete
		public DeletionResult delete() throws DeletionException {
			return null;
		}

	}

	private static class InvalidResult extends AbstractHandler {

		@Delete
		public void delete(String id) throws DeletionException {
		}

	}

	private static class AbstractDeletableResourceHandler extends AbstractHandler {

		private final DeletionResult result;

		public AbstractDeletableResourceHandler(DeletionResult result) {
			this.result = result;
		}

		protected DeletionResult getResult() throws DeletionException {
			if(result==null) {
				throw new DeletionException("Error found");
			}
			return result;
		}

	}

	private static class DeletableResource extends AbstractDeletableResourceHandler implements Deletable {

		public DeletableResource(DeletionResult result) {
			super(result);
		}

		public DeletionResult delete(String id) throws DeletionException {
			return getResult();
		}

	}

	private static class AnnotatedDeletableResource extends AbstractDeletableResourceHandler {

		public AnnotatedDeletableResource(DeletionResult result) {
			super(result);
		}

		@Delete
		public DeletionResult delete(String id) throws DeletionException {
			return getResult();
		}

	}

	private static class NonDeletable extends AbstractHandler {


	}

	private static class MoreThanOne extends AbstractHandler {

		@Delete
		public DeletionResult deleteResource(String resourceId) throws DeletionException {
			return null;
		}

		@Delete
		public DeletionResult delete(String resourceId) throws DeletionException {
			return null;
		}
	}

	@Test
	public void testIsDeletionSupported$deletableResource() throws Exception {
		DeletionHelper sut = new DeletionHelper(new DeletableResource(null));
		assertTrue(sut.isDeletionSupported());
	}

	@Test
	public void testIsDeletionSupported$notDeletableResource$noAnnotation() throws Exception {
		DeletionHelper sut = new DeletionHelper(new NonDeletable());
		assertFalse(sut.isDeletionSupported());
	}

	@Test
	public void testIsDeletionSupported$annotatedResource$singleAnnotation() throws Exception {
		DeletionHelper sut = new DeletionHelper(new WorkingResource());
		assertTrue(sut.isDeletionSupported());
	}

	@Test
	public void testIsDeletionSupported$annotatedResource$multipleAnnotations() throws Exception {
		DeletionHelper sut = new DeletionHelper(new MoreThanOne());
		assertFalse(sut.isDeletionSupported());
	}

	@Test
	public void testIsDeletionSupported$annotatedResource$invalidImplementation$parameters() throws Exception {
		DeletionHelper sut = new DeletionHelper(new InvalidParameter());
		assertFalse(sut.isDeletionSupported());
	}

	@Test
	public void testIsDeletionSupported$annotatedResource$invalidImplementation$returnType() throws Exception {
		DeletionHelper sut = new DeletionHelper(new InvalidResult());
		assertFalse(sut.isDeletionSupported());
	}

	@Test
	public void testDelete$deletableResult$success$OK() throws Exception {
		DeletionHelper sut = new DeletionHelper(new DeletableResource(ok));
		Response delete = sut.delete("1");
		assertNotNull(delete);
		assertTrue(delete.getStatus()==Status.OK.getStatusCode());
		assertTrue(ok.getMessage().equals(delete.getEntity()));
	}

	@Test
	public void testDelete$annotatedResource$singleAnnotation$success$OK() throws Exception {
		DeletionHelper sut = new DeletionHelper(new AnnotatedDeletableResource(ok));
		Response delete = sut.delete("1");
		assertNotNull(delete);
		assertTrue(delete.getStatus()==Status.OK.getStatusCode());
		assertTrue(ok.getMessage().equals(delete.getEntity()));
	}

	@Test
	public void testDelete$deletableResult$success$noContent() throws Exception {
		DeletionHelper sut = new DeletionHelper(new DeletableResource(no_content));
		Response delete = sut.delete("1");
		assertNotNull(delete);
		assertTrue(delete.getStatus()==Status.NO_CONTENT.getStatusCode());
		assertNull(delete.getEntity());
	}

	@Test
	public void testDelete$annotatedResource$singleAnnotation$success$noContent() throws Exception {
		DeletionHelper sut = new DeletionHelper(new AnnotatedDeletableResource(no_content));
		Response delete = sut.delete("1");
		assertNotNull(delete);
		assertTrue(delete.getStatus()==Status.NO_CONTENT.getStatusCode());
		assertNull(delete.getEntity());
	}

	@Test
	public void testDelete$deletableResult$success$accepted() throws Exception {
		DeletionHelper sut = new DeletionHelper(new DeletableResource(accepted));
		Response delete = sut.delete("1");
		assertNotNull(delete);
		assertTrue(delete.getStatus()==Status.ACCEPTED.getStatusCode());
		assertNull(delete.getEntity());
	}

	@Test
	public void testDelete$annotatedResource$singleAnnotation$success$accepted() throws Exception {
		DeletionHelper sut = new DeletionHelper(new AnnotatedDeletableResource(accepted));
		Response delete = sut.delete("1");
		assertNotNull(delete);
		assertTrue(delete.getStatus()==Status.ACCEPTED.getStatusCode());
		assertNull(delete.getEntity());
	}

	@Test(expected=DeletionException.class)
	public void testDelete$deletableResult$failure() throws Exception {
		DeletionHelper sut = new DeletionHelper(new DeletableResource(null));
		sut.delete("1");
	}

	@Test(expected=DeletionException.class)
	public void testDelete$annotatedResource$failure() throws Exception {
		DeletionHelper sut = new DeletionHelper(new AnnotatedDeletableResource(failure));
		sut.delete("1");
	}
}