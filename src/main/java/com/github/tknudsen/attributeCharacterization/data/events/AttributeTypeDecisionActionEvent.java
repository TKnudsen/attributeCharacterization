package com.github.tknudsen.attributeCharacterization.data.events;

import java.awt.event.ActionEvent;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;

/**
 * <p>
 * Decision action event used to signal the end of the characterization of a
 * single attribute, carrying the chosen type and parser.
 * </p>
 *
 * @param <T> the accepted attribute value type
 *
 * @version 1.01
 * @since 2016
 */
public class AttributeTypeDecisionActionEvent<T> extends ActionEvent {

	/** Serial version UID. */
	private static final long serialVersionUID = 2411969924360735247L;

	/** The accepted attribute class type. */
	private final Class<T> attributeType;
	/** The parser selected for converting raw values to type T. */
	private IObjectParser<T> parser;

	/**
	 * Creates an event without a parser (type accepted, parsing ignored).
	 *
	 * @param source        the event source
	 * @param id            the event id
	 * @param command       the action command string
	 * @param attributeType the accepted attribute class type
	 */
	public AttributeTypeDecisionActionEvent(Object source, int id, String command, Class<T> attributeType) {
		this(source, id, command, attributeType, null);
	}

	/**
	 * Creates an event with a specific parser.
	 *
	 * @param source        the event source
	 * @param id            the event id
	 * @param command       the action command string
	 * @param attributeType the accepted attribute class type
	 * @param parser        the parser used to convert raw objects to type T
	 */
	public AttributeTypeDecisionActionEvent(Object source, int id, String command, Class<T> attributeType,
			IObjectParser<T> parser) {
		super(source, id, command);

		this.attributeType = attributeType;
		this.parser = parser;
	}

	/**
	 * Returns the accepted attribute class type.
	 *
	 * @return the attribute class type
	 */
	public Class<T> getAttributeType() {
		return attributeType;
	}

	/**
	 * Returns the selected parser.
	 *
	 * @return the object parser, or null if parsing was ignored
	 */
	public IObjectParser<T> getParser() {
		return parser;
	}

	/**
	 * Sets the parser.
	 *
	 * @param parser the parser used to convert raw objects to type T
	 */
	public void setParser(IObjectParser<T> parser) {
		this.parser = parser;
	}

}
