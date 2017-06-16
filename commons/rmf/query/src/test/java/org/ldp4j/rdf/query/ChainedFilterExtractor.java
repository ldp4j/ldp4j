/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the LDP4j Project:
 *     http://www.ldp4j.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2014-2016 Center for Open Middleware.
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
 *   Artifact    : org.ldp4j.commons.rmf:rmf-query:0.2.2
 *   Bundle      : rmf-query-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.rdf.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ldp4j.commons.IndentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChainedFilterExtractor {
	
	private static final Logger LOGGER=LoggerFactory.getLogger("org.megatwork.debug");
	
	private final List<String> patterns;

	public ChainedFilterExtractor(List<String> patterns) {
		this.patterns=new ArrayList<String>(patterns);
	}
	
	public ChainedFilterExtractor(String... patterns) {
		this(Arrays.asList(patterns));
	}
	
	private void trace(IndentUtils indenter, String format, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(block(indenter, format, args));
		}
	}

	private void debug(IndentUtils indenter, String format, Object... args) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(block(indenter, format, args));
		}
	}

	/**
	 * @param indenter
	 * @param format
	 * @param args
	 * @return
	 */
	private String block(IndentUtils indenter, String format, Object... args) {
		String indentation = indenter.indent();
		String newLine = "\n\t".concat(indentation);
		String rawText = String.format(indentation.concat(format),args);
		String block = rawText.replaceAll("\n", newLine);
		return block;
	}

	private List<String> doFilter(IndentUtils util, String item, String rawPattern) {
		List<String> result=new ArrayList<String>();
		trace(util,"Item:\n%s",item);
		Pattern pattern=Pattern.compile(rawPattern);
		trace(util,"Pattern: %s",pattern);
		Matcher matcher = pattern.matcher(item);
		boolean found = false;
		while (matcher.find()) {
			if (!found) {
				trace(util,"Match found:");
			}
			trace(util,"\t- Text \"%s\" starting at index %d and ending at index %d.",matcher.group(), matcher.start(),matcher.end());
			result.add(matcher.group());
			found = true;
		}
		if (!found) {
			trace(util,"No match found for pattern %s.",rawPattern);
		}
		return result;
	}
	
	private List<String> doChaining(IndentUtils indenter, String item) {
		List<String> result=new ArrayList<String>();
		List<String> toProcess=new ArrayList<String>();
		toProcess.add(item);
		for(Iterator<String> it=patterns.iterator();it.hasNext();) {
			indenter.increase();
			String nextPattern = it.next();
			List<String> tmp=new ArrayList<String>();
			for(String nextItem:toProcess) {
				tmp.addAll(doFilter(indenter,nextItem,nextPattern));
			}
			toProcess=tmp;
			if(it.hasNext()) {
				if(toProcess.isEmpty()) {
					trace(indenter,"Nothing to refine");
				} else {
					trace(indenter,"Refining: %s",toProcess);
				}
			} else {
				result.addAll(toProcess);
			}
		}
		return result;
	}

	public List<String> extract(String item) {
		List<String> result = doChaining(new IndentUtils(), item);
		if(LOGGER.isDebugEnabled()) {
			debug(new IndentUtils(),"Extraction completed:\n-Data:\n%s\n-Result: %s",item,result);
		}
		return result;
	}
	
}