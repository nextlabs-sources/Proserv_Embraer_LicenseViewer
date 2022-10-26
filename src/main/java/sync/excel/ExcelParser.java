/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package main.java.sync.excel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

import main.java.sync.object.License;
import main.java.sync.object.NDAUser;

/**
 * A rudimentary XLSX -> CSV processor modeled on the POI sample program
 * XLS2CSVmra from the package org.apache.poi.hssf.eventusermodel.examples. As
 * with the HSSF version, this tries to spot missing rows and cells, and output
 * empty entries for them.
 * <p/>
 * Data sheets are read using a SAX parser to keep the memory footprint
 * relatively small, so this should be able to read enormous workbooks. The
 * styles table and the shared-string table must be kept in memory. The standard
 * POI styles table class is used, but a custom (read-only) class is used for
 * the shared string table because the standard POI SharedStringsTable grows
 * very quickly with the number of unique strings.
 * <p/>
 * For a more advanced implementation of SAX event parsing of XLSX files, see
 * {@link XSSFEventBasedExcelExtractor} and {@link XSSFSheetXMLHandler}. Note
 * that for many cases, it may be possible to simply use those with a custom
 * {@link SheetContentsHandler} and no SAX code needed of your own!
 */
public class ExcelParser {
	/**
	 * Uses the XSSF Event SAX helpers to do most of the work of parsing the
	 * Sheet XML, and outputs the contents as a (basic) CSV.
	 */
	private class SheetToCSV implements SheetContentsHandler {
		private boolean firstCellOfRow = false;
		private int currentRow = -1;
		private int currentCol = -1;

		private void outputMissingRows(int number) {
			for (int i = 0; i < number; i++) {
				for (int j = 0; j < minColumns; j++) {
					output.append(',');
				}
				output.append('\n');
			}
		}

		public void startRow(int rowNum) {
			// If there were gaps, output the missing rows
			outputMissingRows(rowNum - currentRow - 1);
			// Prepare for this row
			firstCellOfRow = true;
			currentRow = rowNum;
			currentCol = -1;
		}

		public void endRow(int rowNum) {
			// Ensure the minimum number of columns
			for (int i = currentCol; i < minColumns; i++) {
				output.append(',');
			}
			output.append('\n');
		}

		public void cell(String cellReference, String formattedValue, XSSFComment comment) {
			if (firstCellOfRow) {
				firstCellOfRow = false;
			} else {
				output.append(',');
			}

			// gracefully handle missing CellRef here in a similar way as
			// XSSFCell does
			if (cellReference == null) {
				cellReference = new CellAddress(currentRow, currentCol).formatAsString();
			}

			// Did we miss any cells?
			int thisCol = (new CellReference(cellReference)).getCol();
			int missedCols = thisCol - currentCol - 1;
			for (int i = 0; i < missedCols; i++) {
				output.append(',');
			}
			currentCol = thisCol;

			// Number or string?
			try {
				Double.parseDouble(formattedValue);
				output.append(formattedValue);
			} catch (NumberFormatException e) {
				output.append('"');
				output.append(formattedValue);
				output.append('"');
			}
		}

		public void headerFooter(String text, boolean isHeader, String tagName) {
			// Skip, no headers or footers in CSV
		}
	}

	///////////////////////////////////////

	private final OPCPackage xlsxPackage;

	/**
	 * Number of columns to read starting with leftmost
	 */
	private final int minColumns;

	/**
	 * Destination for data
	 */
	private final PrintStream output;

	/**
	 * Creates a new XLSX -> CSV converter
	 *
	 * @param pkg
	 *            The XLSX package to process
	 * @param output
	 *            The PrintStream to output the CSV to
	 * @param minColumns
	 *            The minimum number of columns to output, or -1 for no minimum
	 */
	public ExcelParser(OPCPackage pkg, PrintStream output, int minColumns) {
		this.xlsxPackage = pkg;
		this.output = output;
		this.minColumns = minColumns;
	}

	/**
	 * Parses and shows the content of one sheet using the specified styles and
	 * shared-strings tables.
	 *
	 * @param styles
	 * @param strings
	 * @param sheetInputStream
	 */
	public void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, SheetContentsHandler sheetHandler,
			InputStream sheetInputStream) throws IOException, ParserConfigurationException, SAXException {
		DataFormatter formatter = new DataFormatter();
		InputSource sheetSource = new InputSource(sheetInputStream);
		try {
			XMLReader sheetParser = SAXHelper.newXMLReader();
			ContentHandler handler = new XSSFSheetXMLHandler(styles, null, strings, sheetHandler, formatter, false);
			sheetParser.setContentHandler(handler);
			sheetParser.parse(sheetSource);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
		}
	}

	/**
	 * Initiates the processing of the XLS workbook file to CSV.
	 *
	 * @throws IOException
	 * @throws OpenXML4JException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void process() throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {
		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
		XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
		StylesTable styles = xssfReader.getStylesTable();
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		// int index = 0;
		while (iter.hasNext()) {
			InputStream stream = iter.next();
			// String sheetName = iter.getSheetName();
			// this.output.println();
			// this.output.println(sheetName + " [index=" + index + "]:");
			processSheet(styles, strings, new SheetToCSV(), stream);
			stream.close();
			// ++index;
		}
	}

	public List<License> readFromLicenseCSV(Properties props) throws FileNotFoundException {

		CsvToBean<License> csv = new CsvToBean<License>();

		String csvFilename = "./license.csv";
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));

		Map<String, String> columnMapping = new HashMap<String, String>();
		columnMapping.put(props.getProperty("name", "TAA/License"), "licenseName");
		columnMapping.put(props.getProperty("type", "License Type"), "type");
		columnMapping.put(props.getProperty("approved-parties", "Approved Parties"), "approvedParties");
		columnMapping.put(props.getProperty("approved-parties-code", "Approved Parties Code"), "approvedPartiesCode");
		columnMapping.put(props.getProperty("country-parties", "Country Parties"), "countryParties");
		columnMapping.put(props.getProperty("approved-sublicensees", "Approved Sublicensees"), "approvedSublicensees");
		columnMapping.put(props.getProperty("approved-sublicensees-code", "Approved Sublicensees Code"), "approvedSublicenseesCode");
		columnMapping.put(props.getProperty("nda-sublicensees", "NDA Sublicensees"), "nDASublicensees");
		columnMapping.put(props.getProperty("country-sublicensees", "Country Sublicensees"), "countrySublicensees");
		columnMapping.put(props.getProperty("approved-nationalities", "Approved Nationalities"),
				"approvedNationalities");
		columnMapping.put(props.getProperty("denied-nationalities", "Denied Nationalities"), "deniedNationalities");
/*		columnMapping.put(props.getProperty("proviso-nationalities", "Proviso Nationalities"), "provisoNationalities");*/

		HeaderColumnNameTranslateMappingStrategy<License> strategy = new HeaderColumnNameTranslateMappingStrategy<License>();
		strategy.setType(License.class);
		strategy.setColumnMapping(columnMapping);

		// Set column mapping strategy
		List<License> list = csv.parse(strategy, csvReader);

		return list;
	}

	public List<NDAUser> readFromNDACSV(Properties props) throws FileNotFoundException {

		CsvToBean<NDAUser> csv = new CsvToBean<NDAUser>();

		String csvFilename = "./nda.csv";
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));

		Map<String, String> columnMapping = new HashMap<String, String>();
		columnMapping.put(props.getProperty("employee-id", "Nome"), "employeeName");
		columnMapping.put(props.getProperty("license-name", "TAA"), "licenseName");

		HeaderColumnNameTranslateMappingStrategy<NDAUser> strategy = new HeaderColumnNameTranslateMappingStrategy<NDAUser>();
		strategy.setType(NDAUser.class);
		strategy.setColumnMapping(columnMapping);

		// Set column mapping strategy
		List<NDAUser> list = csv.parse(strategy, csvReader);

		return list;
	}
}