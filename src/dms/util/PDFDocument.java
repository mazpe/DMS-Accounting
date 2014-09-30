/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dms.util;

/**
 *
 * @author Kamran
 */

import javax.swing.*;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

/**
 * Class to display a PDF document in a JFrame
 */
public class PDFDocument extends JPanel
{

  public PDFDocument( String filename )
  {        
        String filePath = filename;        
      
        // build a component controller
        SwingController controller = new SwingController();
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        JPanel viewerComponentPanel = factory.buildViewerPanel();

        // add interactive mouse link annotation support via callback
        controller.getDocumentViewController().setAnnotationCallback(
                new org.icepdf.ri.common.MyAnnotationCallback(
                        controller.getDocumentViewController()));

        JFrame applicationFrame = new JFrame("Preview Check & Print");
        applicationFrame.setSize(1024, 768);
        //applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applicationFrame.getContentPane().add(viewerComponentPanel);

        // Now that the GUI is all in place, we can try openning a PDF
        controller.openDocument(filePath);

        // show the component
        applicationFrame.pack();
        applicationFrame.setVisible(true);     
  }  
}
