package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.panels.piechart.PieCharts;
import com.github.tknudsen.attributeCharacterization.data.events.AttributeTypeDecisionActionEvent;

/**
 * <p>
 * Base panel for attribute characterization with parsing and
 * visualization.
 * </p>
 *
 * <p>
 * <b>Thread Safety:</b> Parsing is synchronized for thread-safe lazy
 * initialization.
 * </p>
 *
 * <p>
 * <b>Immutability:</b> Input collection is copied to prevent external
 * modification.
 * </p>
 *
 * @param <T> the attribute value type produced by the parser
 *
 * @version 2.0 (revised)
 * @since 2016
 */
public abstract class AttributeCharacteristicsPanel<T> extends JPanel {

	@Serial
	private static final long serialVersionUID = 7287578850006835039L;

	// ==================== CONSTANTS ====================

	/** Preferred size in pixels of the parse-ratio pie chart. */
	protected static final int PIE_CHART_SIZE = 36;
	/** Default bar colour used in frequency bar charts. */
	protected static final Color DEFAULT_BAR_COLOR = Color.GRAY.darker();

	// ==================== FIELDS ====================

	private final List<Object> values; // Immutable copy
	private final boolean readOnly;

	private volatile List<T> parsedValues; // Lazy initialized
	private volatile List<T> unmodifiableParsedValues;
	private volatile double parseableRatio; // Cached ratio

	private IObjectParser<T> parser;
	private T missingValueIndicator;

	private final List<ActionListener> listeners;
	private final Object parseLock = new Object();

	// UI components
	/** Panel that holds the type-specific value distribution visualization. */
	protected JPanel valueDistributionPanel;

	// ==================== CONSTRUCTORS ====================

	/**
	 * Creates a panel with default settings (editable).
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to type T
	 * @param missingValueIndicator the sentinel value representing missing data
	 */
	public AttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<T> parser, T missingValueIndicator) {
		this(values, parser, missingValueIndicator, false);
	}

	/**
	 * Creates a panel with a custom read-only setting.
	 *
	 * <p>
	 * <b>Note for subclasses:</b> this constructor calls
	 * {@link #addContentToValueDistributionPanel()}, which subclasses override.
	 * Per the JLS, that override runs before this subclass's own field
	 * initializers and constructor body have executed, so it must not read any
	 * subclass-declared field -- only write to fields it owns, as all current
	 * subclasses do. {@link #onParsedValuesReset()} calls the same method again
	 * later (after construction), so this constraint only matters for the very
	 * first invocation.
	 * </p>
	 *
	 * @param values the collection of raw attribute values
	 * @param parser the parser used to convert raw objects to type T
	 * @param missingValueIndicator the sentinel value representing missing data
	 * @param readOnly if true, the panel does not allow user-driven type changes
	 */
	public AttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<T> parser, T missingValueIndicator,
			boolean readOnly) {

		// Validation
		Objects.requireNonNull(values, "Values collection cannot be null");
		Objects.requireNonNull(parser, "Parser cannot be null");

		// Make defensive copy to prevent external modification
		this.values = new ArrayList<>(values);
		this.parser = parser;
		this.missingValueIndicator = missingValueIndicator;
		this.readOnly = readOnly;
		this.listeners = new ArrayList<>();

		// Initialize UI
		initialize();
	}

	// ==================== ABSTRACT METHODS ====================

	/**
	 * Adds type-specific content to the value distribution panel. Called once
	 * during construction (before this subclass's own field initializers have
	 * run -- only write fields here, never read them) and again on every
	 * subsequent {@link #resetParsedValues()} unless the subclass overrides
	 * {@link #onParsedValuesReset()}.
	 */
	protected abstract void addContentToValueDistributionPanel();

	/**
	 * Returns the class type of parsed values.
	 *
	 * @return class object for type T
	 */
	protected abstract Class<T> getClassType();

	/**
	 * Returns the display name for this attribute type.
	 *
	 * @return human-readable attribute type name
	 */
	@Override
	public abstract String getName();

	/**
	 * Apply type-specific filter to exclude certain values from parsing.
	 * 
	 * @param o Value to check
	 * @return true if value should be excluded
	 */
	protected boolean applySpecificFilter(Object o) {
		return false;
	}

	// ==================== PARSING (THREAD-SAFE) ====================

	/**
	 * Get parsed values, initializing if necessary. Thread-safe lazy
	 * initialization.
	 * 
	 * @return Unmodifiable list of parsed values
	 */
	public List<T> getParsedValues() {
		if (parsedValues == null) {
			synchronized (parseLock) {
				if (parsedValues == null) {
					parseValues();
				}
			}
		}
		return unmodifiableParsedValues;
	}

	/**
	 * Returns the ratio of successfully parsed values.
	 *
	 * @return ratio in [0.0, 1.0]
	 */
	public double getParseableRatio() {
		// Ensure parsing has occurred
		if (parsedValues == null) {
			getParsedValues();
		}
		return parseableRatio;
	}

	/**
	 * Resets parsed values to force re-parsing, and refreshes the visualization
	 * to reflect the change. Call this after changing parser configuration.
	 *
	 * <p>
	 * Not overridable directly -- override {@link #onParsedValuesReset()}
	 * instead to customize how the visualization is refreshed. This split
	 * exists because a subclass overriding this method directly (instead of the
	 * hook) can easily forget to refresh {@link #valueDistributionPanel} or
	 * recompute fields that are otherwise only populated inside
	 * {@link #addContentToValueDistributionPanel()}, leaving them stale forever.
	 * </p>
	 */
	public final void resetParsedValues() {
		synchronized (parseLock) {
			parsedValues = null;
			parseableRatio = 0.0;
		}

		if (valueDistributionPanel != null) {
			onParsedValuesReset();
			revalidate();
			repaint();
		}
	}

	/**
	 * Refreshes {@link #valueDistributionPanel} after parsed values have been
	 * invalidated by {@link #resetParsedValues()}. The default implementation
	 * fully rebuilds it by re-invoking {@link #addContentToValueDistributionPanel()},
	 * which also naturally recomputes any subclass-cached fields that method
	 * populates. Override this if a subclass needs to preserve interactive
	 * controls (e.g. a checkbox) that {@link #addContentToValueDistributionPanel()}
	 * would otherwise recreate from scratch.
	 */
	protected void onParsedValuesReset() {
		valueDistributionPanel.removeAll();
		addContentToValueDistributionPanel();
		addButtonPanelIfNeeded();
	}

	/**
	 * Parse values using the configured parser.
	 */
	protected final void parseValues() {
		var parsed = new ArrayList<T>(values.size());
		int successCount = 0;

		for (Object o : values) {
			// Determine if a value should be excluded from parsing.
			if (applySpecificFilter(o)) {
				continue;
			}

			T t = parser.apply(o);

			if (!testForMissingValue(missingValueIndicator, t)) {
				parsed.add(t);
				successCount++;
			}
		}

		this.parsedValues = parsed;
		this.unmodifiableParsedValues = Collections.unmodifiableList(parsed);
		this.parseableRatio = values.isEmpty() ? 0.0 : (double) successCount / values.size();
	}

	/**
	 * Tests whether a parsed value is considered a missing value.
	 *
	 * @param missingValueIndicator the sentinel value representing missing data
	 * @param value the candidate value to test
	 * @return true if value equals the missing-value indicator
	 */
	protected boolean testForMissingValue(T missingValueIndicator, T value) {
		if (missingValueIndicator == null) {
			return value == null;
		}
		return missingValueIndicator.equals(value);
	}

	// ==================== UI INITIALIZATION ====================

	/**
	 * Initialize the UI components.
	 */
	private void initialize() {
		setLayout(new BorderLayout());

		// North panel: statistics and name
		JPanel northPanel = createNorthPanel();
		add(northPanel, BorderLayout.NORTH);

		// Center panel: value distribution
		valueDistributionPanel = new JPanel(new BorderLayout());
		addContentToValueDistributionPanel();
		addButtonPanelIfNeeded();

		add(valueDistributionPanel, BorderLayout.CENTER);
	}

	/**
	 * Adds the accept/ignore button panel to the south of
	 * {@link #valueDistributionPanel}, unless this panel is read-only.
	 */
	private void addButtonPanelIfNeeded() {
		if (!readOnly) {
			JPanel buttons = createButtonPanel();
			valueDistributionPanel.add(buttons, BorderLayout.SOUTH);
		}
	}

	/**
	 * Create north panel with statistics.
	 */
	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel(new GridLayout(3, 1));

		// Title
		northPanel.add(new JLabel(getName()));

		// Parser info
		northPanel.add(new JLabel(parser.toString()));

		// Statistics
		JPanel statsPanel = createStatisticsPanel();
		northPanel.add(statsPanel);

		return northPanel;
	}

	/**
	 * Create statistics panel with parse ratio.
	 */
	private JPanel createStatisticsPanel() {
		JPanel statsPanel = new JPanel(new BorderLayout());

		// Left: ratio label
		JPanel statsLeft = new JPanel(new GridLayout(1, 2));
		statsLeft.add(new JLabel("Ratio of parsed values: "));

		// Trigger parsing to get ratio
		var ratio = getParseableRatio();
		statsLeft.add(new JLabel(String.valueOf(MathFunctions.round(ratio, 3))));
		statsPanel.add(statsLeft, BorderLayout.WEST);

		// Right: pie chart
		var pieChart = PieCharts.createPieChartBipartite(ratio, Color.GREEN.darker());
		pieChart.setPreferredSize(new Dimension(PIE_CHART_SIZE, PIE_CHART_SIZE));
		statsPanel.add(pieChart, BorderLayout.EAST);

		return statsPanel;
	}

	/**
	 * Create button panel for accepting/ignoring.
	 */
	private JPanel createButtonPanel() {
		JPanel buttons = new JPanel(new GridLayout(2, 1));

		// Accept button
		JButton acceptButton = new JButton("Accept");
		acceptButton.addActionListener(e -> fireAcceptAction(e, parser));
		buttons.add(acceptButton);

		// Accept and ignore button
		JButton acceptIgnoreButton = new JButton("Accept and Ignore");
		acceptIgnoreButton.addActionListener(e -> fireAcceptAction(e, null));
		buttons.add(acceptIgnoreButton);

		return buttons;
	}

	/**
	 * Fire accept action to all listeners.
	 */
	private void fireAcceptAction(ActionEvent e, IObjectParser<T> selectedParser) {
		AttributeTypeDecisionActionEvent<T> event = new AttributeTypeDecisionActionEvent<>(this, e.getID(), getName(),
				getClassType(), selectedParser);

		System.out.println("AttributeCharacteristicsPanel: Decision made for " + getName());

		// Notify all listeners
		for (ActionListener listener : listeners) {
			listener.actionPerformed(event);
		}
	}

	// ==================== LISTENER MANAGEMENT ====================

	/**
	 * Adds an action listener, removing any existing equal instance first.
	 *
	 * @param listener the listener to add
	 * @return true if the listener was added
	 */
	public boolean addActionListener(ActionListener listener) {
		Objects.requireNonNull(listener, "Listener cannot be null");
		removeActionListener(listener);
		return listeners.add(listener);
	}

	/**
	 * Removes the given action listener.
	 *
	 * @param listener the listener to remove
	 * @return true if the listener was removed
	 */
	public boolean removeActionListener(ActionListener listener) {
		return listeners.remove(listener);
	}

	// ==================== GETTERS / SETTERS ====================

	/**
	 * Returns the current parser.
	 *
	 * @return the object parser
	 */
	public IObjectParser<T> getParser() {
		return parser;
	}

	/**
	 * Replaces the current parser and resets parsed values.
	 *
	 * @param parser the new parser
	 */
	public void setParser(IObjectParser<T> parser) {
		this.parser = Objects.requireNonNull(parser, "Parser cannot be null");
		resetParsedValues(); // Force re-parsing with new parser
	}

	/**
	 * Returns the missing-value indicator.
	 *
	 * @return the sentinel value representing missing data
	 */
	public T getMissingValueIndicator() {
		return missingValueIndicator;
	}

	/**
	 * Sets the missing-value indicator and resets parsed values.
	 *
	 * @param missingValueIndicator the new sentinel value representing missing data
	 */
	public void setMissingValueIndicator(T missingValueIndicator) {
		this.missingValueIndicator = missingValueIndicator;
		resetParsedValues(); // Force re-parsing with new indicator
	}

	/**
	 * Returns whether this panel is read-only.
	 *
	 * @return true if the panel does not allow user-driven type changes
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Returns the number of input values.
	 *
	 * @return total input value count
	 */
	public int getValueCount() {
		return values.size();
	}

	/**
	 * Returns the number of successfully parsed values.
	 *
	 * @return count of parsed (non-missing) values
	 */
	public int getParsedValueCount() {
		return getParsedValues().size();
	}
}
