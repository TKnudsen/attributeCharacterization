package com.github.tknudsen.attributeCharacterization.view.views;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import com.github.TKnudsen.ComplexDataObject.data.attributes.AttributeTypeAndParserDetector;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;
import com.github.tknudsen.attributeCharacterization.data.events.AttributeTypeDecisionActionEvent;
import com.github.tknudsen.attributeCharacterization.view.panels.AttributeCharacteristicsPanel;

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
public class AttributeTypeSelectionView extends JPanel implements AttributeTypeAndParserDetector, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8364345034820702346L;

	private final String title;
	protected Collection<Object> values;
	private final boolean showRawValues;

	protected List<AttributeCharacteristicsPanel<?>> attributeCharacteristicsPanels;

	// return types
	private boolean decisionMade = false;
	private Class<?> classType;
	private IObjectParser<?> parser;

	public AttributeTypeSelectionView(String title) {
		this.title = title;
		this.showRawValues = true;
	}

	public AttributeTypeSelectionView(String title, Collection<Object> values) {
		this.title = title;
		this.values = Objects.requireNonNull(values);

		this.showRawValues = true;

		refreshView();
	}

	private final void refreshView() {
		decisionMade = false;

		removeAll();
		setLayout(new GridLayout(1, 0));

		initializeAttributeCharacteristicsPanels();

		JPanel valuesPanel = new JPanel(new BorderLayout());

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(2, 1));

		// Copy attribute to clip board
		JButton clipboardButton = new JButton("Title to Clipboard");
		southPanel.add(clipboardButton, BorderLayout.SOUTH);
		clipboardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Clipboard clipboard = toolkit.getSystemClipboard();
				StringSelection strSel = new StringSelection(title);
				clipboard.setContents(strSel, null);
			}
		});

		// Skip functionality
		JButton skipButton = new JButton("Skip Attribute");
		southPanel.add(skipButton, BorderLayout.SOUTH);
		skipButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				forceNullReturn();
			}
		});

		valuesPanel.add(southPanel, BorderLayout.SOUTH);

		if (isShowRawValues()) {
			valuesPanel.add(new JLabel("Raw Values"), BorderLayout.NORTH);

			StringBuilder builder = new StringBuilder();
			int length = 0;
			for (Object o : values)
				if (o != null) {
					builder.append((o.toString() + ", "));

					length += (o.toString() + ", ").length();
					if (length > 5000) {
						System.out.println(this.getClass().getSimpleName()
								+ ": stopped bringing more Raw Values data to screen after 5000 characters");
						break;
					}
				}

			JTextPane textPane = new JTextPane();
			textPane.setText(builder.toString());
			textPane.setBackground(attributeCharacteristicsPanels.get(0).getBackground());
			valuesPanel.add(textPane, BorderLayout.CENTER);

			valuesPanel.setBorder(BorderFactory.createEtchedBorder());
			add(valuesPanel);
		}

		for (AttributeCharacteristicsPanel<?> panel : attributeCharacteristicsPanels) {
			panel.setBorder(BorderFactory.createEtchedBorder());
			add(panel);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e instanceof AttributeTypeDecisionActionEvent) {
			AttributeTypeDecisionActionEvent<?> event = (AttributeTypeDecisionActionEvent<?>) e;
			classType = event.getAttributeType();
			parser = event.getParser();

			decisionMade = true;

			System.out.println(
					"AttributeTypeSelectionView: attribute type decision detected: " + classType.getSimpleName());
		}
	}

	@Override
	public Class<?> getAttributeType(Collection<Object> values) {
		this.values = Objects.requireNonNull(values);

		refreshView();

		this.setVisible(true);

		repaint();
		revalidate();

		while (!decisionMade) {
			Threads.sleep(100, 0);
		}

		// System.out.println("AttributeTypeSelectionView.getAttributeType: leaving
		// while loop: ");

		return classType;
	}

	@Override
	public <T> Entry<Class<T>, IObjectParser<T>> getAttributeTypeAndParserType(Collection<Object> values) {
		this.values = Objects.requireNonNull(values);

		refreshView();

		this.setVisible(true);

		repaint();
		revalidate();

		while (!decisionMade) {
			Threads.sleep(100, 0);
		}

		@SuppressWarnings("unchecked")
		Class<T> c = (Class<T>) classType;
		@SuppressWarnings("unchecked")
		IObjectParser<T> p = (IObjectParser<T>) parser;
		return new AbstractMap.SimpleEntry<Class<T>, IObjectParser<T>>(c, p);
	}

	/**
	 * can be triggered from an external source. Used, e.g., when a Frame is about
	 * to be closed, or if no decision can be made.
	 */
	public void forceNullReturn() {
		classType = null;
		parser = null;

		decisionMade = true;

		System.out.println("AttributeTypeSelectionView: null return forced by external trigger");
	}

	protected void initializeAttributeCharacteristicsPanels() {
		attributeCharacteristicsPanels = new ArrayList<>();

		LinkedHashMap<AttributeCharacteristicsPanel<?>, IObjectParser<?>> panelsAndParsers = AttributeTypeSelectionViews
				.createPanelsWithParsers(values, this, false);

		for (AttributeCharacteristicsPanel<?> p : panelsAndParsers.keySet())
			attributeCharacteristicsPanels.add(p);

	}

	public boolean isShowRawValues() {
		return showRawValues;
	}

}
