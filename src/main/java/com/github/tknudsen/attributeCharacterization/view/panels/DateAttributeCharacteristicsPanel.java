package com.github.tknudsen.attributeCharacterization.view.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
public class DateAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Date> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -202069619057179336L;

	public DateAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Date> parser) {
		super(values, parser, null);
	}

	public DateAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Date> parser, boolean readOnly) {
		super(values, parser, null, readOnly);
	}

	@Override
	protected void addContentToValueDistributionPanel() {
		Collection<Date> parsedValues = getParsedValues();

		if (parsedValues.size() == 0)
			return;

		List<Double> values = new ArrayList<>();
		for (Date d : parsedValues)
			values.add((double) d.getTime());

		StatisticsSupport dataStatistics = new StatisticsSupport(values);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(2, 0));

		BoxPlotHorizontalChartPanel infoVisBoxPlotHorizontalPanel = new BoxPlotHorizontalChartPanel(dataStatistics);
		infoVisBoxPlotHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisBoxPlotHorizontalPanel);

//		Distribution1DHorizontalPanel<Double> infoVisDistribution1DHorizontalPanel = Distribution1DHorizontalPanels
//				.createForDoubles(values);
		Distribution1DHorizontalPanel<Double> infoVisDistribution1DHorizontalPanel = (Distribution1DHorizontalPanel<Double>) Distribution1DPanels
				.createForDoubles(values, false);
		infoVisDistribution1DHorizontalPanel.setBackgroundColor(null);
		contentPanel.add(infoVisDistribution1DHorizontalPanel);

		valueDistributionPanel.add(contentPanel, BorderLayout.CENTER);
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
		if (missingValueIndicator == null)
			if (value == null)
				return true;
			else
				return false;

		return false;
	}

}
