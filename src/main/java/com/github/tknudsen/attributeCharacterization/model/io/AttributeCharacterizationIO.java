package com.github.tknudsen.attributeCharacterization.model.io;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.TKnudsen.ComplexDataObject.model.io.json.ObjectMapperFactory;
import com.github.TKnudsen.ComplexDataObject.model.tools.FileUtils;

/**
 * <p>
 * AttributeAnalyzer
 * </p>
 * 
 * <p>
 * Little helpers when loading/storing attribute configurations.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2016-2025 Juergen Bernard,
 * https://github.com/TKnudsen/AttributeAnalyzer
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class AttributeConfigIO {

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
	public static void saveStocksAttributeConfig(List<Map<String, Object>> attributesConfigs, String fileName) {

		try {
			File f = new File(FileUtils.createDirectoryNameString(fileName));
			f.mkdirs();

			mapper.writeValue(new File(fileName), attributesConfigs);
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
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> loadStocksAttributesConfigs(String fileName) {

		if (!FileUtils.testFileExists(fileName))
			return null;

		List<Map<String, Object>> attributeConfigs = null;
		try {
			attributeConfigs = mapper.readValue(new File(fileName), List.class);
			return attributeConfigs;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * to prevent instantiation.
	 */
	private AttributeConfigIO() {
	}
}
