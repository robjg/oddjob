/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.io.WildcardSpec.DirectorySplit;
import org.oddjob.tools.OurDirs;

public class WildcardSpecTest extends TestCase {

	private static final Logger logger = Logger.getLogger(WildcardSpecTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("---------------------  " + getName() + 
				"  --------------------------");
	}
	
    public void testDirectorySplitForRootFile() throws IOException {
    	File root = new File("/a");
    	
    	assertEquals(new File("/"), root.getParentFile());
    	assertEquals("a", root.getName());
    	
    	DirectorySplit test = new DirectorySplit(root);
    	
    	assertEquals(root, test.currentFile());
    	assertEquals(new File("/"), test.getParentFile());
       	assertEquals("a", test.getName());
    	assertEquals(true, test.isBottom());
    }
    
    
    public void testDirectorySplitForRootOnly() throws IOException {
    	
    	File root = new File("/");
    	
    	assertEquals(null, root.getParent());
    	assertEquals("", root.getName());
    	
    	DirectorySplit test = new DirectorySplit(root);
    	
    	assertEquals(null, test.getParentFile());
       	assertEquals("", test.getName());
    	assertEquals(true, test.isBottom());
   }
    	
    public void testDirectorySplitFromRootWithDirectoryWildcard() throws IOException {
    	File file = new File("/*/a/b");
    	
    	DirectorySplit test1 = new DirectorySplit(file);
    	
    	assertEquals(new File("/"), test1.getParentFile());
       	assertEquals("*", test1.getName());
    	assertEquals(false, test1.isBottom());
    	
    	DirectorySplit test2 = test1.next("x");
    	
    	assertEquals(new File("/x/a/b"), test2.currentFile());
       	assertEquals("b", test2.getName());
    	assertEquals(true, test2.isBottom());
    }

    public void testDirectorySplitForSingleFile() throws IOException {
    	
    	DirectorySplit test = new DirectorySplit(new File("a"));
    	
    	assertEquals(new File("a"), test.currentFile());
    	assertEquals(null, test.getParentFile());
       	assertEquals("a", test.getName());
    	assertEquals(true, test.isBottom());
    }
    
    public void testDirectorySplitDirectoriesNoWildcard() throws IOException {
    	
    	DirectorySplit test = new DirectorySplit(new File("a/b/c"));
    	
    	assertEquals("c", test.getName());
    	assertEquals(true, test.isBottom());
    	assertEquals(new File("a/b"), test.getParentFile());
    	
    	assertEquals(null, test.next("c"));
    }
    
    public void testDirctorySplitOneDirectoryAndAWildcard() throws IOException {
    	File file = new File("a/???.txt");
    	
    	DirectorySplit test1 = new DirectorySplit(file);
    	
    	assertEquals(new File("a"), test1.getParentFile());
    	assertEquals("???.txt", test1.getName());
    	assertEquals(true, test1.isBottom());
    }
    
    public void testDirectorySplitOneWildcardDirectory() throws IOException {
    	File file = new File("a/??/c");
    	
    	DirectorySplit test1 = new DirectorySplit(file);
    	
    	assertEquals(new File("a"), test1.getParentFile());
    	assertEquals("??", test1.getName());
    	assertEquals(false, test1.isBottom());
    	
    	DirectorySplit test2 = test1.next("x");
    	assertEquals(new File("a/x"), test2.getParentFile());
    	assertEquals("c", test2.getName());
    	assertEquals(true, test2.isBottom());
    }
    	
    public void testDirectorySplitOneDirectoryWildcardAndAFinalWildcard() throws IOException {
    	File file = new File("a/b/*/c/*");
    	
    	DirectorySplit test1 = new DirectorySplit(file);
    	
    	assertEquals(new File("a/b"), test1.getParentFile());
    	assertEquals("*", test1.getName());
    	assertEquals(false, test1.isBottom());

    	DirectorySplit test2 = test1.next("x");
    	assertEquals(new File("a/b/x/c"), test2.getParentFile());
    	assertEquals("*", test2.getName());
    	assertEquals(true, test2.isBottom());
    }
    
    public void testDirectorySplitRelative() throws IOException {
    	
    	DirectorySplit test = new DirectorySplit(new File(".."));
    	
    	assertEquals("..", test.getName());
    	assertEquals(null, test.getParentFile());
    	assertEquals(true, test.isBottom());
    }
    
    public void testDirectorySplitOneWildcard() throws IOException {
    	
    	DirectorySplit test = new DirectorySplit(new File("a/???/c"));
    	
    	assertEquals("???", test.getName());
    	assertEquals(false, test.isBottom());
    	assertEquals(new File("a"), test.getParentFile());
    	
    	DirectorySplit test2 = test.next("b");
    	
    	assertEquals("c", test2.getName());
    	assertEquals(true, test2.isBottom());
    	assertEquals(new File("a/b"), test2.getParentFile());
    	
    	assertEquals(null, test2.next("x"));
    }
    
    public void testDirectorySplitOneWildcardAboveTwoDirectories() throws IOException {
    	
    	DirectorySplit test = new DirectorySplit(new File("a/???/c/d"));
    	
    	assertEquals("???", test.getName());
    	assertEquals(false, test.isBottom());
    	assertEquals(new File("a"), test.getParentFile());
    	
    	DirectorySplit test2 = test.next("b");
    	
    	assertEquals("d", test2.getName());
    	assertEquals(true, test2.isBottom());
    	assertEquals(new File("a/b/c"), test2.getParentFile());
    	
    	assertEquals(null, test2.next("x"));
    }
    
    public void testDirectorySplitManyWildcards() throws IOException {
    	
    	DirectorySplit test = new DirectorySplit(new File("?/?/?/?/x"));
    	
    	assertEquals("?", test.getName());
    	assertEquals(false, test.isBottom());
    	assertEquals(null, test.getParentFile());
    	
    	test = test.next("a");

    	assertEquals("?", test.getName());
    	assertEquals(false, test.isBottom());
    	assertEquals(new File("a"), test.getParentFile());
    	
    	test = test.next("b");

    	assertEquals("?", test.getName());
    	assertEquals(false, test.isBottom());
    	assertEquals(new File("a/b"), test.getParentFile());
    	
    	test = test.next("c");

    	assertEquals("?", test.getName());
    	assertEquals(false, test.isBottom());
    	assertEquals(new File("a/b/c"), test.getParentFile());
    	
    	test = test.next("d");

    	assertEquals("x", test.getName());
    	assertEquals(true, test.isBottom());
    	assertEquals(new File("a/b/c/d"), test.getParentFile());
    }
        
    public void testSplitAbsoluteWildcardDirectoryAndAWildcard() throws IOException {
    	DirectorySplit test = new DirectorySplit(new File("/a/b/*/c/*"));
    	assertEquals(2, test.getSize());
    	    	
    	assertEquals(new File("/a/b"), test.getParentFile());
    	assertEquals("*", test.getName());
    	assertEquals(false, test.isBottom());
    	
    	test = test.next("x");
    	
    	assertEquals(new File("/a/b/x/c"), test.getParentFile());
    	assertEquals("*", test.getName());
    	assertEquals(true, test.isBottom());
    }
    
    public void testSimple() throws IOException {
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
    
    public void testHarder() throws IOException {
    	OurDirs dirs = new OurDirs();
    	
    	WildcardSpec test = new WildcardSpec(
    			dirs.base() + "/test/io/reference/*/x/*.txt");

    	File[] result = test.findFiles();
    	assertEquals(2, result.length);
    	
    	assertEquals(new File(dirs.base() + 
    			"/test/io/reference/a/x/test3.txt"), result[0]);
    	assertEquals(new File(dirs.base() + 
    			"/test/io/reference/b/x/test4.txt"), result[1]);
    }

    public void testHarder2() throws IOException {
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
    
    public void testFileThatDoesntExist() throws IOException {
    	
    	WildcardSpec test = new WildcardSpec(
    			new File("IdontExist").getPath());
    	
    	File[] files = test.findFiles();
    	
    	assertEquals(1, files.length);
    	assertEquals(new File("IdontExist").getCanonicalFile(), files[0]);
    }
    
    public void testRoot() throws IOException {
    	
    	WildcardSpec test = new WildcardSpec(new File("/"));
    	
    	File[] files = test.findFiles();
    	
    	assertEquals(1, files.length);
    	assertEquals(new File("/").getAbsoluteFile(), files[0]);
    }
    
    public void testRootWithWildcard() throws IOException {
    	
    	WildcardSpec test = new WildcardSpec("/*");
    	
    	File[] files = test.findFiles();
    	
    	assertEquals(true, files.length > 0);
    	
    	assertEquals(true, files[0].isAbsolute());
    	
    	for (File file : files) {
        	logger.info(file.getPath());
    	}
    }
    
    public void testCurrentDir() throws IOException {
    	
    	WildcardSpec test = new WildcardSpec(new File("*"));
    	
    	File[] files = test.findFiles();
    	
    	assertEquals(true, files.length > 0);
    	
    	assertEquals(true, files[0].isAbsolute());
    	
    	for (File file : files) {
        	logger.info(file.getPath());
    	}
    }
    
    public void testRelativePath() throws IOException {
    	
    	OurDirs dirs = new OurDirs();
    	
    	WildcardSpec test = new WildcardSpec(
    			new File(dirs.base(), "../*"));
    	
    	File[] files = test.findFiles();
    	
    	for (File file : files) {
        	logger.info(file.getPath());
    	}
    	
    	List<File> list = Arrays.asList(files);

    	assertEquals(true, list.contains(
    			new File(new File(dirs.base(), "..").getCanonicalFile(), "oddjob")));
    }
}
