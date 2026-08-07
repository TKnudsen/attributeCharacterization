package com.github.tknudsen.attributeCharacterization.view.panels;

import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;

/**
 * <p>
 * Panel for discrete integer attribute characterization.
 * </p>
 *
 * @version 2.0
 * @since 2016
 */
public class NumericalIntegerAttributeCharacteristicsPanel extends NumericalAttributeCharacteristicsPanel<Integer> {

	// ==================== CONSTRUCTORS ====================

	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an editable panel for integer attribute characterization.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Integer
	 * @param missingValueIndicator the sentinel value representing missing data
	 */
	public NumericalIntegerAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Integer> parser,
			Integer missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	/**
	 * Creates a panel for integer attribute characterization with a read-only option.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Integer
	 * @param missingValueIndicator the sentinel value representing missing data
	 * @param readOnly if true, the panel does not allow user-driven type changes
	 */
	public NumericalIntegerAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Integer> parser,
			Integer missingValueIndicator, boolean readOnly) {
		super(values, parser, missingValueIndicator, readOnly);
	}

	// ==================== IMPLEMENTATION ====================

	@Override
	protected Class<Integer> getClassType() {
		return Integer.class;
	}

	@Override
	public String getName() {
		return "Numerical (discrete, integer) Properties";
	}
}
