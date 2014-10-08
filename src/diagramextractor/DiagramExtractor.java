/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diagramextractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.PageExtractor;

/**
 *
 * @author Stanislav Chren
 */
public class DiagramExtractor {

    private static final Map<Integer, String[]> diagramMap;

    static {
        diagramMap = new HashMap<Integer, String[]>();
        diagramMap.put(1, new String[]{"Use Case Diagram", "Diagram případů užití"});
        diagramMap.put(2, new String[]{"Activity Diagram", "Diagram aktivit"});
        diagramMap.put(3, new String[]{"Class Diagram", "Diagram tříd"});
        diagramMap.put(4, new String[]{"State Machine Diagram", "Diagram stavového stroje"});
        diagramMap.put(5, new String[]{"Entity Relationship Diagram", "Diagram ERD"});
        diagramMap.put(6, new String[]{"Communication Diagram", "Komunikační diagram"});
        diagramMap.put(7, new String[]{"Sequence Diagram", "Sekvenční diagram"});
        diagramMap.put(8, new String[]{"Deployment Diagram", "Diagram nasazení"});
        diagramMap.put(9, new String[]{"Package Diagram", "Diagram balíčků"});
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, COSVisitorException {

        if (args.length < 2) {
            showHelp();
            System.exit(-1);
        }

        List<Integer> diagramOptionsList = new LinkedList<>();
        diagramOptionsList = parseOptions(args);
        
        List<String> diagramNameList = new LinkedList<>();
        diagramNameList = getDiagramNames(diagramOptionsList);
           

        File inputDir = new File(args[0]);
        File[] reports = inputDir.listFiles();
        String diagramName = args[1];

        PDDocument outputDocument = new PDDocument();

        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName("output.pdf");

        for (File report : reports) {

            PDDocument doc = PDDocument.load(report);
            System.out.println("LOADED FILE: " + report.getName());

            int pageNumber = 0;

            System.out.println("NUMBER OF PAGES: " + doc.getNumberOfPages());

            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(i);
                stripper.setEndPage(i);

                String contents = stripper.getText(doc);
                
                boolean containsDiagram = false;
                
                for (String diagram : diagramNameList) {
                    if (contents.contains(diagram)) {
                        containsDiagram = true;
                    }
                }

                if (containsDiagram &&
                    !contents.contains("Table of Contents") &&
                    !contents.contains("Table of Figures") &&
                    !contents.contains("Obsah") &&
                    !contents.contains("Tabulka čísel")) {
                    pageNumber = i;
                    System.out.println("Diagram found on page: " + pageNumber);

                    PageExtractor extractor = new PageExtractor(doc, pageNumber, pageNumber);
                    PDDocument extractedPage = extractor.extract();

                    PDPage page = (PDPage) extractedPage.getDocumentCatalog().getAllPages().get(0);
                    PDPageContentStream contentStream = new PDPageContentStream(extractedPage, page, true, true, true);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.moveTextPositionByAmount(100, 50);
                    contentStream.drawString(report.getName());
                    contentStream.endText();
                    contentStream.close();

                    merger.appendDocument(outputDocument, extractedPage);

                }

            }

            if (pageNumber == 0) {
                System.out.println("The diagram " + diagramName + " was not found in file " + report.getName());
            }
            doc.close();
        }

        merger.mergeDocuments();

        System.out.println();
        System.out.println("Diagrams have been merged.");

        String outputFileName = generateFilename(inputDir.getCanonicalPath(), "output.pdf");
        outputDocument.save(outputFileName);
        outputDocument.close();

        System.out.println("Output file saved as: " + outputFileName);
    }

    /**
     * Replaces all slashes or backslahes (depending on OS) from inputPath with
     * dashes and then appends the postfix
     *
     * @param inputPath Path to the reports directory
     * @param postfix e.g. output.pdf
     * @return
     */
    private static String generateFilename(String inputPath, String postfix) {
        String dashedPath = inputPath.replace(System.getProperty("file.separator"), "-");
        String croppedPath = dashedPath.substring(3);
        return croppedPath + "-" + postfix;
    }

    private static void showHelp() {
        System.out.println("The DiagramExtractor should be used as follows:");
        System.out.println("java -jar DiagramExtractor.jar path_to_dir_with_reports DIAGRAM_OPTIONS");
        System.out.println("where DIAGRAM_OPTIONS is one or more Numbers according to the list:");
        System.out.println("1 - Use Case Diagram");
        System.out.println("2 - Activity Diagram");
        System.out.println("3 - Class Diagram");
        System.out.println("4 - State Machine Diagram");
        System.out.println("5 - Entity Relationship Diagram");
        System.out.println("6 - Communication Diagram");
        System.out.println("7 - Sequence Diagram");
        System.out.println("8 - Deployment Diagram");
        System.out.println("9 - Package Diagram");
    }

    private static List<Integer> parseOptions(String[] options) {
        List<Integer> result = new LinkedList<>();
        for (int i = 1; i < options.length; i++) {
            try {
                int option = Integer.parseInt(options[i]);
                if (option < 1 || option > 9) {
                    showHelp();
                    System.exit(-1);
                } else {
                    result.add(option);
                }

            } catch (NumberFormatException e) {
                showHelp();
                System.exit(-1);
            }
        }
        return result;

    }
    
    private static List<String> getDiagramNames(List<Integer> options) {
        List<String> results = new LinkedList<>();
        for (Integer option : options) {
            results.addAll(Arrays.asList(diagramMap.get(option)));
        }
        
        return results;
    }

}
