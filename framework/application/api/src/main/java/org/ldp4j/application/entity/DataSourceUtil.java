package org.ldp4j.application.entity;

import org.ldp4j.application.data.Name;


public final class DataSourceUtil {

	private static final String ENTITY_FIELD_PREFIX = "  ";
	private static final String ENTITY_OPENING = ") {";
	private static final String ENTITY_CLOSING = "}";
	private static final String TAB = "\t";
	private static final String NL=System.getProperty("line.separator");

	private DataSourceUtil() {

	}

	public static String formatLiteral(Literal<?> literal) {
		Object value = literal.value();
		return String.format("%s [%s]",literal.value(),value.getClass().getName());
	}

	public static String formatName(Name<?> tmp) {
		if(tmp==null) {
			return "<null>";
		}
		return String.format("%s [%s]",tmp.id(),tmp.id().getClass().getName());
	}

	public static String toString(DataSource dataSource) {
		if(dataSource==null) {
			return null;
		}
		final StringBuilder builder=new StringBuilder();
		builder.append("DataSource(").append(dataSource.identifier()).append(ENTITY_OPENING).append(NL);
		for(Entity entity:dataSource) {
			builder.append(TAB).append("- Entity(").append(entity.identifier()).append(ENTITY_OPENING).append(NL);
			builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append("+ Identity: ").append(entity.identity()).append(NL);
			for(Property property:entity) {
				builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append("+ Property(").append(property.predicate()).append(ENTITY_OPENING).append(NL);
				for(Value value:property) {
					builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append("- ");
					value.accept(
						new ValueVisitor() {
							@Override
							void visitLiteral(Literal<?> value) {
								builder.append(formatLiteral(value));
							}
							@Override
							void visitEntity(final Entity entity) {
								entity.identity().accept(
									new IdentityVisitor() {
										@Override
										public void visitLocal(Local<?> identity) {
											builder.append(identity.toString());
										}
										@Override
										public void visitManaged(Managed<?> identity) {
											builder.append(identity.toString());
										}
										@Override
										public void visitExternal(External identity) {
											builder.append(identity.toString());
										}
									}
								);
							}
						}
					);
					builder.append(NL);
				}
				builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(TAB).append(ENTITY_FIELD_PREFIX).append(ENTITY_CLOSING).append(NL);
			}
			builder.append(TAB).append(ENTITY_FIELD_PREFIX).append(ENTITY_CLOSING).append(NL);
		}
		builder.append(ENTITY_CLOSING);
		return builder.toString();
	}

}
