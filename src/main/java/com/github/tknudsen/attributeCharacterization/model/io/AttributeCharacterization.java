package com.github.tknudsen.attributeCharacterization.model.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.SwingUtilities;

import com.github.TKnudsen.ComplexDataObject.data.attributes.AttributeTypeAndParserDetector;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.ParserTools;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.FileTools;
import com.github.TKnudsen.ComplexDataObject.model.tools.MemoryTools;
import com.github.tknudsen.attributeCharacterization.view.views.AttributeTypeSelectionFrame;

/**
 * <p>
 * Provides utilities for characterizing and parsing tabular data attributes.
 * Handles automatic type detection, parser selection, and data conversion.
 * </p>
 *
 * @version 1.1
 * @since 2016
 */
public class AttributeCharacterization {

	// Constants
	/** Maximum number of data rows sampled for attribute type detection. */
	public static final int MAX_TOKEN_SAMPLING_SIZE = 5000;
	private static final int DEFAULT_HEADLINE_COUNT = 1;

	// Config file suffixes
	private static final String CONFIG_SUFFIX = " dataAttributeConfig.json";
	private static final String HEADLINE_COUNT_SUFFIX = " dataAttributeHeadlineCount.txt";

	// Attribute config keys
	private static final String KEY_ATTRIBUTE = "Attribute";
	private static final String KEY_ATTRIBUTE_TYPE = "Attribute Type";
	private static final String KEY_ATTRIBUTE_PARSER = "Attribute Parser";

	/**
	 * Characterizes a single attribute from a data container.
	 *
	 * @param attribute            the attribute name to characterize
	 * @param complexDataContainer the data container
	 * @param detector             the type detector to use
	 * @return entry containing the detected type and parser
	 * @throws NullPointerException if any parameter is null
	 */
	public static Entry<Class<Object>, IObjectParser<Object>> characterizeAttribute(String attribute,
			ComplexDataContainer complexDataContainer, AttributeTypeAndParserDetector detector) {

		Objects.requireNonNull(attribute, "Attribute must not be null");
		Objects.requireNonNull(complexDataContainer, "ComplexDataContainer must not be null");
		Objects.requireNonNull(detector, "Detector must not be null");

		Collection<Object> values = complexDataContainer.getAttributeValueCollection(attribute);
		return detector.getAttributeTypeAndParserType(values);
	}

	/**
	 * Parses data from a file, automatically detecting headline count.
	 *
	 * @param dataFile           path to the data file
	 * @param tokenizerSeparator the separator for tokenizing rows
	 * @return list of parsed ComplexDataObjects
	 * @throws IOException if file reading fails
	 */
	public static List<ComplexDataObject> parseData(String dataFile, String tokenizerSeparator) throws IOException {

		validateFilePath(dataFile);

		String headlineFileName = buildHeadlineFileName(dataFile);
		int headlineCount = loadHeadlineCount(headlineFileName);

		return parseData(dataFile, tokenizerSeparator, headlineCount);
	}

	/**
	 * Parses data from a file with specified headline count. Ignores rows with a
	 * different number of tokens than the great majority of rows.
	 *
	 * @param dataFile           path to the data file
	 * @param tokenizerSeparator the separator for tokenizing rows
	 * @param headlineCount      number of header rows to skip
	 * @return list of parsed ComplexDataObjects
	 * @throws IOException if file reading fails
	 */
	public static List<ComplexDataObject> parseData(String dataFile, String tokenizerSeparator, int headlineCount)
			throws IOException {

		validateFilePath(dataFile);
		validateHeadlineCount(headlineCount);

		System.out.println("AttributeCharacterization.parseData: loading dataset " + dataFile);

		List<List<String>> tokens = ParserTools.loadTokens(dataFile, tokenizerSeparator);

		System.out.println("AttributeCharacterization.parseData: dataset loaded with " + tokens.size() + " rows");

		List<List<String>> validated = ParserTools.validateTokenCount(tokens, true);

		System.out.println("AttributeCharacterization.parseData: dataset rows validated, with " + tokens.size()
				+ " rows remaining");

		String configFileName = buildConfigFileName(dataFile);
		List<Map<String, Object>> attributesConfigs = attributeCharacterization(validated, configFileName,
				headlineCount);

		return parseAttributes(validated, headlineCount, attributesConfigs);
	}

	/**
	 * Performs attribute characterization for a data file.
	 *
	 * @param dataFile           path to the data file
	 * @param tokenizerSeparator the separator for tokenizing rows
	 * @return list of attribute configuration maps
	 * @throws IOException if file reading fails
	 */
	public static List<Map<String, Object>> attributeCharacterization(String dataFile, String tokenizerSeparator)
			throws IOException {

		validateFilePath(dataFile);

		String configFileName = buildConfigFileName(dataFile);
		String headlineFileName = buildHeadlineFileName(dataFile);
		int headlineCount = loadHeadlineCount(headlineFileName);

		List<List<String>> tokens = ParserTools.loadTokens(dataFile, tokenizerSeparator);
		return attributeCharacterization(tokens, configFileName, headlineCount);
	}

	/**
	 * Performs attribute characterization with explicit config file and headline
	 * count.
	 *
	 * @param dataFile           path to the data file
	 * @param tokenizerSeparator the separator for tokenizing rows
	 * @param configFileName     path to save/load configuration
	 * @param headlineCount      number of header rows
	 * @return list of attribute configuration maps
	 * @throws IOException if file reading fails
	 */
	public static List<Map<String, Object>> attributeCharacterization(String dataFile, String tokenizerSeparator,
			String configFileName, int headlineCount) throws IOException {

		validateFilePath(dataFile);
		validateHeadlineCount(headlineCount);

		List<List<String>> tokens = ParserTools.loadTokens(dataFile, tokenizerSeparator);
		return attributeCharacterization(tokens, configFileName, headlineCount);
	}

	/**
	 * Core attribute characterization logic.
	 *
	 * @param tokens         tokenized data rows
	 * @param configFileName path to configuration file
	 * @param headlineCount  number of header rows
	 * @return list of attribute configuration maps
	 * @throws IOException if file operations fail
	 */
	public static List<Map<String, Object>> attributeCharacterization(List<List<String>> tokens, String configFileName,
			int headlineCount) throws IOException {

		Objects.requireNonNull(tokens, "Tokens must not be null");
		validateHeadlineCount(headlineCount);

		// Try to load existing configuration
		List<Map<String, Object>> attributesConfigs = AttributeCharacterizationIO
				.loadAttributeConfigs(configFileName);

		// Create new configuration if none exists
		if (attributesConfigs == null) {
			attributesConfigs = createAttributeConfigs(tokens, headlineCount);
			AttributeCharacterizationIO.saveAttributeConfig(attributesConfigs, configFileName);
		}

		return attributesConfigs;
	}

	/**
	 * Creates attribute configurations by interpreting data.
	 *
	 * @param tokens        tokenized data rows
	 * @param headlineCount number of header rows
	 * @return list of attribute configuration maps
	 * @throws IOException if interpretation fails
	 */
	private static List<Map<String, Object>> createAttributeConfigs(List<List<String>> tokens, int headlineCount)
			throws IOException {

		List<Map<String, Object>> attributesConfigs = new ArrayList<>();
		var sampledTokens = sampleTokens(tokens, MAX_TOKEN_SAMPLING_SIZE);

		var interpretedData = interpretData(sampledTokens, headlineCount);

		for (int a = 0; a < interpretedData.size(); a++) {
			String attributeName = extractAttributeName(tokens, headlineCount, a);
			var entry = interpretedData.get(a);

			var attributeConfigMap = createAttributeConfigMap(attributeName, entry.getKey(), entry.getValue());
			attributesConfigs.add(attributeConfigMap);
		}

		return attributesConfigs;
	}

	/**
	 * Extracts attribute name from tokens or generates default name.
	 *
	 * @param tokens         tokenized data rows
	 * @param headlineCount  number of header rows
	 * @param attributeIndex index of the attribute
	 * @return attribute name
	 */
	private static String extractAttributeName(List<List<String>> tokens, int headlineCount, int attributeIndex) {

		if (headlineCount > 0 && !tokens.isEmpty() && tokens.get(0).size() > attributeIndex) {
			return tokens.get(0).get(attributeIndex);
		}

		return "Attribute " + (attributeIndex + 1);
	}

	/**
	 * Creates an attribute configuration map.
	 *
	 * @param attribute attribute name
	 * @param type      attribute type class
	 * @param parser    attribute parser
	 * @return configuration map
	 */
	private static Map<String, Object> createAttributeConfigMap(String attribute, Class<Object> type,
			IObjectParser<Object> parser) {

		Map<String, Object> config = new HashMap<>();
		config.put(KEY_ATTRIBUTE, attribute);
		config.put(KEY_ATTRIBUTE_TYPE, type);
		config.put(KEY_ATTRIBUTE_PARSER, parser);

		return config;
	}

	/**
	 * Loads headline count from file or prompts user if not found. THIS METHOD MAY
	 * SHOW GUI DIALOGS - ensure it's called appropriately.
	 *
	 * @param headlineCountFileName path to headline count file
	 * @return number of header rows
	 */
	private static int loadHeadlineCount(String headlineCountFileName) {
		try {
			// Try to load from file first
			Integer savedCount = AttributeCharacterizationIO.loadHeadlineCountFromFile(headlineCountFileName);
			if (savedCount != null) {
				return savedCount;
			}

			// File doesn't exist or is invalid - prompt user
			// CRITICAL: This must run on EDT if called from GUI context
			int headlineCount = promptUserForHeadlineCount();
			AttributeCharacterizationIO.saveHeadlineCount(headlineCountFileName, headlineCount);
			return headlineCount;

		} catch (IOException e) {
			System.err.println("Error loading headline count: " + e.getMessage());
			e.printStackTrace();
			return DEFAULT_HEADLINE_COUNT;
		}
	}

	/**
	 * Prompts user for headline count using a dialog. Must be called on EDT or will
	 * block if invokeLater is used.
	 *
	 * @return headline count entered by user
	 */
	private static int promptUserForHeadlineCount() {
		// If we're already on EDT, show dialog directly
		if (SwingUtilities.isEventDispatchThread()) {
			return showHeadlineCountDialog();
		}

		// Otherwise, we need to show it on EDT and wait for result
		// Using CompletableFuture to handle cross-thread communication
		CompletableFuture<Integer> future = new CompletableFuture<>();

		SwingUtilities.invokeLater(() -> {
			try {
				int count = showHeadlineCountDialog();
				future.complete(count);
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
		});

		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Error getting headline count from user: " + e.getMessage());
			return DEFAULT_HEADLINE_COUNT;
		}
	}

	/**
	 * Shows the actual dialog to get headline count. MUST be called on EDT.
	 *
	 * @return headline count
	 */
	private static int showHeadlineCountDialog() {
		String input = javax.swing.JOptionPane.showInputDialog(null,
				"Please enter the number of header/headline rows in the tabular data file (typically 1)",
				"Header Row Count", javax.swing.JOptionPane.QUESTION_MESSAGE);

		if (input == null || input.trim().isEmpty()) {
			// User cancelled - use default
			return DEFAULT_HEADLINE_COUNT;
		}

		try {
			int count = Integer.parseInt(input.trim());
			if (count < 0) {
				javax.swing.JOptionPane.showMessageDialog(null,
						"Invalid input! Headline count cannot be negative. Using default: " + DEFAULT_HEADLINE_COUNT,
						"Error", javax.swing.JOptionPane.ERROR_MESSAGE);
				return DEFAULT_HEADLINE_COUNT;
			}
			return count;
		} catch (NumberFormatException e) {
			javax.swing.JOptionPane.showMessageDialog(null,
					"Invalid input! Please enter a valid integer. Using default: " + DEFAULT_HEADLINE_COUNT, "Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);
			return DEFAULT_HEADLINE_COUNT;
		}
	}

	/**
	 * Samples tokens to reduce data set size for faster processing.
	 *
	 * @param tokens     the list to sample
	 * @param sampleSize the target sample size
	 * @return sampled list with at most sampleSize elements
	 */
	private static List<List<String>> sampleTokens(List<List<String>> tokens, int sampleSize) {
		if (tokens.size() <= sampleSize) {
			return tokens;
		}

		double step = (double) tokens.size() / sampleSize;
		return IntStream.range(0, sampleSize).mapToObj(i -> tokens.get((int) (i * step))).collect(Collectors.toList());
	}

	/**
	 * Interprets data to determine attribute types and parsers. THIS METHOD SHOWS
	 * GUI - it will create an AttributeTypeSelectionFrame.
	 *
	 * @param tokens        outer list contains rows, inner list contains attributes
	 * @param headlineCount number of header rows to skip
	 * @return list of detected types and parsers
	 * @throws IOException if interpretation fails
	 */
	public static List<Entry<Class<Object>, IObjectParser<Object>>> interpretData(List<List<String>> tokens,
			int headlineCount) throws IOException {

		Objects.requireNonNull(tokens, "Tokens must not be null");
		validateHeadlineCount(headlineCount);

		AttributeTypeSelectionFrame attributeTypeSelector = new AttributeTypeSelectionFrame(
				"Attribute Characterization");

		try {
			List<Entry<Class<Object>, IObjectParser<Object>>> attributes = new ArrayList<>();
			int attributeCount = calculateAttributeCount(tokens);

			for (int a = 0; a < attributeCount; a++) {
				var values = extractAttributeValues(tokens, headlineCount, a);

				updateSelectorTitle(attributeTypeSelector, tokens, headlineCount, a);
				attributes.add(attributeTypeSelector.getAttributeTypeAndParserType(values));
			}

			return attributes;

		} finally {
			// Always dispose the frame
			attributeTypeSelector.dispose();
		}
	}

	/**
	 * Calculates the maximum number of attributes across all rows.
	 *
	 * @param tokens tokenized rows
	 * @return maximum attribute count
	 */
	private static int calculateAttributeCount(List<List<String>> tokens) {
		int attributeCount = 0;
		for (List<String> row : tokens) {
			attributeCount = Math.max(attributeCount, row.size());
		}
		return attributeCount;
	}

	/**
	 * Extracts values for a specific attribute from all data rows.
	 *
	 * @param tokens         tokenized rows
	 * @param headlineCount  number of header rows to skip
	 * @param attributeIndex index of the attribute
	 * @return collection of values for this attribute
	 */
	private static Collection<Object> extractAttributeValues(List<List<String>> tokens, int headlineCount,
			int attributeIndex) {

		Collection<Object> values = new ArrayList<>();

		for (int r = headlineCount; r < tokens.size(); r++) {
			List<String> row = tokens.get(r);
			if (row.size() > attributeIndex) {
				values.add(row.get(attributeIndex));
			}
		}

		return values;
	}

	/**
	 * Updates the selector frame title with current attribute name.
	 *
	 * @param selector       the selector frame
	 * @param tokens         tokenized rows
	 * @param headlineCount  number of header rows
	 * @param attributeIndex current attribute index
	 */
	private static void updateSelectorTitle(AttributeTypeSelectionFrame selector, List<List<String>> tokens,
			int headlineCount, int attributeIndex) {

		if (headlineCount > 0 && !tokens.isEmpty() && tokens.get(0).size() > attributeIndex) {
			String attributeName = tokens.get(0).get(attributeIndex);
			selector.setTitle("Attribute Characterization: " + attributeName);
		}
	}

	/**
	 * Parses token rows into ComplexDataObjects using attribute configurations.
	 *
	 * <p>
	 * <b>Destructive:</b> to bound peak memory on large datasets, each row of
	 * {@code tokens} is nulled out (via {@code tokens.set(i, null)}) immediately
	 * after it has been parsed. Callers must not reuse {@code tokens} after this
	 * method returns.
	 * </p>
	 *
	 * @param tokens            tokenized rows; consumed and destructively
	 *                          cleared row-by-row as parsing progresses
	 * @param headlineCount     number of header rows to skip
	 * @param attributesConfigs attribute configuration maps
	 * @return list of parsed ComplexDataObjects
	 */
	public static List<ComplexDataObject> parseAttributes(List<List<String>> tokens, int headlineCount,
			List<Map<String, Object>> attributesConfigs) {

		Objects.requireNonNull(tokens, "Tokens must not be null");
		Objects.requireNonNull(attributesConfigs, "Attribute configs must not be null");
		validateHeadlineCount(headlineCount);

		List<ComplexDataObject> cdos = new ArrayList<>(tokens.size());

		// Extract attribute names and parsers once -- avoids a Map.get() per row per
		// attribute
		var attributeEntries = extractAttributeEntries(attributesConfigs);

		// Scoped to this call -- avoids leaking interning decisions across
		// concurrent or unrelated parseAttributes() invocations
		var interner = new SmartInterner();

		for (int i = headlineCount; i < tokens.size(); i++) {
			List<String> row = tokens.get(i);

			if (row == null || row.isEmpty()) {
				continue;
			}

			ComplexDataObject cdo = parseRow(row, i, attributeEntries, interner);
			if (cdo != null)
				cdos.add(cdo);

			// critical: clear the row after parsing to free memory
			tokens.set(i, null);

			// Progress indicator: dot every 100, newline every 1000
			if (i % 100 == 0) {
				System.out.print(".");
				if (i % 10000 == 0) {
					System.out.println(" " + i);
					MemoryTools.freeMemory();
				}
			}
		}

		return cdos;
	}

	/**
	 * Parses a single row into a ComplexDataObject.
	 *
	 * @param row      the row to parse
	 * @param rowIndex the row index
	 * @param entries  pre-extracted attribute names and parsers
	 * @param interner string interner scoped to the enclosing parseAttributes()
	 *                 call
	 * @return parsed ComplexDataObject or null if parsing fails
	 */
	private static ComplexDataObject parseRow(List<String> row, int rowIndex, List<AttributeEntry> entries,
			SmartInterner interner) {

		ComplexDataObject cdo = new ComplexDataObject(row.size(), rowIndex);

		try {
			for (int a = 0; a < Math.min(entries.size(), row.size()); a++) {
				var entry = entries.get(a);

				if (entry.parser() != null) {
					Object parsed = entry.parser().apply(row.get(a));

					if (entry.parser().getOutputClassType().equals(String.class))
						cdo.add(entry.name(), interner.intern((String) parsed));
					else
						cdo.add(entry.name(), parsed);
				}
			}
			return cdo;

		} catch (Exception e) {
			System.err.println("AttributeCharacterization.parseRow: unable to parse data row at index " + rowIndex
					+ ": " + row + ". Skipping row.");
			return null;
		}
	}

	// ==================== Helper Methods ====================

	/**
	 * Intelligently decides whether to intern a string, tracking frequency and
	 * only interning strings that appear multiple times.
	 *
	 * <p>
	 * Scoped to a single {@link #parseAttributes(List, int, List)} call (one
	 * instance per call) rather than shared static state, so that interning
	 * decisions from one dataset never leak into -- or race with -- another.
	 * </p>
	 */
	private static final class SmartInterner {

		private static final int MIN_OCCURRENCES = 3; // Intern if seen 3+ times
		private static final int MAX_LENGTH = 100; // Don't intern very long strings

		private final Map<String, Integer> counts = new HashMap<>();
		private final Set<String> interned = new HashSet<>();

		String intern(String str) {
			if (str == null || str.isEmpty() || str.length() > MAX_LENGTH)
				return str;

			// If we've already decided to intern this string, do it
			if (interned.contains(str))
				return str.intern();

			// Count how many times we've seen this string
			Integer count = counts.get(str);
			if (count == null) {
				counts.put(str, 1);
				return str; // First time seeing it, don't intern yet
			}

			// Increment count
			count++;
			counts.put(str, count);

			// If we've seen it enough times, start interning it
			if (count >= MIN_OCCURRENCES) {
				interned.add(str);

				// Clean up - we don't need to count anymore
				counts.remove(str);

				return str.intern();
			}

			return str;
		}
	}

	/**
	 * Pre-extracted attribute name and parser -- avoids repeated Map lookups per
	 * row.
	 */
	private record AttributeEntry(String name, IObjectParser<?> parser) {
	}

	/**
	 * Extracts attribute names and parsers from config maps once, before row
	 * iteration.
	 */
	private static List<AttributeEntry> extractAttributeEntries(List<Map<String, Object>> configs) {
		var entries = new ArrayList<AttributeEntry>(configs.size());
		for (var config : configs) {
			var name = config.get(KEY_ATTRIBUTE).toString();
			var parser = (IObjectParser<?>) config.get(KEY_ATTRIBUTE_PARSER);
			entries.add(new AttributeEntry(name, parser));
		}
		return entries;
	}

	/**
	 * Builds configuration file name from data file path.
	 */
	private static String buildConfigFileName(String dataFile) {
		return FileTools.getFileNameWithoutExtension(dataFile) + CONFIG_SUFFIX;
	}

	/**
	 * Builds headline count file name from data file path.
	 */
	private static String buildHeadlineFileName(String dataFile) {
		return FileTools.getFileNameWithoutExtension(dataFile) + HEADLINE_COUNT_SUFFIX;
	}

	/**
	 * Validates file path is not null or empty.
	 */
	private static void validateFilePath(String filePath) {
		Objects.requireNonNull(filePath, "File path must not be null");
		if (filePath.trim().isEmpty()) {
			throw new IllegalArgumentException("File path must not be empty");
		}
	}

	/**
	 * Validates headline count is non-negative.
	 */
	private static void validateHeadlineCount(int headlineCount) {
		if (headlineCount < 0) {
			throw new IllegalArgumentException("Headline count must be non-negative, got: " + headlineCount);
		}
	}

}
