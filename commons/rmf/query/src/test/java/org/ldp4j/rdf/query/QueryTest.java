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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.ldp4j.commons.IndentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunction3;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.ExprVisitor;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.path.P_Alt;
import com.hp.hpl.jena.sparql.path.P_Distinct;
import com.hp.hpl.jena.sparql.path.P_FixedLength;
import com.hp.hpl.jena.sparql.path.P_Inverse;
import com.hp.hpl.jena.sparql.path.P_Link;
import com.hp.hpl.jena.sparql.path.P_Mod;
import com.hp.hpl.jena.sparql.path.P_Multi;
import com.hp.hpl.jena.sparql.path.P_NegPropSet;
import com.hp.hpl.jena.sparql.path.P_OneOrMore1;
import com.hp.hpl.jena.sparql.path.P_OneOrMoreN;
import com.hp.hpl.jena.sparql.path.P_ReverseLink;
import com.hp.hpl.jena.sparql.path.P_Seq;
import com.hp.hpl.jena.sparql.path.P_Shortest;
import com.hp.hpl.jena.sparql.path.P_ZeroOrMore1;
import com.hp.hpl.jena.sparql.path.P_ZeroOrMoreN;
import com.hp.hpl.jena.sparql.path.P_ZeroOrOne;
import com.hp.hpl.jena.sparql.path.PathVisitor;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;

public class QueryTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(QueryTest.class);

	private static final class VariableShadowingDetector implements ExprVisitor {
		private final IndentUtils ind;
		private final Var var;
		private ExprVar shadowed;
		public VariableShadowingDetector(IndentUtils ind, Var var) {
			this.ind = ind;
			this.var = var;
		}
		private void indent(String format, Object... args) {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace(String.format(ind.indent().concat(format),args));
			}
		}
		private void log(Object obj) {
			indent("{%s}:",obj.getClass().getName());
		}


		@Override
		public void visit(ExprAggregator eAgg) {
			log(eAgg);
		}

		@Override
		public void visit(ExprVar nv) {
			log(nv);
			this.shadowed=nv;
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Shadowing detected: %s shadows %s",var,nv));
			}
		}

		@Override
		public void visit(NodeValue nv) {
			log(nv);
		}

		@Override
		public void visit(ExprFunctionOp funcOp) {
			log(funcOp);
		}

		@Override
		public void visit(ExprFunctionN func) {
			log(func);
		}

		@Override
		public void visit(ExprFunction3 func) {
			log(func);
		}

		@Override
		public void visit(ExprFunction2 func) {
			log(func);
		}

		@Override
		public void visit(ExprFunction1 func) {
			log(func);
		}

		@Override
		public void visit(ExprFunction0 func) {
			log(func);
		}

		@Override
		public void startVisit() {
		}

		@Override
		public void finishVisit() {
		}
		public boolean isShadowing() {
			return shadowed!=null;
		}
		public Var getShadowed() {
			return shadowed.asVar();
		}
	}
	private static final class ElementVisitorImplementation implements ElementVisitor {

		private final Set<String> graphs;

		private final IndentUtils ind=new IndentUtils();

		private final Set<Var> variables;

		public ElementVisitorImplementation(List<String> graphNames, Set<Var> variables) {
			this.graphs=Collections.unmodifiableSet(new HashSet<String>(graphNames));
			this.variables=Collections.unmodifiableSet(new HashSet<Var>(variables));
		}

		public void visit(ElementSubQuery arg0) {
			log(arg0);
			arg0.getQuery().getQueryPattern().visit(ElementVisitorImplementation.this);
		}

		public void visit(ElementService arg0) {
			log(arg0);
			arg0.getElement().visit(ElementVisitorImplementation.this);
		}

		public void visit(ElementMinus arg0) {
			log(arg0);
			arg0.getMinusElement().visit(ElementVisitorImplementation.this);
		}

		public void visit(ElementNotExists arg0) {
			log(arg0);
			arg0.getElement().visit(ElementVisitorImplementation.this);
		}

		public void visit(ElementExists arg0) {
			log(arg0);
			arg0.getElement().visit(ElementVisitorImplementation.this);
		}

		public void visit(ElementNamedGraph arg0) {
			log(arg0);
			ind.increase();
			arg0.getElement().visit(this);
			com.hp.hpl.jena.graph.Node graphNameNode = arg0.getGraphNameNode();
			if(graphNameNode.isURI() && graphs.contains(graphNameNode.getURI())) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Need to relocate graph: "+graphNameNode.getURI());
				}
			} else {
				if(graphNameNode.isVariable() && variables.contains((Var)graphNameNode)) {
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("Need to filter result: "+graphNameNode);
					}
				}
			}
			ind.decrease();
		}

		public void visit(ElementDataset arg0) {
			log(arg0);
			ind.increase();
			arg0.getPatternElement().visit(this);
			ind.decrease();
		}

		public void visit(ElementGroup arg0) {
			log(arg0);
			ind.increase();
			for(Element e:arg0.getElements()) {
				e.visit(this);
			}
			ind.decrease();
		}

		public void visit(ElementOptional arg0) {
			log(arg0);
			arg0.getOptionalElement().visit(ElementVisitorImplementation.this);
		}

		public void visit(ElementUnion arg0) {
			log(arg0);
			ind.increase();
			for(Element e:arg0.getElements()) {
				e.visit(this);
			}
			ind.decrease();
		}

		public void visit(ElementData arg0) {
			log(arg0);
		}

		public void visit(ElementBind arg0) {
			log(arg0);
		}

		public void visit(ElementAssign arg0) {
			log(arg0);
		}

		public void visit(ElementFilter arg0) {
			log(arg0);
		}

		public void visit(ElementPathBlock arg0) {
			log(arg0);
			List<TriplePath> list = arg0.getPattern().getList();
			ind.increase();
			for(TriplePath path:list) {
				if(path.isTriple()) {
					indent("{%s}: %s",path.getClass(),path.asTriple());
				}
				if(path.getPath()!=null) {
					path.getPath().visit(
						new PathVisitor() {
							public void visit(P_Link pathNode) {
								log(pathNode);
							}
							public void visit(P_ReverseLink pathNode) {
								log(pathNode);
							}
							public void visit(P_NegPropSet pathNotOneOf) {
								log(pathNotOneOf);
							}
							public void visit(P_Inverse inversePath) {
								log(inversePath);
							}
							public void visit(P_Mod pathMod) {
								log(pathMod);
							}
							public void visit(P_FixedLength pFixedLength) {
								log(pFixedLength);
							}
							public void visit(P_Distinct pathDistinct) {
								log(pathDistinct);
							}
							public void visit(P_Multi pathMulti) {
								log(pathMulti);
							}
							public void visit(P_Shortest pathShortest) {
								log(pathShortest);
							}
							public void visit(P_ZeroOrOne path) {
								log(path);
							}
							public void visit(P_ZeroOrMore1 path) {
								log(path);
							}
							public void visit(P_ZeroOrMoreN path) {
								log(path);
							}
							public void visit(P_OneOrMore1 path) {
								log(path);
							}
							public void visit(P_OneOrMoreN path) {
								log(path);
							}
							public void visit(P_Alt pathAlt) {
								log(pathAlt);
							}
							public void visit(P_Seq pathSeq) {
								log(pathSeq);
							}
						}
					);
				}
			}
			ind.decrease();
		}

		public void visit(ElementTriplesBlock arg0) {
			log(arg0);
		}

		private void indent(String format, Object... args) {
			if(LOGGER.isTraceEnabled()) {
				LOGGER.trace(String.format(ind.indent().concat(format),args));
			}
		}

		private void log(Object obj) {
			indent("{%s}:",obj.getClass().getName());
		}
	}

	String query(String query) {
		LOGGER.info("Using JENA:");
		Query q=QueryFactory.create(query);
		LOGGER.info("-- Query:\n"+q);
		LOGGER.info("-- Base: "+q.getBaseURI());
		LOGGER.info("-- Prefixes: "+q.getPrefixMapping());
		LOGGER.info("-- Variables: "+q.getProjectVars());
		Set<Var> variables=new HashSet<Var>(q.getProjectVars());
		for(Entry<Var, Expr> entry:q.getProject().getExprs().entrySet()) {
			Var shadowing = entry.getKey();
			VariableShadowingDetector detector = new VariableShadowingDetector(new IndentUtils(),shadowing);
			entry.getValue().visit(detector);
			if(detector.isShadowing()) {
				variables.remove(shadowing);
				variables.add(detector.getShadowed());
			}
		}
		LOGGER.info("-- Graph URIS: "+q.getGraphURIs());
		LOGGER.info("-- Named graphd URIS: "+q.getNamedGraphURIs());
		List<String> search=q.getNamedGraphURIs();
		q.getQueryPattern().visit(new ElementVisitorImplementation(search,variables));
		return null;
	}


	@Test
	public void testQueryParser() throws Exception {
		String varNamePattern = "(\\w+)";
		String varPattern = "(\\?(\\w+))";
		String selectPattern = "SELECT((\\s+(\\?(\\w+)))+)";
		ChainedFilterExtractor extractor=new ChainedFilterExtractor(selectPattern,varPattern,varNamePattern);
		extractor.extract(loadResource("unrestricted_graph_names.sparql"));
		extractor.extract(loadResource("restricted_graph_names.sparql"));
	}

	private String loadResource(String resource) throws IOException {
		return IOUtils.toString(ClassLoader.getSystemResource(resource));
	}

	@Test
	public void testFullQuery() throws Exception {
		query(loadResource("all_cases.sparql"));
	}

	@Test
	public void testUnrestrictedQuery() throws Exception {
		query(loadResource("unrestricted_graph_names.sparql"));
	}

	@Test
	public void testRestrictedQuery() throws Exception {
		query(loadResource("restricted_graph_names.sparql"));
	}

}