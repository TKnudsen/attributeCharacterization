package com.github.tknudsen.attributeCharacterization.model.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.TKnudsen.ComplexDataObject.data.attributes.AttributeTypeAndParserDetector;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.StringParser;

/**
 * Covers the non-GUI logic of {@link AttributeCharacterization}: attribute
 * delegation, row parsing/error handling, and the config-already-exists path
 * that skips the interactive {@code AttributeTypeSelectionFrame} entirely.
 */
public class AttributeCharacterizationTest {

	private static final String KEY_ATTRIBUTE = "Attribute";
	private static final String KEY_ATTRIBUTE_TYPE = "Attribute Type";
	private static final String KEY_ATTRIBUTE_PARSER = "Attribute Parser";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	// ==================== characterizeAttribute ====================

	@Test
	public void characterizeAttribute_delegatesAttributeValuesToDetector() {
		List<ComplexDataObject> objects = new ArrayList<>();
		for (String name : new String[] { "Peter", "Paul", "Mary" }) {
			ComplexDataObject cdo = new ComplexDataObject();
			cdo.add("name", name);
			objects.add(cdo);
		}
		ComplexDataContainer container = new ComplexDataContainer(objects);

		RecordingDetector detector = new RecordingDetector();

		Entry<Class<Object>, IObjectParser<Object>> result = AttributeCharacterization.characterizeAttribute("name",
				container, detector);

		assertEquals(3, detector.receivedValues.size());
		assertTrue(detector.receivedValues.containsAll(List.of("Peter", "Paul", "Mary")));
		assertEquals(String.class, result.getKey());
	}

	@Test(expected = NullPointerException.class)
	public void characterizeAttribute_rejectsNullAttribute() {
		ComplexDataContainer container = new ComplexDataContainer(new ComplexDataObject());
		AttributeCharacterization.characterizeAttribute(null, container, new RecordingDetector());
	}

	@Test(expected = NullPointerException.class)
	public void characterizeAttribute_rejectsNullContainer() {
		AttributeCharacterization.characterizeAttribute("name", null, new RecordingDetector());
	}

	@Test(expected = NullPointerException.class)
	public void characterizeAttribute_rejectsNullDetector() {
		ComplexDataContainer container = new ComplexDataContainer(new ComplexDataObject());
		AttributeCharacterization.characterizeAttribute("name", container, null);
	}

	// ==================== parseAttributes ====================

	@Test
	public void parseAttributes_parsesValidRowsAndSkipsFailingAndEmptyRows() {
		List<Map<String, Object>> configs = new ArrayList<>();
		configs.add(configFor("name", String.class, new ThrowingOnBoomParser()));

		List<List<String>> tokens = new ArrayList<>();
		tokens.add(List.of("header")); // index 0, before headlineCount -- untouched
		tokens.add(List.of("Peter")); // index 1, parses fine
		tokens.add(List.of("BOOM")); // index 2, parser throws -- row dropped
		tokens.add(List.of()); // index 3, empty -- skipped
		tokens.add(List.of("Mary")); // index 4, parses fine

		List<ComplexDataObject> result = AttributeCharacterization.parseAttributes(tokens, 1, configs);

		assertEquals(2, result.size());
		assertEquals("Peter", result.get(0).getAttribute("name"));
		assertEquals("Mary", result.get(1).getAttribute("name"));
	}

	@Test
	public void parseAttributes_destructivelyClearsProcessedRowsOnly() {
		List<Map<String, Object>> configs = new ArrayList<>();
		configs.add(configFor("name", String.class, new ThrowingOnBoomParser()));

		List<List<String>> tokens = new ArrayList<>();
		tokens.add(List.of("header"));
		tokens.add(List.of("Peter"));
		tokens.add(List.of("BOOM"));
		tokens.add(List.of());
		tokens.add(List.of("Mary"));

		AttributeCharacterization.parseAttributes(tokens, 1, configs);

		assertEquals(List.of("header"), tokens.get(0)); // before headlineCount: untouched
		assertNull(tokens.get(1)); // processed (succeeded): cleared
		assertNull(tokens.get(2)); // processed (failed): cleared
		assertEquals(List.of(), tokens.get(3)); // empty row: left as-is, never cleared
		assertNull(tokens.get(4)); // processed (succeeded): cleared
	}

	@Test(expected = NullPointerException.class)
	public void parseAttributes_rejectsNullTokens() {
		AttributeCharacterization.parseAttributes(null, 0, new ArrayList<>());
	}

	@Test(expected = NullPointerException.class)
	public void parseAttributes_rejectsNullConfigs() {
		AttributeCharacterization.parseAttributes(new ArrayList<>(), 0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseAttributes_rejectsNegativeHeadlineCount() {
		AttributeCharacterization.parseAttributes(new ArrayList<>(), -1, new ArrayList<>());
	}

	// ==================== attributeCharacterization (config exists) ====================

	@Test
	public void attributeCharacterization_loadsExistingConfigWithoutTouchingGui() throws Exception {
		String configFileName = temporaryFolder.newFile("existing.json").getAbsolutePath();

		List<Map<String, Object>> existingConfig = new ArrayList<>();
		Map<String, Object> entry = new HashMap<>();
		entry.put(KEY_ATTRIBUTE, "value");
		entry.put(KEY_ATTRIBUTE_TYPE, "String");
		existingConfig.add(entry);
		AttributeCharacterizationIO.saveAttributeConfig(existingConfig, configFileName);

		// If the config did not already exist, this call would open an interactive
		// AttributeTypeSelectionFrame (interpretData()) and hang a headless test run.
		List<Map<String, Object>> result = AttributeCharacterization.attributeCharacterization(new ArrayList<>(),
				configFileName, 1);

		assertEquals(1, result.size());
		assertEquals("value", result.get(0).get(KEY_ATTRIBUTE));
	}

	// ==================== helpers ====================

	private static Map<String, Object> configFor(String attribute, Class<?> type, IObjectParser<?> parser) {
		Map<String, Object> config = new HashMap<>();
		config.put(KEY_ATTRIBUTE, attribute);
		config.put(KEY_ATTRIBUTE_TYPE, type);
		config.put(KEY_ATTRIBUTE_PARSER, parser);
		return config;
	}

	/** Records the values collection it was asked to characterize. */
	private static final class RecordingDetector implements AttributeTypeAndParserDetector {
		Collection<Object> receivedValues;

		@Override
		public Class<?> getAttributeType(Collection<Object> values) {
			this.receivedValues = values;
			return String.class;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> Entry<Class<T>, IObjectParser<T>> getAttributeTypeAndParserType(Collection<Object> values) {
			this.receivedValues = values;
			return new AbstractMap.SimpleEntry<>((Class<T>) String.class, (IObjectParser<T>) new StringParser());
		}
	}

	/** Parses any value to its String form, except "BOOM" which simulates a parse failure. */
	private static final class ThrowingOnBoomParser implements IObjectParser<String> {
		@Override
		public String apply(Object o) {
			if ("BOOM".equals(o))
				throw new RuntimeException("simulated parse failure");
			return o == null ? null : o.toString();
		}

		@Override
		public Class<String> getOutputClassType() {
			return String.class;
		}
	}
}
