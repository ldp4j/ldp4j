package org.ldp4j.application;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class UnsupportedInteractionModelException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5693006810913606248L;

	private final InteractionModel requiredInteractionModel;
	private final Set<InteractionModel> supportedInteractionModels;

	public UnsupportedInteractionModelException(InteractionModel requiredInteractionModel, Set<InteractionModel> supportedInteractionModels) {
		super("Unsupported interaction model '"+requiredInteractionModel+"'. The resource only supports: "+supportedInteractionModels);
		this.requiredInteractionModel = requiredInteractionModel;
		this.supportedInteractionModels = ImmutableSet.copyOf(supportedInteractionModels);
	}

	public InteractionModel getRequiredInteractionModel() {
		return requiredInteractionModel;
	}

	public Set<InteractionModel> getSupportedInteractionModels() {
		return supportedInteractionModels;
	}

}
