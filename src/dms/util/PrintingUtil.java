/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.util;

import java.awt.GridLayout;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.HashMap;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.swing.JPanel;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.swing.JRViewer;

/**
 *
 * @author Administrator
 */
public class PrintingUtil
{
  public static void previewReport(JPanel viewPanel, ResultSet r, String xmlString) throws Exception
  {
    PrintServiceAttributeSet psAS = new HashPrintServiceAttributeSet();
    JRResultSetDataSource rSDS = new JRResultSetDataSource(r);
    JasperReport jasperReport = JasperCompileManager.compileReport(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
    JasperPrint jp = JasperFillManager.fillReport(jasperReport, null, rSDS);
    JRViewer viewer = new JRViewer(jp);
    viewer.setZoomRatio(0.70f);
    viewer.setVisible(true);
    viewPanel.removeAll();
    viewPanel.setLayout(new GridLayout(1,1));
    viewPanel.add(viewer);
    r.getStatement().close();
  }
  
  public static void previewReport(JPanel viewPanel, ResultSet r, InputStream xmlStream) throws Exception
  {
    JRResultSetDataSource rSDS = new JRResultSetDataSource(r);
    JasperPrint jp = JasperFillManager.fillReport(xmlStream, null, rSDS);
    
    JRViewer viewer = new JRViewer(jp);
    viewer.setZoomRatio(0.70f);
    viewer.setVisible(true);
    viewPanel.removeAll();
    viewPanel.setLayout(new GridLayout(1,1));
    viewPanel.add(viewer);

    r.getStatement().close();
  }
  
  public static void previewReport(JPanel viewPanel, ResultSet r1, ResultSet r2, InputStream xmlStream1, InputStream xmlStream2) throws Exception
  {
    JRResultSetDataSource rsds = new JRResultSetDataSource(r1);
    JRResultSetDataSource rsdsSubSet = new JRResultSetDataSource(r2);
    HashMap parameterMap = new HashMap();
    parameterMap.put("JobsDataSet", rsdsSubSet);
    parameterMap.put("SUBREPORT_1", xmlStream2);
    JasperPrint jp = JasperFillManager.fillReport(xmlStream1, parameterMap, rsds);
    
    JRViewer viewer = new JRViewer(jp);
    viewer.setZoomRatio(0.70f);
    viewer.setVisible(true);
    viewPanel.removeAll();
    viewPanel.setLayout(new GridLayout(1,1));
    viewPanel.add(viewer);

    r2.getStatement().close();
  }
  
  public static void printReport(ResultSet rs, String xmlString) throws Exception
  {
    JRResultSetDataSource rSDS = new JRResultSetDataSource(rs);
    JasperReport jasperReport = JasperCompileManager.compileReport(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
    JasperPrint jp = JasperFillManager.fillReport(jasperReport, null, rSDS);
    JasperPrintManager.printReport(jp, true);
    rs.getStatement().close();
  }
  
  public static void printReport(ResultSet rs, InputStream xmlString) throws Exception
  {
    JRResultSetDataSource rSDS = new JRResultSetDataSource(rs);
    JasperReport jasperReport = JasperCompileManager.compileReport(xmlString);
    JasperPrint jp = JasperFillManager.fillReport(jasperReport, null, rSDS);
    JasperPrintManager.printReport(jp, true);
    rs.getStatement().close();
  }
}
