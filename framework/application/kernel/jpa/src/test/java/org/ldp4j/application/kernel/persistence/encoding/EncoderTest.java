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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.2
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.encoding;

import java.net.URI;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.kernel.persistence.encoding.Encoder;
import org.ldp4j.application.kernel.persistence.encoding.NameEncoder;
import org.ldp4j.application.kernel.persistence.encoding.ValueEncoder;

public class EncoderTest {

	private static class Stat {
	
		private long counter=0;
		private int[] max={Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE};
		private int[] min={Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
		private int[] total={0,0,0};
	
		public void add(int e1, int e2) {
			this.counter++;
			update(0, e1);
			update(1, e2);
			update(2, e1-e2);
		}
	
		private void update(int position, int value) {
			this.max[position]=Math.max(this.max[position], value);
			this.min[position]=Math.min(this.min[position], value);
			this.total[position]=this.total[position]+value;
		}
		@Override
		public String toString() {
			StringBuilder builder=new StringBuilder();
			builder.append("Total observations: ").append(counter).append(System.lineSeparator());
			printStats(builder, 0, "Name encoding");
			printStats(builder, 1, "Value encoding");
			printStats(builder, 2, "Difference");
			return builder.toString();
		}
	
		private void printStats(StringBuilder builder, int position, String title) {
			builder.
				append("- ").append(title).append(":").append(System.lineSeparator()).
				append("  + Min : ").append(this.min[position]).append(System.lineSeparator()).
				append("  + Max : ").append(this.max[position]).append(System.lineSeparator()).
				append("  + Mean: ").append(this.total[position]/this.counter).append(System.lineSeparator()).
				append("  + Diff: ").append(this.max[position]-this.min[position]).append(System.lineSeparator());
		}
	
	}

	@Test
	public void testProcessing() {
		Name<?>[] names= {
			NamingScheme.getDefault().name("http://www.ldp4j.org/resource/"),
			NamingScheme.getDefault().name(URI.create("http://www.ldp4j.org/resource/")),
			NamingScheme.getDefault().name(new QName("http://www.ldp4j.org/", "resource/","ldp4j")),
			NamingScheme.getDefault().name(Byte.MAX_VALUE),
			NamingScheme.getDefault().name(Short.MAX_VALUE),
			NamingScheme.getDefault().name(Integer.MAX_VALUE),
			NamingScheme.getDefault().name(Long.MAX_VALUE),
			NamingScheme.getDefault().name(Double.MAX_VALUE),
			NamingScheme.getDefault().name(Float.MAX_VALUE),
//			NamingScheme.getDefault().name(RDFS.RESOURCE)
		};

		Encoder nameEncoder=new NameEncoder();

		Encoder valueEncoder=new ValueEncoder();
		Stat stat=new Stat();
		for(Name<?> name:names) {
			System.out.printf("Name [%s]: %s%n",name.id().getClass().getName(),name);
			String nameEncoding = nameEncoder.encode(name);
			String valueEncoding = valueEncoder.encode(name);
			stat.add(nameEncoding.length(),valueEncoding.length());
			show("Name  encoding",nameEncoding);
			show("Value encoding",valueEncoding);
		}
		System.out.println(stat);
	}

	private void show(String title, String value) {
		System.out.printf("  - %s [%d]: %s%n",title,value.length(),value);
	}

}
