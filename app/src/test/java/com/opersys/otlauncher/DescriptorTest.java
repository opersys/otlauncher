/*
 * Copyright (C) 2015 Opersys inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opersys.otlauncher;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test case for the descriptor.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DescriptorTest {

    private void testGoodDescriptor(Descriptor descriptor) {
        Descriptor.Package pkg;
        Descriptor.PackageFile pkgFile;
        Descriptor.PackageFileMode pkgFileMode;

        // Test the descriptor root.
        assertNotNull(descriptor);
        assertNotNull(descriptor.getPackages());
        assertTrue(descriptor.getPackages().length == 1);
        assertNotNull(descriptor.getPackage("binder-explorer"));

        // Test the package content.
        pkg = descriptor.getPackage("binder-explorer");
        assertEquals("./node app.js", pkg.getCommand());
        assertEquals("Binder Explorer v0.1", pkg.getDescription());
        assertEquals("binder-explorer", pkg.getId());
        assertNotNull(pkg.getFileForArch("arm"));
        assertNotNull(pkg.getFileForArch("ia32"));

        // Test the files content.
        pkgFile = pkg.getFileForArch("arm");
        assertEquals("arm", pkgFile.getArchitecture());
        assertEquals("UNIMPORTANT CONTENT", pkgFile.getMd5Sum());
        assertEquals("assets/binder-explorer_arm.zip", pkgFile.getFile());
        pkgFileMode = pkgFile.getPackageFileModes()[0];
        assertNotNull(pkgFileMode);
        assertEquals("node", pkgFileMode.getFile());
        assertEquals("0755", pkgFileMode.getMode());

        pkgFile = pkg.getFileForArch("ia32");
        assertEquals("ia32", pkgFile.getArchitecture());
        assertEquals("UNIMPORTANT CONTENT", pkgFile.getMd5Sum());
        assertEquals("assets/binder-explorer_ia32.zip", pkgFile.getFile());
        pkgFileMode = pkgFile.getPackageFileModes()[0];
        assertEquals("node", pkgFileMode.getFile());
        assertEquals("0755", pkgFileMode.getMode());

    }

    /**
     * This is the descriptor for what was once the Binder Explorer, as is, outside
     * an .apk. The set* methods are generally not tested here since they are very
     * simple in the {@see Descriptor} class.
     */
    @Test
    public void testSimpleDescriptor() throws IOException, JSONException {
        DescriptorReader dreader;
        Descriptor descriptor;

        dreader = new DescriptorReader("{\n" +
                "\"version\":\"1.0\",\n" +
                "\"packages\":[\n" +
                "{\n" +
                "\"id\":\"binder-explorer\",\n" +
                "\"desc\":\"Binder Explorer v0.1\",\n" +
                "\"cmd\":\"./node app.js\",\n" +
                "\"files\":[\n" +
                "{\n" +
                "\"file\":\"assets/binder-explorer_ia32.zip\",\n" +
                "\"md5sum\":\"UNIMPORTANT CONTENT\",\n" +
                "\"arch\":\"ia32\",\n" +
                "\"modes\":{\"node\":\"0755\"}\n" +
                "},\n" +
                "{\n" +
                "\"file\":\"assets/binder-explorer_arm.zip\",\n" +
                "\"md5sum\":\"UNIMPORTANT CONTENT\",\n" +
                "\"arch\":\"arm\",\n" +
                "\"modes\":{\"node\":\"0755\"}\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]\n" +
                "}\n");

        // Test the descriptor root.
        descriptor = dreader.getDescriptor();
        testGoodDescriptor(descriptor);
    }

    /**
     * Test loading of the descriptor from within a .apk
     */
    @Test
    public void testDescriptorApk() throws IOException, JSONException {
        DescriptorReader dreader;
        Descriptor descriptor;
        File apkFile;

        apkFile = new File("src/test/apks/GoodDescriptor.apk");
        dreader = new DescriptorReader(apkFile);

        descriptor = dreader.getDescriptor();
        testGoodDescriptor(descriptor);
    }

    /**
     * Test an .apk without descriptor.
     */
    @Test
    public void testNoDescriptorApk() {
        DescriptorReader dreader;
        File apkFile;

        apkFile = new File("src/test/apks/NoDescriptor.apk");

        try {
            dreader = new DescriptorReader(apkFile);
            dreader.getDescriptor();
            fail("There is no descriptor in that file.");

        } catch (IOException ex) {
            // 'tis alright!
        } catch (JSONException ex) {
            fail("There is no descriptor");
        }
    }
}
