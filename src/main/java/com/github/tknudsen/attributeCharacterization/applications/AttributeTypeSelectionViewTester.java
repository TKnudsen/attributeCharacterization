package com.github.tknudsen.attributeCharacterization.applications;

import java.util.ArrayList;
import java.util.Collection;

import com.github.TKnudsen.infoVis.view.frames.SVGFrameTools;
import com.github.tknudsen.attributeCharacterization.view.views.AttributeTypeSelectionView;

/**
 * <p>
 * AttributeCharacterization
 * </p>
 * 
 * <p>
 * Tester for diverse values, to prove the effectiveness of the attribute
 * characterization guidance components.
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
public class AttributeTypeSelectionViewTester {

	public static void main(String[] args) {

		Collection<Object> values = new ArrayList<Object>();

		values.add("Peter");
		values.add("Paul");
		values.add("Mary");
		values.add("Mary");
		values.add("");
		values.add(true);
		values.add("true");
		values.add(1);
		values.add(0);
		values.add(1.3);
		values.add(5.5);
		values.add(4.4);
		values.add(1.8);
		values.add(2.5);
		values.add(3);
		values.add(3);
		values.add(3);

		for (int i = 0; i < 100; i++)
			values.add(Math.random() * 3);

		values.add(Double.NaN);

		AttributeTypeSelectionView view = new AttributeTypeSelectionView("Some Attribute", values);

		SVGFrameTools.dropSVGFrame(view, "Test: AttributeTypeSelectionView", 1200, 600);
	}

}
