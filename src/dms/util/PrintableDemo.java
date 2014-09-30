package dms.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

public class PrintableDemo implements Printable {
  public int print(Graphics g, PageFormat pf, int pageIndex) {
    if (pageIndex != 0)
      return NO_SUCH_PAGE;
    Graphics2D g2 = (Graphics2D) g;
    g2.setFont(new Font("Serif", Font.PLAIN, 36));
    g2.setPaint(Color.black);
    g2.drawString("Java Source and Support", 100, 100);
    Rectangle2D outline = new Rectangle2D.Double(pf.getImageableX(), pf
        .getImageableY(), pf.getImageableWidth(), pf
        .getImageableHeight());
    g2.draw(outline);
    return PAGE_EXISTS;
  }  
}
