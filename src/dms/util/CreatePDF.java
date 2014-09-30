/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.util;

import java.io.FileOutputStream;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreatePDF {

    public void generatePDF(String fileName, int checkNo, String checkDate, String checkAmount, String payTo, String[] chequeDataAdditional) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            //addTitlePage(document);
            addContentToSection1(document, checkNo, checkDate, checkAmount, payTo);
            addContentToSection2(document, chequeDataAdditional);
            Paragraph emptyPara = new Paragraph();
            addEmptyLine(emptyPara, 5);
            document.add(emptyPara);
            addContentToSection2(document, chequeDataAdditional);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addContentToSection1(Document document, int checkNo, String checkDate, String checkAmount, String payTo) throws DocumentException {
        try {
            if (checkAmount.contains(",")) {
                checkAmount = checkAmount.replaceAll(",", "");
            }
            if (checkAmount.contains("-")) {
                checkAmount = checkAmount.replaceAll("-", "");
            }
            if (checkAmount.contains("-") && checkAmount.contains(",")) {
                checkAmount = String.valueOf(checkAmount.indexOf("-") + 1);
            }

            Paragraph emptyPara = new Paragraph();
            addEmptyLine(emptyPara, 1);
            document.add(emptyPara);

            String checkAmountInWordsToDisplay = EnglishNumberToWords.convert(Long.parseLong(checkAmount.substring(0, checkAmount.indexOf("."))));

            Paragraph datePara = new Paragraph("                            " + checkDate);
            datePara.setAlignment(datePara.ALIGN_RIGHT);
            document.add(datePara);

            /*Paragraph emptyPara1 = new Paragraph();
             addEmptyLine(emptyPara1, 1);
             document.add(emptyPara1);
             */

            String payToAndAmountFigureValue = "";
            String minSpacing = "                          ";
            if (payTo.length() <= 20) {
                System.out.println("payTo length less than 30");
                Paragraph emptyPara1 = new Paragraph();
                addEmptyLine(emptyPara1, 1);
                document.add(emptyPara1);
                String emptyStringToAdd = "";
                int diff = 80 - payTo.length();
                System.out.println("Diff : " + diff);
                for (int i = 0; i < diff; i++) {
                    emptyStringToAdd += " ";
                }
                minSpacing = minSpacing.concat(emptyStringToAdd);
                payTo = "             " + payTo;
                payToAndAmountFigureValue = payTo + minSpacing + checkAmount;
            } else if (payTo.length() > 20 && payTo.length() < 30) {
                System.out.println("payTo length less than 30");
                String emptyStringToAdd = "";
                int diff = 80 - payTo.length();
                System.out.println("Diff : " + diff);
                for (int i = 0; i < diff; i++) {
                    emptyStringToAdd += " ";
                }
                minSpacing = minSpacing.concat(emptyStringToAdd);
                payTo = "             " + payTo;
                payToAndAmountFigureValue = payTo + minSpacing + checkAmount;
            } else if (payTo.length() > 30) {
                System.out.println("payTo length less than 30");
                String emptyStringToAdd = "";
                int diff = 80 - payTo.length();
                System.out.println("Diff : " + diff);
                for (int i = 0; i < diff; i++) {
                    emptyStringToAdd += " ";
                }
                minSpacing = minSpacing.concat(emptyStringToAdd);
                payTo = "\n             " + payTo;
                payToAndAmountFigureValue = payTo + minSpacing + checkAmount;
            }

            /*else if (payTo.length() > 60) {
             System.out.println("payTo greater than 60");
             String payToLine1 = payTo.substring(0, 60);
             String payToLine2 = payTo.substring(15, payTo.length());
             String emptyStringToAdd = "";
             if (payToLine2.length() < 60) {
             int diff = 65 - payToLine2.length();
             System.out.println("Diff : " + diff);
             for (int i = 0; i < diff; i++) {
             emptyStringToAdd += " ";
             }
             minSpacing = minSpacing.concat(emptyStringToAdd);
             }
             payToAndAmountFigureValue = payToLine1 + "\n           " + payToLine2 + minSpacing + checkAmount;
             }
             */

            Paragraph payToPara = new Paragraph(payToAndAmountFigureValue);
            payToPara.setAlignment(payToPara.ALIGN_LEFT);
            document.add(payToPara);

            Paragraph checkAmountPara = new Paragraph("          " + checkAmountInWordsToDisplay.toUpperCase().concat(" DOLLARS ONLY "));
            checkAmountPara.setAlignment(checkAmountPara.ALIGN_LEFT);
            document.add(checkAmountPara);

            Paragraph nameAndAddressPara = new Paragraph("Name \n Address Line 1 \n Address Line 2");
            addEmptyLine(nameAndAddressPara, 1);
            document.add(nameAndAddressPara);

        } catch (BadElementException ex) {
            Logger.getLogger(CreatePDF.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private void addContentToSection2(Document document, String[] chequeDataAdditional) {
        try {

            Paragraph emptyPara = new Paragraph();
            addEmptyLine(emptyPara, 5);
            document.add(emptyPara);

            String[] currentValArray = chequeDataAdditional[0].split("\\*\\*\\*");

            Paragraph controlNumPara = new Paragraph("Control # (To be added)                                                                                                " + currentValArray[2]);
            controlNumPara.setAlignment(controlNumPara.ALIGN_LEFT);
            document.add(controlNumPara);

            for (int i = 0; i < chequeDataAdditional.length; i++) {
                System.out.println("Line : " + chequeDataAdditional[i]);
                if (chequeDataAdditional[i] == null) {
                    return;
                }

                String bankAccount = currentValArray[0];
                String paidTo = currentValArray[1];
                String depositDate = currentValArray[2];
                String amount = currentValArray[3];
                String memo = currentValArray[4];
                String depositNumber = currentValArray[5];

                String lineOfData = "Acc#"+ paidTo;

                Paragraph lineDataPara = new Paragraph(lineOfData);
                lineDataPara.setAlignment(lineDataPara.ALIGN_CENTER);
                document.add(lineDataPara);
            }
        } catch (DocumentException ex) {
            Logger.getLogger(CreatePDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
