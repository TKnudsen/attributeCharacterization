package com.github.tknudsen.attributeCharacterization.view.panels;

import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;

/**
 * <p>
 * For attributes of type Long.
 * </p>
 *
 * @version 2.0 (optimized)
 * @since 2016
 */
public class NumericalLongAttributeCharacteristicsPanel extends NumericalAttributeCharacteristicsPanel<Long> {

	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an editable panel for long attribute characterization.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Long
	 * @param missingValueIndicator the sentinel value representing missing data
	 */
	public NumericalLongAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Long> parser,
			Long missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	/**
	 * Creates a panel for long attribute characterization with a read-only option.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Long
	 * @param missingValueIndicator the sentinel value representing missing data
	 * @param readOnly if true, the panel does not allow user-driven type changes
	 */
	public NumericalLongAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Long> parser,
			Long missingValueIndicator, boolean readOnly) {
		super(values, parser, missingValueIndicator, readOnly);
	}

	@Override
	protected Class<Long> getClassType() {
		return Long.class;
	}

	@Override
	public String getName() {
		return "Numerical (discrete, long) Properties";
	}

}
