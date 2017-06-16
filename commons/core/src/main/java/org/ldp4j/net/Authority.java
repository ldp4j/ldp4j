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
 *   Artifact    : org.ldp4j.commons:ldp4j-commons-core:0.2.2
 *   Bundle      : ldp4j-commons-core-0.2.2.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.ldp4j.net;

import java.util.Objects;

public final class Authority {

	private static final int DEFAULT_SCHEME_PORT = -1;

	private String userInfo;
	private String host;
	private int port;

	private Authority(String host) {
		setHost(host);
		setPort(DEFAULT_SCHEME_PORT);
	}

	private Authority(Authority authority) {
		this(authority.host);
		setPort(authority.port);
		setUserInfo(authority.userInfo);
	}

	private void setHost(String host) {
		if(host==null || host.trim().isEmpty()) {
			throw new IllegalArgumentException("Host cannot be empty");
		}
		this.host = host;
	}

	private void setPort(int port) {
		if(port>Short.MAX_VALUE) {
			throw new IllegalArgumentException("Port cannot be bigger than "+Short.MAX_VALUE);
		}
		int cPort=port;
		if(cPort<0) {
			cPort=DEFAULT_SCHEME_PORT;
		}
		this.port = cPort;
	}

	private void setUserInfo(String userInfo) {
		this.userInfo=userInfo;
	}

	public String getUserInfo() {
		return this.userInfo;
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public boolean isDefaultSchemePort() {
		return this.port==DEFAULT_SCHEME_PORT;
	}

	public Authority withHost(String host) {
		Authority result = new Authority(this);
		result.setHost(host);
		return result;
	}

	public Authority withPort(int port) {
		Authority result = new Authority(this);
		result.setPort(port);
		return result;
	}

	public Authority withUserInfo(String userInfo) {
		if(userInfo==null || userInfo.trim().isEmpty()) {
			throw new IllegalArgumentException("User info cannot be null nor empty");
		}
		Authority result = new Authority(this);
		result.setUserInfo(userInfo);
		return result;
	}

	public Authority withoutUserInfo() {
		Authority result = new Authority(this);
		result.setUserInfo(null);
		return result;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.host,this.port,this.userInfo);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Authority) {
			Authority that=(Authority)obj;
			result=
				Objects.equals(this.host,that.host) &&
				Objects.equals(this.port,that.port) &&
				Objects.equals(this.userInfo,that.userInfo);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder=new StringBuilder();
		if(this.userInfo!=null && !this.userInfo.isEmpty()) {
			builder.append(this.userInfo);
			builder.append("@");
		}
		builder.append(this.host);
		if(!isDefaultSchemePort()) {
			builder.append(":");
			builder.append(this.port);
		}
		return builder.toString();
	}

	public static Authority forHost(String host) {
		return new Authority(host);
	}

	public static Authority create(java.net.URI jdkURI) {
		Authority result=null;
		if(jdkURI.getAuthority()!=null) {
			result=new Authority(jdkURI.getHost());
			result.setUserInfo(jdkURI.getUserInfo());
			result.setPort(jdkURI.getPort());
		}
		return result;
	}

}