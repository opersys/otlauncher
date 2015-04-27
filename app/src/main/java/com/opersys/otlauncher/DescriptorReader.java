/*
 * Copyright (C) 2015, Opersys inc.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Date: 09/04/15
 * Time: 1:02 PM
 */
public class DescriptorReader {

    private final JSONTokener descTok;

    private ZipFile apkZip;

    private Descriptor descriptor;

    /**
     *
     * @throws IOException
     */
    private void readPackagesFileMode(Descriptor descriptor,
                                      Descriptor.PackageFile pkgFile,
                                      JSONObject fileModesObj)
            throws IOException, JSONException {
        String val, key;
        Descriptor.PackageFileMode pkgFileMode;
        Iterator<String> it;

        it = fileModesObj.keys();

        while (it.hasNext()) {
            key = it.next();
            val = fileModesObj.getString(key);

            pkgFileMode = descriptor.new PackageFileMode();
            pkgFileMode.setFile(key);
            pkgFileMode.setMode(val);

            pkgFile.addPackageFileMode(pkgFileMode);
        }
    }

    /**
     *
     *
     * @throws IOException
     */
    private void readPackageFiles(Descriptor descriptor,
                                  Descriptor.Package pkg,
                                  JSONArray filesArray) throws IOException, JSONException {
        JSONObject fileObj;
        Descriptor.PackageFile pkgFile;

        for (int i = 0; i < filesArray.length(); i++) {
            fileObj = filesArray.getJSONObject(i);
            pkgFile = descriptor.new PackageFile();

            pkgFile.setFile(fileObj.getString("file"));
            pkgFile.setMd5sum(fileObj.getString("md5sum"));
            pkgFile.setArchitecture(fileObj.getString("arch"));

            readPackagesFileMode(descriptor, pkgFile, fileObj.getJSONObject("modes"));

            pkg.addPackageFile(pkgFile);
        }
    }

    /**
     *
     * @throws IOException
     */
    private void readPackages(Descriptor descriptor, JSONArray pkgsArray)
            throws IOException, JSONException {
        JSONObject pkgObj;
        Descriptor.Package pkg;

        for (int i = 0; i < pkgsArray.length(); i++) {
            pkgObj = pkgsArray.getJSONObject(i);
            pkg = descriptor.new Package();

            pkg.setId(pkgObj.getString("id"));
            pkg.setDescription(pkgObj.getString("desc"));
            pkg.setCommand(pkgObj.getString("cmd"));

            readPackageFiles(descriptor, pkg, pkgObj.getJSONArray("files"));

            descriptor.addPackage(pkg);
        }
    }

    public Descriptor getDescriptor() throws IOException, JSONException {
        JSONObject descObj;
        JSONArray pkgsArray;
        String defaultAppId;

        if (descriptor != null)
            return descriptor;

        descObj = new JSONObject(descTok);

        if (!descObj.get("version").equals("1.0"))
            throw new JSONException("Unknown version: " + descObj.get("version"));

        defaultAppId = descObj.optString("default", null);
        descriptor = new Descriptor(apkZip, defaultAppId);
        pkgsArray = descObj.getJSONArray("packages");
        readPackages(descriptor, pkgsArray);

        return descriptor;
    }

    public void close()  {
        if (apkZip != null) {
            try {
                apkZip.close();
            } catch (IOException ex) {}
            apkZip = null;
        }
    }

    public DescriptorReader(String json) throws IOException {
        descTok = new JSONTokener(json);
    }

    public DescriptorReader(File apkFile) throws IOException {
        ZipFile apkZip;
        ZipEntry descEntry;
        byte[] buf;
        long n;

        apkZip = new ZipFile(apkFile.toString());
        descEntry = apkZip.getEntry("assets/otlauncher.json");

        if (descEntry == null)
            throw new IOException("No descriptor");

        // Read the full descriptor string to be parsed.
        n = descEntry.getSize();
        buf = new byte[(int) n];
        if (apkZip.getInputStream(descEntry).read(buf) < n)
            throw new IOException("Unable to read descriptor");

        this.descTok = new JSONTokener(new String(buf));
        this.apkZip = apkZip;
    }
}
