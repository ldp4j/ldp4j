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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.1.0
 *   Bundle      : ldp4j-commons-core-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class Path {

	private static final char     CURRENT_CHAR   = '.';
	private static final String   SLASH          = "/";
	private static final String   PARENT         = "..";
	private static final String   CURRENT        = ".";
	private static final Path     EMPTY_PATH     = Path.create("");

	private static final String[] EMPTY_SEGMENTS = new String[0];
	private static final String[] ROOT_SEGMENTS   = new String[] {""};


	private String directory;
	private String fileName;
	private String fileExtension;

	private Path() {
	}

	private Path(Path path) {
		setDirectory(path.getDirectory());
		setFile(path.getFile());
	}

	private void setDirectory(String directory) {
		this.directory = directory;
	}

	private void setFile(String file) {
		String tFileName=null;
		String tFileExtension=null;
		if(file!=null && !file.isEmpty()) {
			int ext = file.lastIndexOf(CURRENT_CHAR);
			if(ext>=0) {
				tFileName=file.substring(0,ext);
				tFileExtension=file.substring(ext+1);
			} else {
				tFileName=file;
			}
		}
		setFileName(tFileName);
		setFileExtension(tFileExtension);
	}

	private void setFileExtension(String fileExtension) {
		this.fileExtension=fileExtension;
	}

	private void setFileName(String fileName) {
		this.fileName=fileName;
	}

	private String nullable(String string) {
		return string==null?"":string;
	}

	private String file() {
		StringBuilder builder=new StringBuilder();
		builder.append(nullable(this.fileName));
		if(this.fileExtension!=null) {
			builder.append(CURRENT);
			builder.append(this.fileExtension);
		}
		return builder.toString();
	}

	private String normalizePath(String[] segments, String file) {
		Deque<String> buffer=new LinkedList<String>();
		for(String segment:segments) {
			if(segment.equals(CURRENT)) {
				// do nothing
			} else if(segment.equals(PARENT)) {
				processParentSegment(buffer);
			} else {
				buffer.addLast(segment);
			}
		}
		return assembleSegments(buffer, file);
	}

	private void processParentSegment(Deque<String> buffer) {
		if(!buffer.isEmpty()) {
			if(buffer.peekLast().equals(PARENT)) {
				buffer.addLast(PARENT);
			} else if(buffer.peekLast().isEmpty()){
				buffer.addLast(PARENT);
			} else {
				buffer.removeLast();
			}
		} else {
			buffer.addLast(PARENT);
		}
	}

	private String assembleSegments(Deque<String> segments, String file) {
		StringBuilder builder=new StringBuilder();
		for(Iterator<String> it=segments.iterator();it.hasNext();) {
			String segment=it.next();
			builder.append(segment);
			if(it.hasNext() || !isDotSegment(segment) || file!=null) {
				builder.append(SLASH);
			}
		}
		if(file!=null) {
			builder.append(file);
		}
		return builder.toString();
	}

	private boolean isDotSegment(String segment) {
		return segment.equals(CURRENT) || segment.equals(PARENT);
	}

	private String[] segments() {
		if(this.directory==null) {
			return EMPTY_SEGMENTS;
		}
		if(SLASH.equals(this.directory)) {
			return ROOT_SEGMENTS;
		} else {
			return this.directory.split(SLASH);
		}
	}

	public boolean isEmpty() {
		return this.directory==null && this.fileName==null && this.fileExtension==null;
	}

	public boolean isRoot() {
		return this.directory!=null && this.directory.startsWith(SLASH);
	}

	public boolean isOutOfScope() {
		// Files are always on scope
		if(this.directory==null) {
			return false;
		}

		// First, normalize
		Path normalize=normalize();

		// If now we are a file, we are in scope
		if(normalize.directory==null) {
			return false;
		}

		// If we find a segment which is '..' we are out of scope
		String[] segments=normalize.segments();
		boolean result=false;
		for(int i=0;i<segments.length && !result;i++) {
			result=isDotSegment(segments[i]);
		}
		return result;
	}

	public boolean isDirectory() {
		return this.directory!=null && this.fileName==null && this.fileExtension==null;
	}

	public boolean isFile() {
		return !isDirectory();
	}

	public String getDirectory() {
		return this.directory;
	}

	public String getFile() {
		String file=file();
		return file.isEmpty()?null:file;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getFileExtension() {
		return this.fileExtension;
	}

	public Path withDirectory(String directory) {
		Path result = new Path(this);
		result.setDirectory(directory);
		return result;
	}

	public Path withFile(String file) {
		Path result = new Path(this);
		result.setFile(file);
		return result;
	}

	public Path unroot() {
		if(!isRoot()) {
			return this;
		}
		return Path.create(toString().substring(1));
	}

	public Path normalize() {
		String[] segments=
			this.directory==null?
				EMPTY_SEGMENTS:
				SLASH.equals(this.directory)?
					ROOT_SEGMENTS:
					this.directory.split(SLASH);
		return Path.create(normalizePath(segments, getFile()));
	}

	/**
	 * Computes relative relative path to reference "target" from "base". Uses
	 * ".." if needed, in contrast to {@link URI#relativize(URI)}.
	 */
	public Path relativize(Path path) {
		Objects.requireNonNull("Path cannot be null");

		Path base=this;

		// By default, we return the normalized form of the input path
		Path defaultRelative = path.normalize();

		// Either root or not
		if(base.isRoot()!=path.isRoot()) {
			return defaultRelative;
		}

		// If the path is not root, return its normalized base
		if(!path.isRoot()) {
			return defaultRelative;
		}

		// Beyond this point we need the normalized form
		Path nBase = base.normalize();

		// If we are out of the scope...
		if(base.isOutOfScope()) {
			// ... we must return the input path because it will define the
			// scope
			Path relative=defaultRelative;
			if(nBase.equals(relative)) {
				// ... unless the path is the same as ourselves, in which
				// case it is safe to return the empty path
				relative=EMPTY_PATH;
			}
			return relative;
		}

		// Can only compare for equality iff normalized.
		Path unBase=nBase.unroot();
		Path unPath=defaultRelative.unroot();

		// If the base and the target are the same, return the empty path
		if(unBase.equals(unPath)) {
			return EMPTY_PATH;
		}

		return
			assembleRelativeSegments(
				path,
				base,
				getRelativeSegments(
					unBase.segments(),
					unPath.segments()));
	}

	/**
	 * If there are no segments in the resolved path, and we are trying to
	 * resolve a directory coming from a path, we have to make explicit that we
	 * want the directory
	 */
	private Path assembleRelativeSegments(Path path, Path base, Deque<String> segments) {
		if(segments.isEmpty() && path.isDirectory() && base.isFile()) {
			segments.add(CURRENT);
		}

		return Path.create(assembleSegments(segments,path.getFile()));
	}

	private Deque<String> getRelativeSegments(String[] baseSegments,String[] targetSegments) {
		Deque<String> segments=new LinkedList<String>();
		// Look for index of last common segment
		int commonSegments=countCommonSegments(baseSegments, targetSegments);
		// For each different segment of the base path, add '..' to the
		// segments of the relative path
		addParentSegments(segments,baseSegments,commonSegments);
		// Add each different segment of the input path to the segments of
		// the relative path
		addChildSegments(segments,targetSegments,commonSegments);
		return segments;
	}

	private void addChildSegments(Deque<String> segments, String[] targetSegments, int commonSegments) {
		for(int i=commonSegments;i<targetSegments.length;i++) {
			segments.add(targetSegments[i]);
		}
	}

	private void addParentSegments(Deque<String> segments, String[] baseSegments, int commonSegments) {
		for(int i=commonSegments;i<baseSegments.length;i++) {
			segments.add(PARENT);
		}
	}

	private int countCommonSegments(String[] s1, String[] s2) {
		int comparableSegments=Math.min(s1.length,s2.length);
		int commonSegments=0;
		for(int i=0;i<comparableSegments;i++) {
			if(!s1[i].equals(s2[i])) {
				break;
			}
			commonSegments++;
		}
		return commonSegments;
	}

	public Path resolve(Path path) {
		if(path==null) {
			throw new NullPointerException("Target path cannot be null");
		}
		if(path.equals(Path.create(""))) {
			return this.normalize();
		}
		if(path.isEmpty()) {
			return this.normalize();
		}
		if(path.isRoot()) {
			return path.normalize();
		}
		Path base=normalize();
		Path relative=path.normalize();

		List<String> baseSegments=new ArrayList<String>(Arrays.asList(base.segments()));
		baseSegments.addAll(Arrays.asList(relative.segments()));
		String[] segments = baseSegments.toArray(new String[baseSegments.size()]);
		String resolved=normalizePath(segments,path.getFile());
		return Path.create(resolved);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.directory,this.fileName,this.fileExtension);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof Path) {
			Path that=(Path)obj;
			result=
				Objects.equals(this.directory,that.directory) &&
				Objects.equals(this.fileName,that.fileName) &&
				Objects.equals(this.fileExtension,that.fileExtension);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder=new StringBuilder();
		if(this.directory!=null) {
			builder.append(this.directory);
		}
		builder.append(file());
		return builder.toString();
	}
	public static Path create(java.net.URI jdkURI) {
		return create(jdkURI.getPath());
	}

	public static Path create(String path) {
		Path result=null;
		if(path!=null) {
			String directory=null;
			String file=null;
			int lastSegmentSeparator = path.lastIndexOf('/');
			if(lastSegmentSeparator<0) {
				if(CURRENT.equals(path)) {
					directory=CURRENT;
				} else if(PARENT.equals(path)) {
					directory=PARENT;
				} else {
					file=path;
				}
			} else if(lastSegmentSeparator==path.length()-1) {
				directory=path;
			} else {
				directory=path.substring(0,lastSegmentSeparator+1);
				String segment=path.substring(lastSegmentSeparator+1);
				if(CURRENT.equals(segment)) {
					directory+=CURRENT;
				} else if(PARENT.equals(segment)) {
					directory+=PARENT;
				} else {
					file=segment;
				}
			}
			result=new Path();
			result.setDirectory(directory);
			result.setFile(file);
		}
		return result;
	}

}