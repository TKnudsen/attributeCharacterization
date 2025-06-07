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
public class BooleanAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1617737257897086025L;

	public BooleanAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Boolean> parser) {
		super(values, parser, null);
	}

	public BooleanAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<Boolean> parser,
			boolean readOnly) {
		super(values, parser, null, readOnly);
	}

	@Override
	protected void addContentToValueDistributionPanel() {
		Map<Boolean, Double> counts = new LinkedHashMap<>();
		for (Boolean s : getParsedValues())
			if (!counts.containsKey(s))
				counts.put(s, 1.0);
			else
				counts.put(s, counts.get(s) + 1);

		if (counts.size() == 0)
			return;

		List<Double> data = new ArrayList<Double>(counts.values());
		List<Color> colors = new ArrayList<>();
		for (Double d : data)
			colors.add(Color.GRAY.darker());

		BarChartHorizontal panel = new BarChartHorizontal(data, colors);
		panel.setBackgroundColor(null);

		valueDistributionPanel.add(panel, BorderLayout.CENTER);

		valueDistributionPanel.add(
				new JLabel("cardinality/size: "
						+ MathFunctions.round(counts.size() / (double) getParsedValues().size(), 3)),
				BorderLayout.NORTH);
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
