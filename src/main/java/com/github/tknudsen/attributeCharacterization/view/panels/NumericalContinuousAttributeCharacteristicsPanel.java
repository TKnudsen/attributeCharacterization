package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalChartPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DPanels;

/**
 * <p>
 * Panel for continuous numerical (Double) attribute characterization.
 * </p>
 *
 * <b>Special Features:</b>
 * <ul>
 * <li>Interactive check box for European number format (dot = thousands separator)</li>
 * <li>Dynamic re-parsing when format changes</li>
 * <li>Box plot and distribution visualization</li>
 * </ul>
 *
 * @version 2.0 (revised)
 * @since 2016
 */
public class NumericalContinuousAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Double> {

	private static final long serialVersionUID = -1617737257897086025L;

	// UI Components
	private JPanel contentPanel;
	private JCheckBox dotMeansThousandsCheckBox;

	// ==================== CONSTRUCTORS ====================

	/**
	 * Creates an editable panel for continuous numerical attribute characterization.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Double
	 * @param missingValueIndicator the sentinel value representing missing data
	 */
	public NumericalContinuousAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Double> parser,
			Double missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	/**
	 * Creates a panel for continuous numerical attribute characterization with a read-only option.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Double
	 * @param missingValueIndicator the sentinel value representing missing data
	 * @param readOnly if true, the panel does not allow user-driven type changes
	 */
	public NumericalContinuousAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Double> parser,
			Double missingValueIndicator, boolean readOnly) {
		super(values, parser, missingValueIndicator, readOnly);
	}

	// ==================== IMPLEMENTATION ====================

	@Override
	protected void addContentToValueDistributionPanel() {
		// Create check box for number format configuration
		dotMeansThousandsCheckBox = createFormatCheckbox();
		valueDistributionPanel.add(dotMeansThousandsCheckBox, BorderLayout.NORTH);

		// Create content panel (will be refreshed on format change)
		contentPanel = new JPanel(new GridLayout(2, 1));
		refreshContentPanel();
		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);
	}

	/**
	 * Create check box for European number format configuration.
	 */
	private JCheckBox createFormatCheckbox() {
		JCheckBox checkbox = new JCheckBox("Dot means Thousand", false);

		// Initialize from current parser state
		if (getParser() instanceof DoubleParser parser) {
			checkbox.setSelected(parser.isDotMeansThousands());
		}

		// Add listener to handle format changes
		checkbox.addActionListener(e -> handleFormatChange(checkbox.isSelected()));

		return checkbox;
	}

	/**
	 * Handle change in number format setting.
	 */
	private void handleFormatChange(boolean dotMeansThousands) {
		IObjectParser<Double> parser = getParser();

		if (!(parser instanceof DoubleParser)) {
			System.err.println("NumericalContinuousAttributeCharacteristicsPanel: "
					+ "Parser is not a DoubleParser, cannot change format");
			return;
		}

		// Update parser configuration
		((DoubleParser) parser).setDotMeansThousands(dotMeansThousands);

		// Re-parse with new format; resetParsedValues() refreshes the
		// visualization (via onParsedValuesReset()) and repaints on its own
		resetParsedValues();
	}

	/**
	 * Refresh the visualization panel with current parsed values.
	 */
	private void refreshContentPanel() {
		contentPanel.removeAll();

		Collection<Double> parsedValues = getParsedValues();

		if (parsedValues.isEmpty()) {
			contentPanel.add(new JLabel("No valid numerical values"));
			return;
		}

		// Create statistics
		StatisticsSupport dataStatistics = new StatisticsSupport(parsedValues);

		// Boxplot
		BoxPlotHorizontalChartPanel boxplot = new BoxPlotHorizontalChartPanel(dataStatistics);
		boxplot.setBackground(null);
		contentPanel.add(boxplot);

		// Distribution
		Distribution1DHorizontalPanel<Double> distribution = (Distribution1DHorizontalPanel<Double>) Distribution1DPanels
				.createForDoubles(parsedValues, false);
		distribution.setBackground(null);
		contentPanel.add(distribution);
	}

	@Override
	protected Class<Double> getClassType() {
		return Double.class;
	}

	@Override
	public String getName() {
		return "Numerical (continuous) Properties";
	}

	@Override
	protected boolean testForMissingValue(Double missingValueIndicator, Double value) {
		// Special handling for NaN
		if (missingValueIndicator == null) {
			return value == null;
		}

		if (Double.isNaN(missingValueIndicator)) {
			return Double.isNaN(value);
		}

		// Standard equality check
		return missingValueIndicator.equals(value);
	}

	// ==================== PUBLIC API ====================

	/**
	 * Returns the current dot-means-thousands setting.
	 *
	 * @return true if dots are treated as thousands separators
	 */
	public boolean isDotMeansThousands() {
		if (getParser() instanceof DoubleParser) {
			return ((DoubleParser) getParser()).isDotMeansThousands();
		}
		return false;
	}

	/**
	 * Sets the dot-means-thousands format programmatically.
	 *
	 * @param dotMeansThousands if true, dots are treated as thousands separators
	 */
	public void setDotMeansThousands(boolean dotMeansThousands) {
		if (dotMeansThousandsCheckBox != null) {
			dotMeansThousandsCheckBox.setSelected(dotMeansThousands);
			handleFormatChange(dotMeansThousands);
		}
	}

	/**
	 * Overridden (rather than {@code resetParsedValues()} itself) so that only
	 * the box plot/distribution content is rebuilt on reset -- the format
	 * checkbox is preserved instead of being recreated from scratch.
	 */
	@Override
	protected void onParsedValuesReset() {
		refreshContentPanel();
	}
}
