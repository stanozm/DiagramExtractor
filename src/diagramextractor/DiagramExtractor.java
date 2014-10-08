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
import java.util.LinkedList;
import java.util.List;
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, COSVisitorException {

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

                if (contents.contains(diagramName)  && !contents.contains("Table of Contents") && !contents.contains("Table of Figures")) {
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
                   
//                
                 System.out.println("The diagram " + diagramName + " was not found in file " + report.getName());
                
            }


            doc.close();
        }
            
        merger.mergeDocuments();
        
        System.out.println();
        System.out.println("Diagrams have been merged.");
        
        outputDocument.save(generateFilename(inputDir.getCanonicalPath(), "output.pdf"));
        outputDocument.close();
    }
    
    /**
     * Replaces all slashes or backslahes (depending on OS) from inputPath with dashes and then appends the postfix
     * 
     * @param inputPath Path to the reports directory
     * @param postfix e.g. output.pdf
     * @return 
     */
    private static String generateFilename(String inputPath, String postfix) {
        String dashedPath = inputPath.replace(System.getProperty("file.separator"),"-");
        String croppedPath = dashedPath.substring(3);
        System.out.println(croppedPath+"-"+ postfix);
        return  croppedPath+"-"+ postfix;
    }
}
