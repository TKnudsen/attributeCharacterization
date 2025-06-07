package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.infoVis.view.panels.piechart.PieChart;
import com.github.TKnudsen.infoVis.view.panels.piechart.PieCharts;
import com.github.tknudsen.attributeCharacterization.data.events.AttributeTypeDecisionActionEvent;

/**
 * <p>
 * AttributeCharacterization
 * </p>
 * 
 * <p>
 * 
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
public abstract class AttributeCharacteristicsPanel<T> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7287578850006835039L;

	private Collection<Object> values;
	private List<T> parsedValues;

	private IObjectParser<T> parser;
	private T missingValueIndicator;
	private boolean readOnly = false;

	private double parseableRatio;

	private List<ActionListener> listeners = new ArrayList<>();

	// visuals
	protected JPanel valueDistributionPanel;

	public AttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<T> parser, T missingValueIndicator) {
		this(values, parser, missingValueIndicator, false);
	}

	public AttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<T> parser, T missingValueIndicator,
			boolean readOnly) {
		this.values = values;
		this.parser = parser;
		this.missingValueIndicator = missingValueIndicator;
		this.readOnly = readOnly;

		parseValues();

		initialize();
	}

	public boolean addActionListener(ActionListener listener) {
		removeActionListener(listener);

		return this.listeners.add(listener);
	}

	protected abstract void addContentToValueDistributionPanel();

	protected boolean applySpecificFilter(Object o) {
		return false;
	}

	protected abstract Class<T> getClassType();

	public T getMissingValueIndicator() {
		return missingValueIndicator;
	}

	@Override
	public abstract String getName();

	public List<T> getParsedValues() {
		if (parsedValues == null)
			parseValues();

		return parsedValues;
	}

	/**
	 * allows re-configuring parsers
	 */
	public void resetParsedValues() {
		parsedValues = null;
	}

	public IObjectParser<T> getParser() {
		return parser;
	}

	private void initialize() {
		setLayout(new BorderLayout());

		JPanel statisticsInformationPanel = new JPanel(new BorderLayout());

		JPanel statisticsLeft = new JPanel();
		statisticsLeft.setLayout(new GridLayout(0, 2));
		statisticsLeft.add(new JLabel("Ratio of parsed values: "));
		statisticsLeft.add(new JLabel(String.valueOf(MathFunctions.round(parseableRatio, 3))));
		statisticsInformationPanel.add(statisticsLeft, BorderLayout.WEST);

		PieChart pieChartRight = PieCharts.createPieChartBipartite(parseableRatio, Color.GREEN.darker());
		pieChartRight.setPreferredSize(new Dimension(36, 36));
		statisticsInformationPanel.add(pieChartRight, BorderLayout.EAST);

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(3, 0));

		northPanel.add(new JLabel(getName()));
		northPanel.add(new JLabel(parser.toString()));
		northPanel.add(statisticsInformationPanel);

		add(northPanel, BorderLayout.NORTH);

		valueDistributionPanel = new JPanel();
		valueDistributionPanel.setLayout(new BorderLayout());

		addContentToValueDistributionPanel();

		if (!readOnly) {
			JButton acceptButton = new JButton("Accept");
			valueDistributionPanel.add(acceptButton, BorderLayout.SOUTH);

			acceptButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					AttributeTypeDecisionActionEvent<T> actionEvent = new AttributeTypeDecisionActionEvent<T>(this,
							e.getID(), getName(), getClassType(), parser);

					System.out.println("Decision made for: " + getName());

					for (ActionListener listener : listeners)
						listener.actionPerformed(actionEvent);
				}
			});
		}

		add(valueDistributionPanel, BorderLayout.CENTER);
	}

	protected final void parseValues() {
		parseableRatio = 0.0;
		parsedValues = new ArrayList<>();

		for (Object o : values) {
			T t = parser.apply(o);
			if (testForMissingValue(missingValueIndicator, t) || applySpecificFilter(o)) {
			} else {
				parseableRatio++;
				parsedValues.add(t);
			}
		}

		parseableRatio /= values.size();
	}

	private boolean removeActionListener(ActionListener listener) {
		return this.listeners.remove(listener);
	}

	public void setMissingValueIndicator(T missingValueIndicator) {
		this.missingValueIndicator = missingValueIndicator;
	}

	public void setParser(IObjectParser<T> parser) {
		this.parser = parser;
	}

	protected boolean testForMissingValue(T missingValueIndicator, T value) {
		if (missingValueIndicator == null)
			if (value == null)
				return true;
			else
				return false;

		if (missingValueIndicator.equals(value))
			return true;
		return false;
	}

}
