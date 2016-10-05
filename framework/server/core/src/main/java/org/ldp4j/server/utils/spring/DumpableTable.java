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
 *   Artifact    : org.ldp4j.framework:ldp4j-server-core:0.2.2
 *   Bundle      : ldp4j-server-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.server.utils.spring;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class DumpableTable {

	private ColumnConfiguration[] configurations;

	private String[] columnTitles;
	private int[] columnLengths;
	private List<List<String>> rows;
	private boolean sort=false;

	public enum Alignment {
		LEFT,
		RIGHT,
		CENTER
	}

	public static class ColumnConfiguration {

		private final String title;
		private Alignment alignment=Alignment.LEFT;
		private int width=Integer.MIN_VALUE;

		public ColumnConfiguration(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		public Alignment getAlignment() {
			return alignment;
		}

		public void setAlignment(Alignment alignment) {
			this.alignment = alignment;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}
	}

	public DumpableTable(String... columnTitles){
		this.columnTitles=new String[columnTitles.length];
		this.columnLengths=new int[columnTitles.length];
		for(int i=0;i<columnTitles.length;i++) {
			String title = columnTitles[i];
			this.columnTitles[i]=title;
			this.columnLengths[i]=title.length();
		}
		this.rows=new ArrayList<List<String>>();
		this.configurations=new ColumnConfiguration[columnTitles.length];
		for(int i=0;i<columnTitles.length;i++) {
			this.configurations[i]=new ColumnConfiguration(columnTitles[i]);
		}
	}

	public void setColumnWidth(int column, int witdh) {
		this.configurations[column].setWidth(witdh);
	}

	public void setColumnAlignment(int column, Alignment alignment) {
		this.configurations[column].setAlignment(alignment);
	}

	public void addRow(String... rowValues) {
		if(rowValues.length!=columnTitles.length) {
			throw new IllegalArgumentException("Number of columns does not match. Got "+rowValues.length+" but expected "+columnTitles.length);
		}
		List<String> asList = Arrays.asList(rowValues);
		rows.add(asList);
		for(int i=0;i<rowValues.length;i++) {
			this.columnLengths[i]=Math.max(this.columnLengths[i], rowValues[i].length());
		}

	}

	private String[] adjust(String value, int length) {
		int tModule=value.length()%length;
		int tRows=(value.length()-tModule)/length+(tModule>0?1:0);
		String[] result=new String[tRows];
		for(int i=0;i<result.length;i++) {
			int j = (i+1)*length;
			if(j>value.length()) {
				j=value.length();
			}
			result[i]=value.substring(i*length,j);
		}
		return result;
	}

	private String align(String property, int length, Alignment alignment, char character) {
		String result=property;
		int padLength=length-property.length();
		if(padLength>0) {
			char[] blanksL=null;
			char[] blanksR=null;
			switch(alignment) {
			case CENTER:
				blanksL=new char[padLength/2];
				blanksR=new char[padLength-blanksL.length];
				break;
			case RIGHT:
				blanksL=new char[padLength];
				blanksR=new char[0];
				break;
			default: // Left:
				blanksL=new char[0];
				blanksR=new char[padLength];
				break;
			}
			Arrays.fill(blanksL, character);
			Arrays.fill(blanksR, character);
			result=String.format("%s%s%s",new String(blanksL),result,new String(blanksR));
		} else if(padLength<0) {
			int module=property.length()%length;
			int pad=length-module;
			char[] blanksL=null;
			char[] blanksR=null;
			switch(alignment) {
			case CENTER:
			case LEFT:
				blanksL=new char[0];
				blanksR=new char[pad];
				break;
			default: // Right:
				blanksL=new char[pad];
				blanksR=new char[0];
				break;
			}
			Arrays.fill(blanksL, character);
			Arrays.fill(blanksR, character);
			result=String.format("%s%s%s",new String(blanksL),result,new String(blanksR));
		}
		return result;
	}

	public void sort(boolean sort) {
		this.sort=sort;
	}

	public void dump(PrintStream out) {
		String[] columnFillers=new String[columnTitles.length];
		int[] columnWidths=new int[columnTitles.length];
		for(int i=0;i<columnTitles.length;i++) {
			columnWidths[i] = getColumnWidth(i);
			columnFillers[i]= align("",columnWidths[i],Alignment.LEFT,' ');
		}

		StringBuilder sepBuilder=new StringBuilder();
		sepBuilder.append("+");
		for(int i=0;i<columnTitles.length;i++) {
			sepBuilder.append("-").append(align("",columnWidths[i],Alignment.LEFT,'-')).append("-+");
		}
		String separator = sepBuilder.toString();
		String titleSeparator= separator.replace('-', '=');

		out.println(titleSeparator);
		StringBuilder builder=new StringBuilder();
		builder.append("|");
		for(int i=0;i<columnTitles.length;i++) {
			builder.append(" ").append(align(columnTitles[i],columnWidths[i],Alignment.CENTER,' ')).append(" |");
		}
		out.println(builder.toString());
		out.println(titleSeparator);
		for(List<String> row:sort(rows)) {
			String[][] cellRows=new String[columnTitles.length][];
			int tRows=0;
			for(int i=0;i<columnTitles.length;i++) {
				String cell=
					align(
						row.get(i),
						columnWidths[i],
						configurations[i].getAlignment(),
						' ');
				cellRows[i]=adjust(cell,columnWidths[i]);
				tRows=Math.max(tRows, cellRows[i].length);
			}
			for(int i=0;i<tRows;i++) {
				StringBuilder rowBuilder=new StringBuilder();
				rowBuilder.append("|");
				for(int j=0;j<columnTitles.length;j++) {
					String cell=cellRows[j].length>i?cellRows[j][i]:columnFillers[j];
					rowBuilder.append(" ").append(cell).append(" |");
				}
				out.println(rowBuilder.toString());
			}
			out.println(separator);
		}
	}

	private int getColumnWidth(int i) {
		return configurations[i].getWidth()>0?configurations[i].getWidth():columnLengths[i];
	}

	private List<List<String>> sort(List<List<String>> rows) {
		List<List<String>> result=new ArrayList<List<String>>();
		if(sort) {
			Set<OrderingEntry> sorted=new TreeSet<OrderingEntry>();
			for(int i=0;i<rows.size();i++) {
				sorted.add(new OrderingEntry(rows.get(i).get(0),i));
			}
			for(OrderingEntry entry:sorted) {
				result.add(rows.get(entry.getPosition()));
			}
		} else {
			result.addAll(rows);
		}
		return result;
	}
}