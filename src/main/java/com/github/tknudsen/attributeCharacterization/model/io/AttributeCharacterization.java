package com.github.tknudsen.attributeCharacterization.model.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.attributes.AttributeTypeAndParserDetector;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.ParserTools;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.FileUtils;
import com.github.TKnudsen.ComplexDataObject.model.tools.StringUtils;
import com.github.tknudsen.attributeCharacterization.view.views.AttributeTypeSelectionFrame;

/**
 * <p>
 * AttributeCharacterization
 * </p>
 * 
 * <p>
 * 
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
public class AttributeCharacterization {

	public static Entry<Class<Object>, IObjectParser<Object>> characterizeAttribute(String attribute,
			ComplexDataContainer complexDataContainer, AttributeTypeAndParserDetector detector) {

		Objects.requireNonNull(attribute);
		Objects.requireNonNull(complexDataContainer);
		Objects.requireNonNull(detector);

		Collection<Object> values = complexDataContainer.getAttributeValueCollection(attribute);

		return detector.getAttributeTypeAndParserType(values);
	}

	/**
	 * 
	 * @param tokens        outer list contains rows, inner list contains attributes
	 * @param headlineCount number of headlines containing attribute names and to be
	 *                      ignored for the interpretation task
	 * @return list
	 * @throws IOException e
	 */
	public static List<Entry<Class<Object>, IObjectParser<Object>>> interpreteData(List<List<String>> tokens,
			int headlineCount) throws IOException {

		AttributeTypeSelectionFrame attributeTypeSelecor = new AttributeTypeSelectionFrame(
				"Attribute Characterization");

		List<Entry<Class<Object>, IObjectParser<Object>>> attributes = new ArrayList<>();

		int attributeCount = 0;
		for (List<String> row : tokens)
			attributeCount = Math.max(attributeCount, row.size());

		for (int a = 0; a < attributeCount; a++) {
			Collection<Object> values = new ArrayList<>();

			for (int r = headlineCount; r < tokens.size(); r++)
				if (tokens.get(r).size() > a)
					values.add(tokens.get(r).get(a));

			if (headlineCount > 0)
				attributeTypeSelecor.setTitle("Attribute Characterization: " + tokens.get(0).get(a));

			attributes.add(attributeTypeSelecor.getAttributeTypeAndParserType(values));
		}

		attributeTypeSelecor.dispose();

		return attributes;
	}

	/**
	 * this powerful new method manages the parsing of tabular and json-based files.
	 * It creates rows, tokenizes rows, and then applies the attribute
	 * characterization routine.
	 * 
	 * @param dataFile
	 * @param tokenizerSeparator
	 * @param headlineCount
	 * @return
	 * @throws IOException
	 */
	public static List<ComplexDataObject> parseData(String dataFile, String tokenizerSeparator, int headlineCount)
			throws IOException {
//		File f = new File(dataFile);
//		String configFileName = f.getAbsolutePath().replace(f.getName(), "")
//				+ FileUtils.getFilenameNameWithoutExtension(dataFile) + " dataAttributeConfig.json";
		String configFileName = FileUtils.getFilenameNameWithoutExtension(dataFile) + " dataAttributeConfig.json";

		List<String> rows = ParserTools.loadRows(dataFile);

		List<List<String>> tokens = new ArrayList<List<String>>();
		int coloumbsCount = 0;
		for (int i = 0; i < rows.size(); i++) {
			String row = rows.get(i);
			List<String> lineTokens = StringUtils.tokenize(row, tokenizerSeparator);
			coloumbsCount = Math.max(coloumbsCount, lineTokens.size());
			tokens.add(lineTokens);
		}

		// load attribute configuration
		List<Map<String, Object>> attributesConfigs = AttributeConfigIO.loadStocksAttributesConfigs(configFileName);

		// create if not existing
		if (attributesConfigs == null) {
			attributesConfigs = new ArrayList<>();

			List<Entry<Class<Object>, IObjectParser<Object>>> interpreteData = AttributeCharacterization
					.interpreteData(tokens, headlineCount);

			for (int a = 0; a < coloumbsCount; a++) {
				String attribute = (headlineCount > 0 && tokens.get(0).size() > a) ? tokens.get(0).get(a)
						: "Attribute " + (a + 1);

				Entry<Class<Object>, IObjectParser<Object>> entry = interpreteData.get(a);

				Map<String, Object> attributeConfigMap = new HashMap<>();
				attributeConfigMap.put("Attribute", attribute);
				attributeConfigMap.put("Attribute Type", entry.getKey());
				attributeConfigMap.put("Attribute Parser", entry.getValue());
				attributesConfigs.add(attributeConfigMap);
			}

			AttributeConfigIO.saveStocksAttributeConfig(attributesConfigs, configFileName);
		}

		// parse data to complex data objects
		List<ComplexDataObject> cdos = new ArrayList<>();
		for (int i = headlineCount; i < rows.size(); i++) {
			List<String> row = tokens.get(i);

			ComplexDataObject cdo = new ComplexDataObject(i);

			for (int a = 0; a < coloumbsCount; a++) {
				Map<String, Object> attributeConfig = attributesConfigs.get(a);

				String attribute = attributeConfig.get("Attribute").toString();

				// apply attribute characterization
				IObjectParser<?> parser = (IObjectParser<?>) attributeConfig.get("Attribute Parser");

				Object parsed = parser.apply(row.get(a));

				cdo.add(attribute, parsed);
			}

			cdos.add(cdo);
		}

		return cdos;
	}

}
