package com.github.tknudsen.attributeCharacterization.view.views;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DateParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IntegerParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.LongParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.StringParser;
import com.github.tknudsen.attributeCharacterization.view.panels.AttributeCharacteristicsPanel;
import com.github.tknudsen.attributeCharacterization.view.panels.BooleanAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeCharacterization.view.panels.CategoricalAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeCharacterization.view.panels.DateAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeCharacterization.view.panels.NumericalContinuousAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeCharacterization.view.panels.NumericalIntegerAttributeCharacteristicsPanel;
import com.github.tknudsen.attributeCharacterization.view.panels.NumericalLongAttributeCharacteristicsPanel;

/**
 * <p>
 * AttributeCharacterization
 * </p>
 * 
 * <p>
 * Factory to create AttributeCharacteristicsPanels for all supported attribute
 * types.
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
public class AttributeTypeSelectionViews {

	public static LinkedHashMap<AttributeCharacteristicsPanel<?>, IObjectParser<?>> createPanelsWithParsers(
			Collection<Object> values, ActionListener listener, boolean readOnly) {
		LinkedHashMap<AttributeCharacteristicsPanel<?>, IObjectParser<?>> panelsWithParsers = new LinkedHashMap<>();

		StringParser p1 = new StringParser();
		CategoricalAttributeCharacteristicsPanel categoricalPanel = new CategoricalAttributeCharacteristicsPanel(values,
				p1, "", readOnly);
		categoricalPanel.addActionListener(listener);
		panelsWithParsers.put(categoricalPanel, p1);

		BooleanParser p2 = new BooleanParser();
		BooleanAttributeCharacteristicsPanel booleanPanel = new BooleanAttributeCharacteristicsPanel(values, p2,
				readOnly);
		booleanPanel.addActionListener(listener);
		panelsWithParsers.put(booleanPanel, p2);

		DateParser p3 = new DateParser();
		DateAttributeCharacteristicsPanel datePanel = new DateAttributeCharacteristicsPanel(values, p3, readOnly);
		datePanel.addActionListener(listener);
		panelsWithParsers.put(datePanel, p3);

		IntegerParser p4 = new IntegerParser();
		NumericalIntegerAttributeCharacteristicsPanel numericalDiscretePanel = new NumericalIntegerAttributeCharacteristicsPanel(
				values, p4, null, readOnly);
		numericalDiscretePanel.addActionListener(listener);
		panelsWithParsers.put(numericalDiscretePanel, p4);

		LongParser p5 = new LongParser();
		NumericalLongAttributeCharacteristicsPanel numericalDiscreteLongPanel = new NumericalLongAttributeCharacteristicsPanel(
				values, p5, null, readOnly);
		numericalDiscreteLongPanel.addActionListener(listener);
		panelsWithParsers.put(numericalDiscreteLongPanel, p5);

		DoubleParser p6 = new DoubleParser(false);
		NumericalContinuousAttributeCharacteristicsPanel numericalPanel2 = new NumericalContinuousAttributeCharacteristicsPanel(
				values, p6, Double.NaN, readOnly);
		numericalPanel2.addActionListener(listener);
		panelsWithParsers.put(numericalPanel2, p6);

		DoubleParser p7 = new DoubleParser(true);
		NumericalContinuousAttributeCharacteristicsPanel numericalPanel = new NumericalContinuousAttributeCharacteristicsPanel(
				values, p7, Double.NaN, readOnly);
		numericalPanel.addActionListener(listener);
		panelsWithParsers.put(numericalPanel, p7);

		return panelsWithParsers;
	}
}
