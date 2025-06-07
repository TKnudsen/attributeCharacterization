package com.github.tknudsen.attributeCharacterization.view.views;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.Map.Entry;

import javax.swing.JFrame;

import com.github.TKnudsen.ComplexDataObject.data.attributes.AttributeTypeAndParserDetector;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.infoVis.view.frames.SVGFrame;

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
public class AttributeTypeSelectionFrame extends SVGFrame implements AttributeTypeAndParserDetector {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2263072137892221324L;

	private final String title;
	private AttributeTypeSelectionView attributeTypeSelectionView;

	public AttributeTypeSelectionFrame(String title) {
		this.title = title;

		initialize();
	}

	private final void initialize() {
		setTitle(title);

//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		getContentPane().setLayout(new GridLayout(0, 1));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width, screenSize.height - 45);
//		setSize(1200, 800);
//		setLocation(200, 200);

		setVisible(true);

		attributeTypeSelectionView = createAttributeTypeSelectionView();
		add(attributeTypeSelectionView);

		revalidate();
		repaint();

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
//				if (JOptionPane.showConfirmDialog(null, "Do you want to store null values?", "Store attribute values?",
//						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

				attributeTypeSelectionView.forceNullReturn();
			}
		});
	}

	@Override
	public void paintComponents(Graphics g) {
		super.paintComponents(g);

		System.out.println("TEST");
	}

	public AttributeTypeSelectionView createAttributeTypeSelectionView() {
		return new AttributeTypeSelectionView(title);
	}

	@Override
	public Class<?> getAttributeType(Collection<Object> values) {
		Class<?> attributeType = attributeTypeSelectionView.getAttributeType(values);

		this.dispose();

		// System.out.println("AttributeTypeSelectionFramw: attribute type decision
		// detected. return.");
		return attributeType;
	}

	@Override
	public <T> Entry<Class<T>, IObjectParser<T>> getAttributeTypeAndParserType(Collection<Object> values) {
		Entry<Class<T>, IObjectParser<T>> attributeTypeAndParserType = attributeTypeSelectionView
				.getAttributeTypeAndParserType(values);

		// no. do not do this here. otherwise you can only use it once
		// this.dispose();

		return attributeTypeAndParserType;
	}

	@Override
	public String getTitle() {
		return title;
	}

}
