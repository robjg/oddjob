/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.oddjob.io.WildcardSpec.AboveAndBelow;
import org.oddjob.io.WildcardSpec.DirectorySplit;
import org.oddjob.tools.OurDirs;

public class WildcardSpecTest extends TestCase {

	
    public void testAboveAndBelow() throws IOException {
    	File f = new File("a/b/*/c/*");
    	AboveAndBelow ab1 = new AboveAndBelow(f);
    	
    	assertEquals(new File("a/b/*/c"), ab1.parent);
    	assertEquals("*", ab1.name);
    	assertNull(ab1.below);
    	assertFalse(ab1.top);

    	AboveAndBelow ab2 = new AboveAndBelow(ab1);
    	assertEquals(new File("a/b/*"), ab2.parent);
    	assertEquals("c", ab2.name);
    	assertEquals(new File("*"), ab2.below);
    	assertFalse(ab2.top);

    	AboveAndBelow ab3 = new AboveAndBelow(ab2);
    	assertEquals(new File("a/b"), ab3.parent);
    	assertEquals("*", ab3.name);
    	assertEquals(new File("c/*"), ab3.below);
    	assertFalse(ab3.top);
    	
    	AboveAndBelow ab4 = new AboveAndBelow(ab3);
    	assertEquals(new File("a"), ab4.parent);
    	assertEquals("b", ab4.name);
    	assertEquals(new File("*/c/*"), ab4.below);
    	assertFalse(ab4.top);

    	AboveAndBelow ab5 = new AboveAndBelow(ab4);
    	assertEquals(new File(".").getAbsoluteFile().getParentFile(), ab5.parent);
    	assertEquals("a", ab5.name);
    	assertEquals(new File("b/*/c/*"), ab5.below);
    	assertTrue(ab5.top);    	
    }
    
    public void testAboveAndBelow2() throws IOException {
    	File f = new File("/");
    	AboveAndBelow ab1 = new AboveAndBelow(f);
    	
    	assertNull(ab1.parent);
       	assertEquals("", ab1.name);
    	assertNull(ab1.below);
    	assertTrue(ab1.top);
   }
    	
    public void testAboveAndBelow3() throws IOException {
    	File f = new File("/a/b");
    	
    	AboveAndBelow ab1 = new AboveAndBelow(f);
    	
    	assertEquals(new File("/a"), ab1.parent);
       	assertEquals("b", ab1.name);
    	assertNull(ab1.below);
    	assertFalse(ab1.top);
    	
    	AboveAndBelow ab2 = new AboveAndBelow(ab1);
    	
    	assertEquals(new File("/"), ab2.parent);
       	assertEquals("a", ab2.name);
    	assertEquals(new File("b"), ab2.below);
    	assertTrue(ab2.top);
   }
    
    public void testSplitRelative() {
    	DirectorySplit test = new DirectorySplit(new File("a/b/*/c/*"));
    	assertEquals(3, test.getSize());
    	    	
    	assertEquals(new File("a/b"), test.getParentFile());
    	assertEquals("*", test.getName());
    	
    	test = test.next("x");
    	
    	assertEquals(new File("a/b/x"), test.getParentFile());
    	assertEquals("c", test.getName());
    	
    	test = test.next("c");
    	
    	assertEquals(new File("a/b/x/c"), test.getParentFile());
    	assertEquals("*", test.getName());
    	
    	assertNull(test.next("foo"));
    }
    
    public void testSplitAbsolute() {
    	DirectorySplit test = new DirectorySplit(new File("/a/b/*/c/*"));
    	assertEquals(3, test.getSize());
    	    	
    	assertEquals(new File("/a/b"), test.getParentFile());
    	assertEquals("*", test.getName());
    	
    	test = test.next("x");
    	
    	assertEquals(new File("/a/b/x"), test.getParentFile());
    	assertEquals("c", test.getName());
    	
    	test = test.next("c");
    	
    	assertEquals(new File("/a/b/x/c"), test.getParentFile());
    	assertEquals("*", test.getName());
    	
    	assertNull(test.next("foo"));
    }
    
    public void testSimple() {
    	OurDirs dirs = new OurDirs();
    	
    	WildcardSpec test = new WildcardSpec(
    			dirs.base() + "/test/io/reference/test*");

    	File[] result = test.findFiles();
    	assertEquals(3, result.length);
    	
    	Set<File> set = new HashSet<File>(Arrays.asList(result)); 
    	
    	assertTrue(set.contains(
    			new File(dirs.base() + "/test/io/reference/test1.txt")));
    	assertTrue(set.contains(
    			new File(dirs.base() + "/test/io/reference/test2.txt")));
    }
    
    public void testHarder() {
    	OurDirs dirs = new OurDirs();
    	
    	WildcardSpec test = new WildcardSpec(
    			dirs.base() + "/test/io/reference/*/x/*.txt");

    	File[] result = test.findFiles();
    	assertEquals(2, result.length);
    	
    	Set<File> set = new HashSet<File>(Arrays.asList(result)); 
    	assertTrue(set.contains(
    			new File(dirs.base() + "/test/io/reference/a/x/test3.txt")));
    	assertTrue(set.contains(
    			new File(dirs.base() + "/test/io/reference/b/x/test4.txt")));
    }

    public void testHarder2() {
    	OurDirs dirs = new OurDirs();
    	
    	WildcardSpec test = new WildcardSpec(
    			new File(dirs.base(), "test/io/reference/*/*/*.txt").getPath());

    	File[] result = test.findFiles();
    	assertEquals(3, result.length);
    	
    	Set<File> set = new HashSet<File>(Arrays.asList(result)); 
    	assertTrue(set.contains(
    			new File(dirs.base(), "test/io/reference/a/x/test3.txt")));
    	assertTrue(set.contains(
    			new File(dirs.base(), "test/io/reference/b/x/test4.txt")));
    	assertTrue(set.contains(
    			new File(dirs.base(), "test/io/reference/a/y/test5.txt")));
    }
}
