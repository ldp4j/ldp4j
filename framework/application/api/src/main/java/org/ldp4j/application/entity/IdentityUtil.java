package org.ldp4j.application.entity;

import java.util.Comparator;

final class IdentityUtil {

	private static final class IdentityComparator implements Comparator<Identity> {

		@Override
		public int compare(Identity o1, Identity o2) {
			if(o1==null && o2!=null) {
				return -1;
			}
			if(o1!=null && o2==null) {
				return 1;
			}
			if(o1==null && o2==null) {
				return 0;
			}
			return o1.identifier().toString().compareTo(o2.toString());
		}

	}

	private IdentityUtil() {
	}

	static int compare(Identity one, Identity another) {
		return new IdentityComparator().compare(one, another);
	}

}
