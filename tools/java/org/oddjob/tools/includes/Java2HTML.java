package org.oddjob.tools.includes;

//java2html.java

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * I found this little gem at 
 * http://www.geocities.com/SiliconValley/Horizon/6481/Java2HTML.html
 * and modified it slightly to add unique colours for strings
 * and the two different comment types both of which are also italicized.
 * All default colours are websafe and can be customized by
 * specifying an optimal colours property file.
 *
 * @author Melinda Green - Superliminal Software
 */
public class Java2HTML
{
    private static final String keywords[] = 
    {
        "abstract", "default",  "if",           "private",      "throw",
        "boolean",  "do",       "implements",   "protected",    "throws",
        "break",    "double",   "import",       "public",       "transient",
        "byte",     "else",     "instanceof",   "return",       "try",
        "case",     "extends",  "int",          "short",        "void",
        "catch",    "final",    "interface",    "static",       "volatile",
        "char",     "finally",  "long",         "super",        "while",
        "class",    "float",    "native",       "switch",
        "const",    "for",      "new",          "synchronized",
        "continue", "goto",     "package",      "this"
    };
    
    private static List<String> keyw = new ArrayList<String>(keywords.length);
    static 
    {
        for (int i = 0; i < keywords.length; i++)
            keyw.add(keywords[i]);
    }
    
    private int tabsize = 4;
    
    private String 
        kwcolor = "0000FF", // blue
        cmcolor = "009900", // dark faded green
        c2color = "999999", // light gray
        stcolor = "FF0000"; // red

    public String convert(String input) throws IOException
    {
        Reader in = new StringReader(input);
        StringWriter string = new StringWriter();
        
        PrintWriter out = new PrintWriter(string);
        
        out.println("<pre class=\"code\">");

        
        StringBuffer buf = new StringBuffer(2048);
        int c = 0, kwl = 0, bufl = 0;
        int nexttolast = 0; // just for handling case of >> "\\" << which is not escaping a double quote
        char ch = 0, lastch = 0;
        int s_normal  = 0;
        int s_string  = 1;
        int s_char    = 2;
        int s_comline = 3;
        int s_comment = 4;
        int state = s_normal;
        while (c != -1)
        {
            c = in.read();
            nexttolast = lastch;
            lastch = ch;
            ch = c >= 0 ? (char) c : 0;
            if (state == s_normal)
                if (kwl == 0 && Character.isJavaIdentifierStart(ch) 
                             && !Character.isJavaIdentifierPart(lastch)
                    || kwl > 0 && Character.isJavaIdentifierPart(ch))
                {
                    buf.append(ch);
                    bufl++;
                    kwl++;
                    continue;
                } else
                    if (kwl > 0)
                    {
                        String kw = buf.toString().substring(buf.length() - kwl);
                        if (keyw.contains(kw))
                        {
                            buf.insert(buf.length() - kwl, 
                                "<font color=\"" + kwcolor + "\">");
                            buf.append("</font>");
                        }
                        kwl = 0;
                    }
            switch (ch)
            {
                case '&':
                    buf.append("&amp;");
                    bufl++;
                    break;
                case '\"': // double quote
                    buf.append("&quot;");
                    bufl++;
                    if (state == s_normal) { // start string
                        state = s_string;
                        buf.insert(buf.length() - "&quot;".length(), 
                            "<font color=\"" + stcolor + "\"><i>");
                    }
                    else
                        if (state == s_string && ((lastch != '\\') || (lastch == '\\' && nexttolast == '\\'))) {
                            // inside a string and found either a non-escaped closing double quote,
                            // so close the string.
                            buf.append("</i></font>");
                            state = s_normal;
                        }
    
                    break;
                case '\'': // single quote
                    buf.append("\'");
                    bufl++;
                    if (state == s_normal)
                        state = s_char;
                    else
                        if (state == s_char && lastch != '\\')
                            state = s_normal;
                    break;
                case '\\': // backslash
                    buf.append("\\");
                    bufl++;
                    if(lastch == '\\')
                        nexttolast = '\\';
                    if (lastch == '\\' && (state == s_string || state == s_char))
                        ;//lastch = 0;
                    break;
                case '/': // forward slash
                    buf.append("/");
                    bufl++;
                    if(state == s_string || state == s_comline)
                        break;
                    if (state == s_comment && lastch == '*') // star slash ends c++ comment
                    {
                        buf.append("</i></font>");
                        state = s_normal;
                    }
                    if(state == s_comment)
                        break;
                    if (lastch == '/') // second forward slash starts line comment
                    {
                        buf.insert(buf.length() - 2, 
                            "<font color=\"" + cmcolor + "\"><i>");
                        state = s_comline;
                    }
                    break;
                case '*':
                    buf.append("*");
                    bufl++;
                    if (state == s_normal && lastch == '/') // slash star starts c++ comment
                    {
                        buf.insert(buf.length() - 2, 
                            "<font color=\"" + c2color + "\"><i>");
                        state = s_comment;
                    }
                    break;
                case '<':
                    buf.append("&lt;");
                    bufl++;
                    break;
                case '>':
                    buf.append("&gt;");
                    bufl++;
                    break;
                case '\t':
                    int n = bufl / tabsize * tabsize + tabsize;
                    while (bufl < n)
                    {
                        buf.append(' ');
                        bufl++;
                    }
                    break;
                case '\r':
                case '\n':
                    if (state == s_comline) // EOL ends line comment
                    {
                        buf.append("</i></font>");
                        state = s_normal;
                    }
                    buf.append(ch);
                    if (buf.length() >= 1024)
                    {
                        out.write(buf.toString());
                        buf.setLength(0);
                    }
                    bufl = 0;
                    if (kwl != 0)
                        kwl = 0; // This should never execute
                    if (state != s_normal && state != s_comment)
                        state = s_normal; // Syntax Error
                    break;
                case 0:
                    if (c < 0)
                    {
                        if (state == s_comline)
                        {
                            buf.append("</font>");
                            state = s_normal;
                        }
                        out.write(buf.toString());
                        buf.setLength(0);
                        bufl = 0;
                        if (state == s_comment)
                        {
                            // Syntax Error
                            buf.append("</font>");
                            state = s_normal;
                        }
                        break;
                    }
                default:
                    bufl++;
                    buf.append(ch);
            }
        }
        out.println("</pre>");
        in.close();
        out.close();
        
        return string.toString();
    }


	public int getTabsize() {
		return tabsize;
	}

	public void setTabsize(int tabsize) {
		this.tabsize = tabsize;
	}

	public String getKwcolor() {
		return kwcolor;
	}

	public void setKwcolor(String kwcolor) {
		this.kwcolor = kwcolor;
	}

	public String getCmcolor() {
		return cmcolor;
	}

	public void setCmcolor(String cmcolor) {
		this.cmcolor = cmcolor;
	}

	public String getC2color() {
		return c2color;
	}

	public void setC2color(String c2color) {
		this.c2color = c2color;
	}

	public String getStcolor() {
		return stcolor;
	}

	public void setStcolor(String stcolor) {
		this.stcolor = stcolor;
	}

}

