# attributeCharacterization

A data attribute characterization library that helps parsing, analyzing, and interpreting data attributes (columns of data tables). Interactive visual interfaces ease the task even for non-programmers and lay users, enabling attribute characterization for the masses.

Requires Java 21 or later.

## Usage

Current snapshot:

```xml
<dependency>
  <groupId>com.github.tknudsen</groupId>
  <artifactId>attribute-characterization</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Built on top of the [ComplexDataObject](https://github.com/TKnudsen/ComplexDataObject) and [infoVis](https://github.com/TKnudsen/infoVis) libraries, which are pulled in transitively as SNAPSHOT dependencies from the same TKnudsen ecosystem.

## The Problem

Given a raw tabular dataset (CSV, TSV, ...), every column is initially just a list of strings. Before the data is useful, each column needs a *type* (is this a date, a number, a category?) and a *parser* that can reliably convert raw strings to that type. Type detection is inherently ambiguous -- `"03"` could be an integer, a zero-padded code, or part of a date -- so fully automatic guessing is often wrong in ways that only a human reviewing the actual values would catch.

This library addresses that with a two-tier approach: try automatic detection first, and fall back to a lightweight interactive UI that lets a human confirm or correct the guess, one attribute at a time.

## Core Workflow

`AttributeCharacterization` (`model.io`) is the entry point:

- **`parseData(dataFile, tokenizerSeparator)`** -- reads a delimited file, characterizes every column, and returns a list of `ComplexDataObject`s (see ComplexDataObject) with correctly typed attributes.
- **`attributeCharacterization(...)`** -- runs just the characterization step, producing a list of attribute configs (name, detected type, chosen parser) without parsing the full dataset.
- **`characterizeAttribute(attribute, container, detector)`** -- characterizes a single already-loaded attribute against a pluggable `AttributeTypeAndParserDetector`.

Attribute configs are cached as a JSON file next to the source data file (`<filename> dataAttributeConfig.json`, written via `AttributeCharacterizationIO`). Once a dataset has been characterized -- interactively or automatically -- re-parsing it later reuses the saved config and skips the UI entirely.

## Interactive Type Selection

When no cached config exists, `AttributeCharacterization` opens an `AttributeTypeSelectionFrame` (`view.views`) per attribute. It implements ComplexDataObject's `AttributeTypeAndParserDetector` interface, so it plugs directly into the same detection slot an automatic/headless detector would use.

For each attribute, `AttributeTypeSelectionViews` builds one candidate `AttributeCharacteristicsPanel` (`view.panels`) per supported type, all shown side by side:

- **`CategoricalAttributeCharacteristicsPanel`** -- frequency bar chart, cardinality ratio.
- **`BooleanAttributeCharacteristicsPanel`** -- true/false frequency chart.
- **`DateAttributeCharacteristicsPanel`** -- date range, boxplot and distribution over the parsed timestamps.
- **`NumericalIntegerAttributeCharacteristicsPanel`** / **`NumericalLongAttributeCharacteristicsPanel`** / **`NumericalContinuousAttributeCharacteristicsPanel`** (share a common `NumericalAttributeCharacteristicsPanel` base) -- boxplot and 1D distribution; the continuous variant additionally offers a live toggle for European ("." as thousands separator) vs. standard number formats.

Each panel shows the parse-success ratio for its candidate type and lets the user Accept it (optionally "Accept and Ignore" to drop the attribute). Accepting fires an `AttributeTypeDecisionActionEvent` (`data.events`) that resolves the selection for that attribute and closes the frame.

## Development

```
mvn test
```

Runs the JUnit suite covering the non-GUI logic in `AttributeCharacterization`/`AttributeCharacterizationIO` (parsing, config persistence, validation). The interactive panels and frames are exercised manually via the demo classes in `applications/`.
