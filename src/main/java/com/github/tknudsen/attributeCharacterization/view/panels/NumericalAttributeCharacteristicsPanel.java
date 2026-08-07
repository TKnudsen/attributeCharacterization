package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalChartPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DPanels;

/**
 * <p>
 * Panel for numerical attribute characterization. Displays box
 * plot and distribution visualization. Common base for all numerical types.
 * </p>
 *
 * @param <N> Numerical type (Integer, Long, Double)
 *
 * @version 2.0 (revised)
 * @since 2016
 */
public abstract class NumericalAttributeCharacteristicsPanel<N extends Number>
		extends AttributeCharacteristicsPanel<N> {

	private static final long serialVersionUID = 1L;

	// ==================== CONSTRUCTORS ====================

	/**
	 * Creates an editable numerical attribute characterization panel.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to type N
	 * @param missingValueIndicator the sentinel value representing missing data
	 */
	public NumericalAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<N> parser,
			N missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	/**
	 * Creates a numerical attribute characterization panel with a read-only option.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to type N
	 * @param missingValueIndicator the sentinel value representing missing data
	 * @param readOnly if true, the panel does not allow user-driven type changes
	 */
	public NumericalAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<N> parser,
			N missingValueIndicator, boolean readOnly) {
		super(values, parser, missingValueIndicator, readOnly);
	}

	// ==================== IMPLEMENTATION ====================

	@Override
	protected void addContentToValueDistributionPanel() {
		Collection<N> parsedValues = getParsedValues();

		if (parsedValues.isEmpty()) {
			valueDistributionPanel.add(new JLabel("No valid numerical values"), BorderLayout.CENTER);
			return;
		}

		// Create visualization panel
		JPanel contentPanel = createVisualizationPanel(parsedValues);
		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates a panel containing a box plot and a 1D distribution chart.
	 *
	 * @param parsedValues the parsed numerical values to visualize
	 * @return panel with box plot and distribution visualization
	 */
	protected JPanel createVisualizationPanel(Collection<N> parsedValues) {
		JPanel contentPanel = new JPanel(new GridLayout(2, 1));

		// Box plot
		StatisticsSupport stats = new StatisticsSupport(parsedValues);
		BoxPlotHorizontalChartPanel boxplot = new BoxPlotHorizontalChartPanel(stats);
		boxplot.setBackground(null);
		contentPanel.add(boxplot);

		// Distribution
		List<Double> doubleValues = DataConversion.listWithNumbersToDoubleList(parsedValues);
		Distribution1DHorizontalPanel<Double> distribution = (Distribution1DHorizontalPanel<Double>) Distribution1DPanels
				.createForDoubles(doubleValues, false);
		distribution.setBackground(null);
		contentPanel.add(distribution);

		return contentPanel;
	}
}