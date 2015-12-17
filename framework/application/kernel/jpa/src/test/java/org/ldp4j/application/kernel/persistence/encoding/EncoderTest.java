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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-kernel-jpa:0.2.0-SNAPSHOT
 *   Bundle      : ldp4j-application-kernel-jpa-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.kernel.persistence.encoding;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;

public class EncoderTest {

	private static class Stat {

		private long counter=0;
		private int[] max={Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE};
		private int[] min={Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
		private int[] total={0,0,0,0,0};

		public void add(int e1, int e2, int e3) {
			this.counter++;
			update(0, e1);
			update(1, e2);
			update(2, e3);
			update(3, e1-e3);
			update(4, e2-e3);
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
			printStats(builder, 0, "[1] Name encoding");
			printStats(builder, 1, "[2] Value encoding");
			printStats(builder, 2, "[3] Hybrid encoding");
			printStats(builder, 3, "Difference [1-3]");
			printStats(builder, 4, "Difference [2-3]");
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

	public static class Data implements Serializable {

		private static final long serialVersionUID = -3504519947095426718L;

		private final String prefix;
		private final String localName;

		public Data(String prefix, String localName) {
			this.prefix = prefix;
			this.localName = localName;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.prefix,this.localName);
		}

		@Override
		public boolean equals(Object obj) {
			boolean result = false;
			if(obj instanceof Data) {
				Data that=(Data)obj;
				result=
					Objects.equals(this.prefix,that.prefix) &&
					Objects.equals(this.localName,that.localName);
			}
			return result;
		}

		@Override
		public String toString() {
			return this.prefix+":"+this.localName;
		}

		public static Data valueOf(String str) {
			String[] split = str.split(":");
			if(split.length!=2) {
				throw new IllegalArgumentException("Invalid data");
			}
			return new Data(split[0],split[1]);
		}

	}

	@SuppressWarnings("unchecked")
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
			NamingScheme.getDefault().name(new Data("sdh","platform")),
		};

		Encoder nameEncoder=new NameEncoder();
		Encoder valueEncoder=new ValueEncoder();
		Encoder customEncoder=new CustomEncoder();

		Stat stat=new Stat();
		for(Name<?> name:names) {
			System.out.printf("Name [%s]: %s%n",name.id().getClass().getName(),name);
			String nameEncoding = nameEncoder.encode(name);
			String valueEncoding = valueEncoder.encode(name);
			String customEncoding = customEncoder.encode(name);
			stat.add(nameEncoding.length(),valueEncoding.length(),customEncoding.length());
			show("Name   encoding",nameEncoding);
			show("Value  encoding",valueEncoding);
			show("Custom encoding",customEncoding);
			Name<Serializable> decoded = customEncoder.decode(name.id().getClass().getName(),customEncoding);
			assertThat(decoded,equalTo((Name<Serializable>)name));
		}
		System.out.println(stat);
	}

	private void show(String title, String value) {
		System.out.printf("  - %s [%d]: %s%n",title,value.length(),value);
	}

}
