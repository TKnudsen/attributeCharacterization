package com.github.tknudsen.attributeCharacterization.data.events;

import java.awt.event.ActionEvent;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;

/**
 * <p>
 * AttributeCharacterization
 * </p>
 * 
 * <p>
 * Decision action event, used to trigger the end of the characterization of a
 * single attribute.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2025 Juergen Bernard,
 * https://github.com/TKnudsen/AttributeCharacterization
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class AttributeTypeDecisionActionEvent<T> extends ActionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2411969924360735247L;

	private final Class<T> attributeType;
	private IObjectParser<T> parser;

	public AttributeTypeDecisionActionEvent(Object source, int id, String command, Class<T> attributeType) {
		this(source, id, command, attributeType, null);
	}

	public AttributeTypeDecisionActionEvent(Object source, int id, String command, Class<T> attributeType,
			IObjectParser<T> parser) {
		super(source, id, command);

		this.attributeType = attributeType;
		this.parser = parser;
	}

	public Class<T> getAttributeType() {
		return attributeType;
	}

	public IObjectParser<T> getParser() {
		return parser;
	}

	public void setParser(IObjectParser<T> parser) {
		this.parser = parser;
	}

}
