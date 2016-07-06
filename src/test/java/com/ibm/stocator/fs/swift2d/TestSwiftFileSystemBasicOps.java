/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ibm.stocator.fs.swift2d;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.ibm.stocator.fs.ObjectStoreFileSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.ParentNotDirectoryException;
import org.apache.hadoop.fs.Path;
import org.junit.Ignore;
import org.junit.Test;

import static com.ibm.stocator.fs.swift2d.SwiftTestUtils.assertFileHasLength;
import static com.ibm.stocator.fs.swift2d.SwiftTestUtils.readBytesToString;
import static com.ibm.stocator.fs.swift2d.SwiftTestUtils.writeTextFile;


/**
 * Test basic filesystem operations.
 * -this is a JUnit4 test suite used to initially test the Swift
 * component. Once written, there's no reason not to retain these tests.
 */
public class TestSwiftFileSystemBasicOps extends SwiftFileSystemBaseTest {

  private static final Log LOG =
          LogFactory.getLog(TestSwiftFileSystemBasicOps.class);


  @Test(timeout = SWIFT_TEST_TIMEOUT)
  public void testLsRoot() throws Throwable {
    Path path = new Path(getBaseURI() + "/");
    FileStatus[] statuses = fs.listStatus(path);
  }


  @Test(timeout = SWIFT_TEST_TIMEOUT)
  public void testMkDir() throws Throwable {
    Path path = new Path(getBaseURI() + "/test/MkDir");
    fs.mkdirs(path);
    fs.delete(path, true);
  }

  @Ignore("Unexpected")
  public void testDeleteNonexistentFile() throws Throwable {
    Path path = new Path(getBaseURI() + "/test/DeleteNonexistentFile");
    assertFalse("delete returned true", fs.delete(path, false));
  }

  @Test(timeout = SWIFT_TEST_TIMEOUT)
  public void testPutFile() throws Throwable {
    Path path = new Path(getBaseURI() + "/test/PutFile");
    writeTextFile(fs, path, "Testing a put to a file", false);
    assertDeleted(path, false);
  }

  @Test(timeout = SWIFT_TEST_TIMEOUT)
  public void testPutGetFile() throws Throwable {
    Path path = new Path(getBaseURI() + "/test/PutGetFile");
    try {
      String text = "Testing a put and get to a file "
              + System.currentTimeMillis();
      writeTextFile(fs, path, text, false);

      String result = readBytesToString(fs, path, text.length());
      assertEquals(text, result);
    } finally {
      delete(fs, path);
    }
  }

  @Ignore("Not supported")
  public void testPutDeleteFileInSubdir() throws Throwable {
    Path path =
            new Path(getBaseURI() + "/test/PutDeleteFileInSubdir/testPutDeleteFileInSubdir");
    String text = "Testing a put and get to a file in a subdir "
            + System.currentTimeMillis();
    writeTextFile(fs, path, text, false);
    assertDeleted(path, false);
    //now delete the parent that should have no children
    assertDeleted(new Path(getBaseURI() + "/test/PutDeleteFileInSubdir"), false);
  }

  @Ignore("Not supported")
  public void testRecursiveDelete() throws Throwable {
    Path childpath =
            new Path(getBaseURI() + "/test/testRecursiveDelete");
    String text = "Testing a put and get to a file in a subdir "
            + System.currentTimeMillis();
    writeTextFile(fs, childpath, text, false);
    //now delete the parent that should have no children
    assertDeleted(new Path(getBaseURI() + "/test"), true);
    assertFalse("child entry still present " + childpath, fs.exists(childpath));
  }

  private void delete(ObjectStoreFileSystem fs, Path path) {
    try {
      if (!fs.delete(path, false)) {
        LOG.warn("Failed to delete " + path);
      }
    } catch (IOException e) {
      LOG.warn("deleting " + path, e);
    }
  }

  private void deleteR(ObjectStoreFileSystem fs, Path path) {
    try {
      if (!fs.delete(path, true)) {
        LOG.warn("Failed to delete " + path);
      }
    } catch (IOException e) {
      LOG.warn("deleting " + path, e);
    }
  }


  @Test(timeout = SWIFT_TEST_TIMEOUT)
  public void testOverwrite() throws Throwable {
    Path path = new Path(getBaseURI() + "/test/Overwrite");
    try {
      String text = "Testing a put to a file "
              + System.currentTimeMillis();
      writeTextFile(fs, path, text, false);
      assertFileHasLength(fs, path, text.length());
      String text2 = "Overwriting a file "
              + System.currentTimeMillis();
      writeTextFile(fs, path, text2, true);
      assertFileHasLength(fs, path, text2.length());
      String result = readBytesToString(fs, path, text2.length());
      assertEquals(text2, result);
    } finally {
      delete(fs, path);
    }
  }

  @Test(timeout = SWIFT_TEST_TIMEOUT)
  public void testOverwriteDirectory() throws Throwable {
    Path path = new Path(getBaseURI() + "/test/testOverwriteDirectory");
    try {
      fs.mkdirs(path.getParent());
      String text = "Testing a put to a file "
              + System.currentTimeMillis();
      writeTextFile(fs, path, text, false);
      assertFileHasLength(fs, path, text.length());
    } finally {
      delete(fs, path);
    }
  }

  /**
   * Assert that a newly created directory is a directory
   *
   * @throws Throwable if not, or if something else failed
   */
  @Ignore("Not supported")
  public void testDirStatus() throws Throwable {
    Path path = new Path("/test/DirStatus");
    try {
      fs.mkdirs(path);
      SwiftTestUtils.assertIsDirectory(fs, path);
    } finally {
      delete(fs, path);
    }
  }

  /**
   * Assert that if a directory that has children is deleted, it is still
   * a directory
   *
   * @throws Throwable if not, or if something else failed
   */
  @Ignore("Not supported")
  public void testDirStaysADir() throws Throwable {
    Path path = new Path("/test/dirStaysADir");
    Path child = new Path(path, "child");
    try {
      //create the dir
      fs.mkdirs(path);
      //assert the parent has the directory nature
      SwiftTestUtils.assertIsDirectory(fs, path);
      //create the child dir
      writeTextFile(fs, child, "child file", true);
      //assert the parent has the directory nature
      SwiftTestUtils.assertIsDirectory(fs, path);
      //now rm the child
      delete(fs, child);
    } finally {
      deleteR(fs, path);
    }
  }

  @Ignore("Not supported")
  public void testCreateMultilevelDir() throws Throwable {
    Path base = new Path(getBaseURI() + "/test/CreateMultilevelDir");
    Path path = new Path(base, "1/2/3");
    fs.mkdirs(path);
    assertExists("deep multilevel dir not created", path);
    fs.delete(base, true);
    assertPathDoesNotExist("Multilevel delete failed", path);
    assertPathDoesNotExist("Multilevel delete failed", base);

  }

  @Ignore("Not supported")
  public void testCreateDirWithFileParent() throws Throwable {
    Path path = new Path(getBaseURI() + "/test/CreateDirWithFileParent");
    Path child = new Path(path, "subdir/child");
    fs.mkdirs(path.getParent());
    try {
      //create the child dir
      writeTextFile(fs, path, "parent", true);
      try {
        fs.mkdirs(child);
      } catch (ParentNotDirectoryException expected) {
        LOG.debug("Expected Exception", expected);
      }
    } finally {
      fs.delete(path, true);
    }
  }

  @Ignore("Unexpected")
  public void testLongObjectNamesForbidden() throws Throwable {
    StringBuilder buffer = new StringBuilder(1200);
    buffer.append("/");
    for (int i = 0; i < (1200 / 4); i++) {
      buffer.append(String.format("%04x", i));
    }
    String pathString = buffer.toString();
    pathString = "/A/B/C";
    Path path = new Path(getBaseURI() + pathString);
    try {
      writeTextFile(fs, path, pathString, true);
      //if we get here, problems.
      fs.delete(path, false);
      fail("Managed to create an object with a name of length "
              + pathString.length());
    } catch (Exception e) {
      //expected
      //LOG.debug("Caught exception " + e, e);
    }
  }

  @Ignore("Unexpected")
  public void testLsNonExistentFile() throws Exception {
    try {
      Path path = new Path(getBaseURI() + "/test/hadoop/file");
      FileStatus[] statuses = fs.listStatus(path);
      fail("Should throw FileNotFoundException on " + path
              + " but got list of length " + statuses.length);
    } catch (FileNotFoundException fnfe) {
      // expected
    }
  }
}
