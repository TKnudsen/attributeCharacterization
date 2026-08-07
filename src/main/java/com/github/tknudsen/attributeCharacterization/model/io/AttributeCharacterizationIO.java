package com.github.tknudsen.attributeCharacterization.model.io;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.TKnudsen.ComplexDataObject.model.io.json.ObjectMapperFactory;
import com.github.TKnudsen.ComplexDataObject.model.tools.FileTools;

/**
 * <p>
 * Little helpers when loading/storing attribute configurations.
 * </p>
 *
 * @version 1.03
 * @since 2016
 */
public class AttributeCharacterizationIO {

	/** Shared Jackson mapper configured for ComplexDataObject serialization. */
	private static ObjectMapper mapper = ObjectMapperFactory.getComplexDataObjectObjectMapper();

	/**
	 * 
	 * JSON-based method that saves attribute-configurations to a file. Each
	 * attribute configuration (stored in a map) consists of three keys:
	 * 
	 * Attribute - the attribute name
	 * 
	 * Attribute Type - the class type of the attribute (output after parsing)
	 * 
	 * Attribute Parser - the parser instance that can do the conversion job
	 * 
	 * @param attributesConfigs data
	 * @param fileName          target file name
	 */
	public static void saveAttributeConfig(List<Map<String, Object>> attributesConfigs, String fileName) {

		try {
			File configFile = new File(fileName);
			FileTools.createParentDirectory(configFile);

			// Now write the actual file
			mapper.writeValue(configFile, attributesConfigs);
			System.out.println("Successfully saved config to: " + configFile.getAbsolutePath());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * JSON-based method that loads attribute-configurations from file. Each
	 * attribute configuration (stored in a map) consists of three keys:
	 * 
	 * Attribute - the attribute name
	 * 
	 * Attribute Type - the class type of the attribute (output after parsing)
	 * 
	 * Attribute Parser - the parser instance that can do the conversion job
	 * 
	 * @param fileName source file name
	 * @return attribute configuration data
	 */
	public static List<Map<String, Object>> loadAttributeConfigs(String fileName) {

		if (!FileTools.exists(fileName))
			return null;

		List<Map<String, Object>> attributeConfigs = null;
		try {
			attributeConfigs = mapper.readValue(new File(fileName), new TypeReference<List<Map<String, Object>>>() {
			});

			return attributeConfigs;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * to prevent instantiation.
	 */
	private AttributeCharacterizationIO() {
	}

	/**
	 * Loads headline count from file.
	 *
	 * @param headlineCountFileName path to file
	 * @return headline count or null if not found
	 * @throws IOException if file reading fails
	 */
	public static Integer loadHeadlineCountFromFile(String headlineCountFileName) throws IOException {
		File file = new File(headlineCountFileName);
		if (!file.exists()) {
			return null;
		}

		List<String> lines = FileTools.readLines(headlineCountFileName);
		if (lines.isEmpty()) {
			return null;
		}

		for (String line : lines) {
			if (line.contains("\t")) {
				String numberStr = line.substring(line.indexOf("\t") + 1).trim();
				try {
					return Integer.valueOf(numberStr);
				} catch (NumberFormatException e) {
					System.err.println("Invalid headline count in file: " + numberStr);
				}
			}
		}

		return null;
	}

	/**
	 * Saves headline count to file.
	 *
	 * @param headlineCountFileName path to file
	 * @param headlineCount         count to save
	 * @throws IOException if file writing fails
	 */
	public static void saveHeadlineCount(String headlineCountFileName, int headlineCount) throws IOException {
		String content = "Number of Headlines that contain Attribute Information\t" + headlineCount;

		FileTools.writeString(headlineCountFileName, content, false);
	}

	/**
	 * Get configuration file path from data file path.
	 * 
	 * @param dataFilePath Original data file path
	 * @return Configuration file path
	 */
	public static String getCharacterizationFilePath(String dataFilePath) {
		String baseName = FileTools.getFileNameWithoutExtension(dataFilePath);
		return baseName + " dataAttributeConfig.json";
	}

}
