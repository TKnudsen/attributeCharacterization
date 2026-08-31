package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartHorizontalValueBased;

/**
 * <p>
 * Panel for categorical attribute characterization. Displays
 * frequency distribution as horizontal bar chart.
 * </p>
 *
 * @version 2.0 (revised)
 * @since 2016
 */
public class CategoricalAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<String> {

	private static final long serialVersionUID = -1617737257897086025L;

	/**
	 * Cached cardinality (number of unique categories), computed by
	 * {@link #addContentToValueDistributionPanel()} and kept in sync across
	 * resets by the base class's {@code onParsedValuesReset()} hook.
	 */
	private volatile int cardinality;

	// ==================== CONSTRUCTORS ====================

	/**
	 * Creates an editable panel for categorical attribute characterization.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to String
	 * @param missingValueIndicator the sentinel value representing missing data
	 */
	public CategoricalAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<String> parser,
			String missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	/**
	 * Creates a panel for categorical attribute characterization with a read-only option.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to String
	 * @param missingValueIndicator the sentinel value representing missing data
	 * @param readOnly if true, the panel does not allow user-driven type changes
	 */
	public CategoricalAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<String> parser,
			String missingValueIndicator, boolean readOnly) {
		super(values, parser, missingValueIndicator, readOnly);
	}

	// ==================== IMPLEMENTATION ====================

	@Override
	protected void addContentToValueDistributionPanel() {
		List<String> parsed = getParsedValues();

		if (parsed.isEmpty()) {
			this.cardinality = 0;
			valueDistributionPanel.add(new JLabel("No valid categorical values"), BorderLayout.CENTER);
			return;
		}

		// Count frequencies efficiently
		Map<String, Integer> counts = countFrequencies(parsed);
		this.cardinality = counts.size();

		// Create bar chart
		BarChartHorizontalValueBased chart = createBarChart(counts);
		chart.setBackground(null);
		valueDistributionPanel.add(chart, BorderLayout.CENTER);

		// Add cardinality label
		double distinctnessRatio = cardinality / (double) parsed.size();
		JLabel cardinalityLabel = new JLabel("cardinality/size: " + MathFunctions.round(distinctnessRatio, 3));
		valueDistributionPanel.add(cardinalityLabel, BorderLayout.NORTH);
	}

	/**
	 * Count frequency of each category efficiently.
	 */
	private Map<String, Integer> countFrequencies(List<String> values) {
		int capacity = capacityForExpectedSize(values.size());
		Map<String, Integer> counts = new HashMap<>(capacity);

		for (String value : values) {
			counts.merge(value, 1, Integer::sum);
		}

		return counts;
	}

	/**
	 * Create bar chart from frequency counts.
	 */
	private BarChartHorizontalValueBased createBarChart(Map<String, Integer> counts) {
		// Convert to chart data
		List<Double> data = new ArrayList<>(counts.size());
		List<Color> colors = new ArrayList<>(counts.size());

		for (Integer count : counts.values()) {
			data.add(count.doubleValue());
			colors.add(DEFAULT_BAR_COLOR);
		}

		return new BarChartHorizontalValueBased(data, colors);
	}

	/**
	 * Calculate optimal HashMap capacity to avoid rehashing. Formula: ceil(n /
	 * 0.75) + 1
	 */
	private static int capacityForExpectedSize(int expectedSize) {
		if (expectedSize <= 0) {
			return 16;
		}
		int cap = (expectedSize * 4) / 3 + 1;
		return Math.max(cap, 16);
	}

	@Override
	protected boolean applySpecificFilter(Object o) {
		// Exclude non-integer doubles (continuous data shouldn't be categorical)
		if (o instanceof Double d) {
			return !Double.isFinite(d) || d != Math.rint(d);
		}
		return false;
	}

	@Override
	protected Class<String> getClassType() {
		return String.class;
	}

	@Override
	public String getName() {
		return "Categorical Properties";
	}

	// ==================== GETTERS ====================

	/**
	 * Returns the cardinality (number of unique categories).
	 *
	 * @return number of distinct category values
	 */
	public int getCardinality() {
		return cardinality;
	}
}