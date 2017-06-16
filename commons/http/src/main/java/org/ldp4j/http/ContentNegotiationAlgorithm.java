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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-http:0.2.2
 *   Bundle      : ldp4j-commons-http-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.http;

import java.util.List;
import java.util.SortedSet;

import org.ldp4j.http.ImmutableAlternatives.Builder;
import org.ldp4j.http.Quality.Type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

final class ContentNegotiationAlgorithm {

	private final ImmutableList<MediaType> mediaTypes;
	private final ImmutableList<CharacterEncoding> characterEncodings;
	private final ImmutableList<Language> languages;
	private final ImmutableList<Alternative> supported;

	private final ImmutableList<Weighted<MediaType>> accepts;
	private final ImmutableList<Weighted<CharacterEncoding>> acceptCharsets;
	private final ImmutableList<Weighted<Language>> acceptLanguages;

	private final ImmutableVariant errorVariant;
	private ImmutableAlternatives alternatives;

	ContentNegotiationAlgorithm(  // NOSONAR
			ImmutableList<MediaType> mediaTypes,
			ImmutableList<CharacterEncoding> characterEncodings,
			ImmutableList<Language> languages,
			ImmutableList<Alternative> supported,
			ImmutableList<Weighted<MediaType>> accepts,
			ImmutableList<Weighted<CharacterEncoding>> acceptCharsets,
			ImmutableList<Weighted<Language>> acceptLanguages,
			ImmutableVariant errorVariant) {
		this.mediaTypes=mediaTypes;
		this.characterEncodings=characterEncodings;
		this.languages=languages;
		this.supported=supported;
		this.accepts=accepts;
		this.acceptCharsets=acceptCharsets;
		this.acceptLanguages=acceptLanguages;
		this.errorVariant=errorVariant;
	}

	NegotiationResult execute() {
		final SortedSet<AlternativeEvaluation> ranking=rankAlternatives();
		final AlternativeEvaluation selection=selectAlternative(ranking);
		return assembleResult(selection);
	}

	private AlternativeEvaluation selectAlternative(final SortedSet<AlternativeEvaluation> ranking) {
		AlternativeEvaluation definite=null;
		AlternativeEvaluation speculative=null;
		for(AlternativeEvaluation calculation:ranking) {
			definite=select(definite, calculation, Quality.Type.DEFINITE);
			speculative=select(speculative, calculation, Quality.Type.SPECULATIVE);
			if(definite!=null && speculative!=null) {
				break;
			}
		}
		return definite!=null?definite:speculative;
	}

	private AlternativeEvaluation select(AlternativeEvaluation current, AlternativeEvaluation candidate,Type type) {
		if(current!=null) {
			return current;
		}
		if(candidate.quality().type().equals(type)) {
			return candidate;
		}
		return null;
	}

	private ImmutableNegotiationResult assembleResult(final AlternativeEvaluation calculation) {
		ImmutableVariant variant = null;
		ImmutableQuality quality = null;
		if(calculation!=null) {
			variant=calculation.variant();
			quality=calculation.quality();
		}
		return
			new ImmutableNegotiationResult(
				variant,
				quality,
				this.errorVariant,
				this.alternatives);
	}

	private SortedSet<AlternativeEvaluation> rankAlternatives() {
		final Builder alternativesBuilder=ImmutableAlternatives.builder();
		final SortedSet<AlternativeEvaluation> sortedCalculations=Sets.newTreeSet(AlternativeEvaluation.COMPARATOR);
		for(AlternativeEvaluation calculation:createCalculations()) {
			calculation.evaluate(this.accepts,this.acceptCharsets,this.acceptLanguages);
			if(Double.compare(0D, calculation.quality().weight())!=0) {
				sortedCalculations.add(calculation);
			}
			alternativesBuilder.add(calculation.quality(),calculation.alternative());
		}
		this.alternatives=alternativesBuilder.build();
		return sortedCalculations;
	}

	private List<AlternativeEvaluation> createCalculations() {
		final List<AlternativeEvaluation> calculations=Lists.newArrayList();
		populatePredefinedAlternatives(calculations);
		populateGeneratedAlternatives(calculations);
		if(calculations.isEmpty()) {
			throw new CannotNegotiateException();
		}
		return calculations;
	}

	private void populateGeneratedAlternatives(final List<AlternativeEvaluation> calculations) {
		final AlternativeProvider provider=new AlternativeProvider(this.mediaTypes, this.characterEncodings, this.languages);
		while(provider.hasNext()) {
			ImmutableAlternative alternative = provider.next();
			if(!matchesPredefinedAlternative(alternative)) {
				calculations.add(AlternativeEvaluation.generated(alternative));
			}
		}
	}

	private void populatePredefinedAlternatives(final List<AlternativeEvaluation> alternatives) {
		for(Alternative alternative:this.supported) {
			alternatives.add(AlternativeEvaluation.predefined(alternative));
		}
	}

	private boolean matchesPredefinedAlternative(final ImmutableAlternative alternative) {
		for(Alternative predefined:this.supported) {
			if(Variants.equals(alternative,predefined)) {
				return true;
			}
		}
		return false;
	}

}