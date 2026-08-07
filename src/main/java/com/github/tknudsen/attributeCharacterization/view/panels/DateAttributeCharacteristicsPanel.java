package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalChartPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DPanels;

/**
 * <p>
 * Panel for date/timestamp attribute characterization.
 * </p>
 *
 * <b>Features:</b>
 * <ul>
 * <li>Converts dates to millisecond timestamps for visualization</li>
 * <li>Displays boxplot showing temporal distribution</li>
 * <li>Shows 1D distribution of dates</li>
 * <li>Provides date range statistics</li>
 * </ul>
 *
 * @version 2.0 (revised)
 * @since 2016
 */
public class DateAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Date> {

	private static final long serialVersionUID = -202069619057179336L;

	// Date formatting
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// Cached statistics
	private volatile Date minDate;
	private volatile Date maxDate;
	private volatile long dateRangeMillis;

	// ==================== CONSTRUCTORS ====================

	/**
	 * Creates an editable panel for date attribute characterization.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Date
	 */
	public DateAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Date> parser) {
		super(values, parser, null); // null missing value indicator
	}

	/**
	 * Creates a panel for date attribute characterization with a read-only option.
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to Date
	 * @param readOnly if true, the panel does not allow user-driven type changes
	 */
	public DateAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Date> parser, boolean readOnly) {
		super(values, parser, null, readOnly);
	}

	// ==================== IMPLEMENTATION ====================

	@Override
	protected void addContentToValueDistributionPanel() {
		Collection<Date> parsedValues = getParsedValues();

		if (parsedValues.isEmpty()) {
			valueDistributionPanel.add(new JLabel("No valid date values"), BorderLayout.CENTER);
			return;
		}

		// Convert dates to milliseconds for statistics
		var timestamps = convertDatesToTimestamps(parsedValues);

		// Calculate statistics
		var dataStatistics = new StatisticsSupport(timestamps);
		cacheStatistics(parsedValues);

		// Create visualization panel
		JPanel contentPanel = createVisualizationPanel(dataStatistics, timestamps);
		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);

		// Add date range information
		JLabel rangeLabel = createRangeLabel();
		valueDistributionPanel.add(rangeLabel, BorderLayout.NORTH);
	}

	/**
	 * Convert dates to millisecond timestamps for numerical analysis.
	 */
	private List<Double> convertDatesToTimestamps(Collection<Date> dates) {
		List<Double> timestamps = new ArrayList<>(dates.size());

		for (Date date : dates) {
			if (date != null) {
				timestamps.add((double) date.getTime());
			}
		}

		return timestamps;
	}

	/**
	 * Cache min/max dates and range for quick access.
	 */
	private void cacheStatistics(Collection<Date> dates) {
		if (dates.isEmpty()) {
			this.minDate = null;
			this.maxDate = null;
			this.dateRangeMillis = 0;
			return;
		}

		// Find min and max dates
		Date min = null;
		Date max = null;

		for (Date date : dates) {
			if (date == null)
				continue;

			if (min == null || date.before(min)) {
				min = date;
			}
			if (max == null || date.after(max)) {
				max = date;
			}
		}

		this.minDate = min;
		this.maxDate = max;
		this.dateRangeMillis = (max != null && min != null) ? max.getTime() - min.getTime() : 0;
	}

	/**
	 * Create visualization panel with boxplot and distribution.
	 */
	private JPanel createVisualizationPanel(StatisticsSupport dataStatistics, List<Double> timestamps) {

		JPanel contentPanel = new JPanel(new GridLayout(2, 1));

		// Boxplot
		BoxPlotHorizontalChartPanel boxplot = new BoxPlotHorizontalChartPanel(dataStatistics);
		boxplot.setBackground(null);
		contentPanel.add(boxplot);

		// Distribution
		Distribution1DHorizontalPanel<Double> distribution = (Distribution1DHorizontalPanel<Double>) Distribution1DPanels
				.createForDoubles(timestamps, false);
		distribution.setBackground(null);
		contentPanel.add(distribution);

		return contentPanel;
	}

	/**
	 * Create label showing date range.
	 */
	private JLabel createRangeLabel() {
		if (minDate == null || maxDate == null) {
			return new JLabel("Date range: N/A");
		}

		String minStr = DATE_FORMAT.format(minDate);
		String maxStr = DATE_FORMAT.format(maxDate);
		String rangeStr = formatDuration(dateRangeMillis);

		return new JLabel(String.format("Range: %s to %s (%s)", minStr, maxStr, rangeStr));
	}

	/**
	 * Format duration in human-readable form.
	 */
	private String formatDuration(long millis) {
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		long years = days / 365;

		if (years > 0) {
			return String.format("%d years, %d days", years, days % 365);
		} else if (days > 0) {
			return String.format("%d days, %d hours", days, hours % 24);
		} else if (hours > 0) {
			return String.format("%d hours, %d minutes", hours, minutes % 60);
		} else if (minutes > 0) {
			return String.format("%d minutes, %d seconds", minutes, seconds % 60);
		} else {
			return String.format("%d seconds", seconds);
		}
	}

	@Override
	protected Class<Date> getClassType() {
		return Date.class;
	}

	@Override
	public String getName() {
		return "Date Properties";
	}

	@Override
	protected boolean testForMissingValue(Date missingValueIndicator, Date value) {
		// For dates, null is the only missing value indicator
		if (missingValueIndicator == null) {
			return value == null;
		}
		return missingValueIndicator.equals(value);
	}

	// ==================== PUBLIC API ====================

	/**
	 * Returns the earliest date in the dataset.
	 *
	 * @return minimum date, or null if none parsed
	 */
	public Date getMinDate() {
		if (minDate == null) {
			getParsedValues(); // Force computation
		}
		return minDate;
	}

	/**
	 * Returns the latest date in the dataset.
	 *
	 * @return maximum date, or null if none parsed
	 */
	public Date getMaxDate() {
		if (maxDate == null) {
			getParsedValues(); // Force computation
		}
		return maxDate;
	}

	/**
	 * Returns the date range in milliseconds.
	 *
	 * @return difference between max and min date in ms
	 */
	public long getDateRangeMillis() {
		if (minDate == null) {
			getParsedValues(); // Force computation
		}
		return dateRangeMillis;
	}

	/**
	 * Returns the date range in days.
	 *
	 * @return difference between max and min date in days
	 */
	public long getDateRangeDays() {
		return dateRangeMillis / (1000 * 60 * 60 * 24);
	}

	/**
	 * Returns the date range in years (approximate).
	 *
	 * @return difference between max and min date in fractional years
	 */
	public double getDateRangeYears() {
		return dateRangeMillis / (1000.0 * 60 * 60 * 24 * 365.25);
	}

	// resetParsedValues() is inherited unchanged: the base class's default
	// onParsedValuesReset() re-invokes addContentToValueDistributionPanel(),
	// which already recomputes cacheStatistics() and rebuilds the range label
	// and charts together.
}
