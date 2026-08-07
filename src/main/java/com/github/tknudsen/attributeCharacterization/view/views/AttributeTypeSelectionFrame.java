package com.github.tknudsen.attributeCharacterization.view.views;

import java.awt.Dimension;
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
 * Frame wrapping an {@link AttributeTypeSelectionView} for
 * interactive attribute type and parser selection.
 * </p>
 *
 * @version 1.02
 * @since 2016
 */
public class AttributeTypeSelectionFrame extends SVGFrame implements AttributeTypeAndParserDetector {

	/** Serial version UID. */
	private static final long serialVersionUID = -2263072137892221324L;

	/** Window title also used as attribute name label. */
	private final String title;
	/** Embedded view that drives the type-selection interaction. */
	private AttributeTypeSelectionView attributeTypeSelectionView;

	/**
	 * Creates and displays a full-screen attribute type selection frame.
	 *
	 * @param title the window title shown to the user
	 */
	public AttributeTypeSelectionFrame(String title) {
		this.title = title;

		initialize();
	}

	private final void initialize() {
		setTitle(title);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		getContentPane().setLayout(new GridLayout(0, 1));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width, screenSize.height - 45);

		setVisible(true);

		attributeTypeSelectionView = createAttributeTypeSelectionView();
		add(attributeTypeSelectionView);

		revalidate();
		repaint();

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				attributeTypeSelectionView.forceNullReturn();
			}
		});
	}

	/**
	 * Propagates title changes to the embedded view so that the "Title to
	 * Clipboard" button always reflects the currently displayed attribute.
	 */
	@Override
	public void setTitle(String title) {
		super.setTitle(title);

		if (attributeTypeSelectionView != null)
			attributeTypeSelectionView.setTitle(title);
	}

	/**
	 * Creates the embedded attribute type selection view.
	 *
	 * @return a new {@link AttributeTypeSelectionView} for this frame's title
	 */
	public AttributeTypeSelectionView createAttributeTypeSelectionView() {
		return new AttributeTypeSelectionView(title);
	}

	@Override
	public Class<?> getAttributeType(Collection<Object> values) {
		Class<?> attributeType = attributeTypeSelectionView.getAttributeType(values);

		this.dispose();

		return attributeType;
	}

	@Override
	public <T> Entry<Class<T>, IObjectParser<T>> getAttributeTypeAndParserType(Collection<Object> values) {
		Entry<Class<T>, IObjectParser<T>> attributeTypeAndParserType = attributeTypeSelectionView
				.getAttributeTypeAndParserType(values);

		return attributeTypeAndParserType;
	}

}
