package com.github.tknudsen.attributeCharacterization.model.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.StringParser;

/**
 * Covers the file-based persistence helpers in {@link AttributeCharacterizationIO}.
 */
public class AttributeCharacterizationIOTest {

	private static final String KEY_ATTRIBUTE = "Attribute";
	private static final String KEY_ATTRIBUTE_TYPE = "Attribute Type";
	private static final String KEY_ATTRIBUTE_PARSER = "Attribute Parser";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void saveAndLoadAttributeConfig_roundTripsAttributeNames() throws Exception {
		String configFile = temporaryFolder.newFile("config.json").getAbsolutePath();

		List<Map<String, Object>> configs = new ArrayList<>();
		Map<String, Object> config = new HashMap<>();
		config.put(KEY_ATTRIBUTE, "name");
		config.put(KEY_ATTRIBUTE_TYPE, String.class);
		config.put(KEY_ATTRIBUTE_PARSER, new StringParser());
		configs.add(config);

		AttributeCharacterizationIO.saveAttributeConfig(configs, configFile);
		List<Map<String, Object>> loaded = AttributeCharacterizationIO.loadAttributeConfigs(configFile);

		assertEquals(1, loaded.size());
		assertEquals("name", loaded.get(0).get(KEY_ATTRIBUTE));
	}

	@Test
	public void loadAttributeConfigs_returnsNullForMissingFile() {
		String missingFile = temporaryFolder.getRoot().toPath().resolve("does-not-exist.json").toString();

		assertNull(AttributeCharacterizationIO.loadAttributeConfigs(missingFile));
	}

	@Test
	public void saveAndLoadHeadlineCount_roundTrips() throws Exception {
		String headlineFile = temporaryFolder.newFile("headline.txt").getAbsolutePath();

		AttributeCharacterizationIO.saveHeadlineCount(headlineFile, 2);
		Integer loaded = AttributeCharacterizationIO.loadHeadlineCountFromFile(headlineFile);

		assertEquals(Integer.valueOf(2), loaded);
	}

	@Test
	public void loadHeadlineCountFromFile_returnsNullForMissingFile() throws Exception {
		String missingFile = temporaryFolder.getRoot().toPath().resolve("does-not-exist.txt").toString();

		assertNull(AttributeCharacterizationIO.loadHeadlineCountFromFile(missingFile));
	}

	@Test
	public void getCharacterizationFilePath_appendsExpectedSuffix() {
		// getFileNameWithoutExtension() strips only the extension, keeping the
		// directory -- so the config file lands next to the original data file.
		String path = AttributeCharacterizationIO.getCharacterizationFilePath("data/myFile.csv");

		assertEquals("data/myFile dataAttributeConfig.json", path);
	}
}
