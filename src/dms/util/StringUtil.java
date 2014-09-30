/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dms.util;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Lester Ariel Mesa
 */
public class StringUtil
{

    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
        } catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }
    
    public static boolean isDate(CharSequence date) {
 
    // some regular expression
    String time = "(\\s(([01]?\\d)|(2[0123]))[:](([012345]\\d)|(60))"
        + "[:](([012345]\\d)|(60)))?"; // with a space before, zero or one time
 
    // no check for leap years (Schaltjahr)
    // and 31.02.2006 will also be correct
    String day = "(([12]\\d)|(3[01])|(0?[1-9]))"; // 01 up to 31
    String month = "((1[012])|(0\\d))"; // 01 up to 12
    String year = "\\d{4}";
 
    // define here all date format
    List<Pattern> patterns = new ArrayList<Pattern>();
    //patterns.add(Pattern.compile(day + "[-./]" + month + "[-./]" + year + time));
    patterns.add(Pattern.compile(month + "[-./]" + day + "[-./]" + year + time));
    patterns.add(Pattern.compile(year + "-" + month + "-" + day + time));
    //patterns.add(Pattern.compile(year + "[/]" + month + "[/]" + day + time));
    //patterns.add(Pattern.compile(year + "/" + month + "/" + day));
    // here you can add more date formats if you want
 
    // check dates
    for (Pattern p : patterns)
      if (p.matcher(date).matches())
        return true;
 
    return false;
  }
/*    
    public static void main(String[] args) {
 
    ArrayList<String> dates = new ArrayList<String>();
    //dates.add("05.10.1981"); // swiss date format (dd.MM.yyyy)
    //dates.add("05-10-1981");
    //dates.add("07-09-2006 23:00:33");
    //dates.add("2006-09-07 23:01:24");
    //dates.add("2003-08-30");
    dates.add("12/13/2009");
    //dates.add("2003-30-30"); // false
    dates.add("some text");  // false
 
    for (String d : dates)
      System.out.println(isDate(d));
  }
    */
}