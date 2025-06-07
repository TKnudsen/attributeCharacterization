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
public class CategoricalAttributeCharacteristicsPanel extends AttributeCharacteristicsPanel<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1617737257897086025L;

	public CategoricalAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<String> parser,
			String missingValueIndicator) {
		super(values, parser, missingValueIndicator);
	}

	public CategoricalAttributeCharacteristicsPanel(Collection<Object> values, IObjectParser<String> parser,
			String missingValueIndicator, boolean readOnly) {
		super(values, parser, missingValueIndicator, readOnly);
	}

	@Override
	protected void addContentToValueDistributionPanel() {
		Map<String, Double> counts = new LinkedHashMap<>();
		for (String s : getParsedValues())
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
	protected boolean applySpecificFilter(Object o) {
		// special case: if a rational number is detected, we have ALSO a missing value

		// System.out.println(o);

		if (o instanceof Double) {
			Double d = (Double) o;
			Integer i = d.intValue();
			if (!d.equals(i))
				return true;
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

	@Override
	protected boolean testForMissingValue(String missingValueIndicator, String value) {
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
