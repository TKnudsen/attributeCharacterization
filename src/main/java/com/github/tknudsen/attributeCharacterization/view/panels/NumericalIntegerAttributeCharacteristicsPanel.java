package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;
import com.github.TKnudsen.infoVis.view.panels.boxplot.BoxPlotHorizontalChartPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DHorizontalPanel;
import com.github.TKnudsen.infoVis.view.panels.distribution1D.Distribution1DPanels;

/**
 * <p>
 * AttributeCharacterization
 * </p>
 * 
 * <p>
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
public class NumericalIntegerAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1617737257897086025L;

	public NumericalIntegerAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Integer> parser,
			Integer missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	public NumericalIntegerAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Integer> parser,
			Integer missingValueIndicator, boolean readOnly) {
		super(values, parser, missingValueIndicator, readOnly);
	}

	@Override
	protected void addContentToValueDistributionPanel() {
		Collection<Integer> parsedValues = getParsedValues();

		if (parsedValues.size() == 0)
			return;

		StatisticsSupport dataStatistics = new StatisticsSupport(parsedValues);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(2, 0));

		BoxPlotHorizontalChartPanel infoVisBoxPlotHorizontalPanel = new BoxPlotHorizontalChartPanel(dataStatistics);
		infoVisBoxPlotHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisBoxPlotHorizontalPanel);

		List<Double> values = new ArrayList<>();
		for (Integer i : getParsedValues())
			values.add(i.doubleValue());

//		Distribution1DHorizontalPanel<Double> infoVisDistribution1DHorizontalPanel = Distribution1DHorizontalPanels
//				.createForDoubles(values);
		Distribution1DHorizontalPanel<Double> infoVisDistribution1DHorizontalPanel = (Distribution1DHorizontalPanel<Double>) Distribution1DPanels
				.createForDoubles(values, false);
		infoVisDistribution1DHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisDistribution1DHorizontalPanel);

		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);
	}

	@Override
	protected Class<Integer> getClassType() {
		return Integer.class;
	}

	@Override
	public String getName() {
		return "Numerical (discrete, integer) Properties";
	}

}
