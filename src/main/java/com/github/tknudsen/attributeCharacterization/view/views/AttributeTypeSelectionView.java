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
 * Interactive panel that presents all candidate attribute type
 * panels and blocks until the user accepts one.
 * </p>
 *
 * @version 1.01
 * @since 2016
 */
public class AttributeTypeSelectionView extends JPanel implements AttributeTypeAndParserDetector, ActionListener {

	/** Serial version UID. */
	private static final long serialVersionUID = 8364345034820702346L;

	/** Attribute name shown as label and copied to the clipboard. */
	private String title;
	/** Raw attribute values being characterised. */
	protected Collection<Object> values;
	/** Whether to show the raw-value text pane. */
	private final boolean showRawValues;

	/** Ordered list of type-specific characterization panels. */
	protected List<AttributeCharacteristicsPanel<?>> attributeCharacteristicsPanels;

	// return types
	/** Flag set to true once the user (or a forced null-return) makes a decision. */
	private boolean decisionMade = false;
	/** The accepted attribute class type, or null if skipped. */
	private Class<?> classType;
	/** The accepted parser, or null if skipped. */
	private IObjectParser<?> parser;

	/**
	 * Creates a view without values; call {@link #getAttributeType(Collection)}
	 * or {@link #getAttributeTypeAndParserType(Collection)} to supply them later.
	 *
	 * @param title the attribute name shown to the user
	 */
	public AttributeTypeSelectionView(String title) {
		this.title = title;
		this.showRawValues = true;
	}

	/**
	 * Creates a view pre-loaded with values and immediately refreshes the UI.
	 *
	 * @param title the attribute name shown to the user
	 * @param values the collection of raw attribute values
	 */
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
		clipboardButton.addActionListener(e -> {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Clipboard clipboard = toolkit.getSystemClipboard();
			StringSelection strSel = new StringSelection(title);
			clipboard.setContents(strSel, null);
		});

		// Skip functionality
		JButton skipButton = new JButton("Skip Attribute");
		southPanel.add(skipButton, BorderLayout.SOUTH);
		skipButton.addActionListener(e -> forceNullReturn());

		valuesPanel.add(southPanel, BorderLayout.SOUTH);

		if (isShowRawValues()) {
			valuesPanel.add(new JLabel("Raw Values"), BorderLayout.NORTH);

			StringBuilder builder = new StringBuilder();
			int length = 0;
			for (Object o : values)
				if (o != null) {
					var s = o.toString() + ", ";
					builder.append(s);
					length += s.length();
					if (length > 5000) {
						System.out.println(this.getClass().getSimpleName()
								+ ": stopped bringing more Raw Values data to screen after 1000 characters");
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
		if (e instanceof AttributeTypeDecisionActionEvent<?> event) {
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

	/**
	 * Populates {@link #attributeCharacteristicsPanels} with one panel per
	 * supported attribute type.
	 */
	protected void initializeAttributeCharacteristicsPanels() {
		attributeCharacteristicsPanels = new ArrayList<>();

		LinkedHashMap<AttributeCharacteristicsPanel<?>, IObjectParser<?>> panelsAndParsers = AttributeTypeSelectionViews
				.createPanelsWithParsers(values, this, false);

		for (AttributeCharacteristicsPanel<?> p : panelsAndParsers.keySet())
			attributeCharacteristicsPanels.add(p);

	}

	/**
	 * Returns whether the raw-value text pane is shown.
	 *
	 * @return true if raw values are displayed
	 */
	public boolean isShowRawValues() {
		return showRawValues;
	}

	/**
	 * Updates the attribute title used as the "Title to Clipboard" button's
	 * clipboard content.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

}
