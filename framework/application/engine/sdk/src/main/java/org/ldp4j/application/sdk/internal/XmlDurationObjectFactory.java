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
 *   Artifact    : org.ldp4j.framework:ldp4j-application-engine-sdk:0.2.2
 *   Bundle      : ldp4j-application-engine-sdk-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.application.sdk.internal;

import javax.xml.datatype.Duration;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.ldp4j.application.data.Literals;
import org.ldp4j.application.data.TimeUtils;
import org.ldp4j.application.sdk.spi.ObjectFactory;
import org.ldp4j.application.sdk.spi.ObjectParseException;

public class XmlDurationObjectFactory implements ObjectFactory<Duration> {

	@Override
	public Class<? extends Duration> targetClass() {
		return Duration.class;
	}

	@Override
	public Duration fromString(String rawValue) {
		try {
			Period period = ISOPeriodFormat.standard().parsePeriod(rawValue);
			return TimeUtils.newInstance().from(period.toStandardDuration()).toDuration();
		} catch (Exception e) {
			throw new ObjectParseException(e,Duration.class,rawValue);
		}
	}

	@Override
	public String toString(javax.xml.datatype.Duration value) {
		return Literals.of(value).get().toString();
	}

}