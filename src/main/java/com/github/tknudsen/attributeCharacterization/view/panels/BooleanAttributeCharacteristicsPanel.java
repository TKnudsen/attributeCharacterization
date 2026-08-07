package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.panels.barchart.BarChartHorizontal;

/**
 * <p>
 * Panel for boolean attribute characterization.
 * Displays frequency distribution of true/false values as a horizontal bar chart.
 * </p>
 *
 * <b>Features:</b>
 * <ul>
 * <li>Preserves insertion order (true/false order)</li>
 * <li>Shows cardinality ratio</li>
 * <li>Visual frequency comparison</li>
 * </ul>
 *
 * @version 2.0 (revised)
 * @since 2016
 */
public class BooleanAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Boolean> {

	private static final long serialVersionUID = -1617737257897086025L;

	// ==================== CONSTRUCTORS ====================

	/**
	 * Creates an editable panel for boolean attribute characterization.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Boolean
	 */
	public BooleanAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Boolean> parser) {
		super(values, parser, null); // null missing value indicator
	}

	/**
	 * Creates a panel for boolean attribute characterization with a read-only option.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Boolean
	 * @param readOnly if true, the panel does not allow user-driven type changes
	 */
	public BooleanAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Boolean> parser,
			boolean readOnly) {
		super(values, parser, null, readOnly);
	}

	// ==================== IMPLEMENTATION ====================

	@Override
	protected void addContentToValueDistributionPanel() {
		var parsedValues = getParsedValues();

		if (parsedValues.isEmpty()) {
			valueDistributionPanel.add(new JLabel("No valid boolean values"), BorderLayout.CENTER);
			return;
		}

		// Count frequencies (preserving order)
		var counts = countFrequencies(parsedValues);

		if (counts.isEmpty()) {
			valueDistributionPanel.add(new JLabel("No boolean values after filtering"), BorderLayout.CENTER);
			return;
		}

		// Create bar chart
		var chart = createBarChart(counts);
		chart.setBackground(null);
		valueDistributionPanel.add(chart, BorderLayout.CENTER);

		// Add cardinality label
		double cardinalityRatio = counts.size() / (double) parsedValues.size();
		JLabel cardinalityLabel = new JLabel("cardinality/size: " + MathFunctions.round(cardinalityRatio, 3));
		valueDistributionPanel.add(cardinalityLabel, BorderLayout.NORTH);
	}

	/**
	 * Count frequency of true/false values. Uses LinkedHashMap to preserve order
	 * (typically true, then false).
	 */
	private Map<Boolean, Integer> countFrequencies(List<Boolean> values) {
		Map<Boolean, Integer> counts = new LinkedHashMap<>();

		for (Boolean value : values) {
			counts.merge(value, 1, Integer::sum);
		}

		return counts;
	}

	/**
	 * Create bar chart from frequency counts.
	 */
	private BarChartHorizontal createBarChart(Map<Boolean, Integer> counts) {
		List<Double> data = new ArrayList<>(counts.size());
		List<Color> colors = new ArrayList<>(counts.size());

		for (Integer count : counts.values()) {
			data.add(count.doubleValue());
			colors.add(DEFAULT_BAR_COLOR);
		}

		return new BarChartHorizontal(data, colors);
	}

	@Override
	protected Class<Boolean> getClassType() {
		return Boolean.class;
	}

	@Override
	public String getName() {
		return "Boolean Properties";
	}

	@Override
	protected boolean testForMissingValue(Boolean missingValueIndicator, Boolean value) {
		// For booleans, null is the only missing value indicator
		if (missingValueIndicator == null) {
			return value == null;
		}
		return missingValueIndicator.equals(value);
	}

	// ==================== PUBLIC API ====================

	/**
	 * Returns the count of true values.
	 *
	 * @return number of true entries
	 */
	public int getTrueCount() {
		List<Boolean> values = getParsedValues();
		return (int) values.stream().filter(b -> b).count();
	}

	/**
	 * Returns the count of false values.
	 *
	 * @return number of false entries
	 */
	public int getFalseCount() {
		List<Boolean> values = getParsedValues();
		return (int) values.stream().filter(b -> !b).count();
	}

	/**
	 * Returns the ratio of true values in [0.0, 1.0].
	 *
	 * @return fraction of parsed values that are true
	 */
	public double getTrueRatio() {
		var values = getParsedValues();
		if (values.isEmpty())
			return 0.0;
		long trueCount = values.stream().filter(b -> b).count();
		return (double) trueCount / values.size();
	}

	/**
	 * Check if data is balanced (roughly 50/50 split).
	 * 
	 * @param threshold Acceptable deviation from 0.5 (default: 0.1)
	 * @return true if ratio is within threshold of 0.5
	 */
	public boolean isBalanced(double threshold) {
		double ratio = getTrueRatio();
		return Math.abs(ratio - 0.5) <= threshold;
	}

	/**
	 * Returns whether data is balanced with the default threshold (10%).
	 *
	 * @return true if the true-value ratio is within 0.1 of 0.5
	 */
	public boolean isBalanced() {
		return isBalanced(0.1);
	}
}
