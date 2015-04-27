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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Simple data structure to hold the information provided by the "otlauncher.json"
 * files that describes what is stored in the .apk package.
 */
public class Descriptor {

    private final String defaultAppId;

    public class PackageFileMode {

        private String mode;
        private String file;

        public String getMode() {
            return this.mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getFile() {
            return this.file;
        }

        public PackageFileMode() {}
    }

    public class PackageFile {

        private String file;
        private String md5sum;
        private String arch;
        private List<PackageFileMode> fileModes;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getMd5Sum() {
            return md5sum;
        }

        public void setMd5sum(String md5sum) {
            this.md5sum = md5sum;
        }

        public String getArchitecture() {
            return arch;
        }

        public void setArchitecture(String arch) {
            this.arch = arch;
        }

        public void addPackageFileMode(PackageFileMode fileMode) {
            fileModes.add(fileMode);
        }

        public PackageFileMode[] getPackageFileModes() {
            return fileModes.toArray(new PackageFileMode[fileModes.size()]);
        }

        public PackageFile() {
            fileModes = new ArrayList<PackageFileMode>();
        }
    }

    public class Package {

        private String id;
        private String desc;
        private String cmd;

        private HashMap<String, PackageFile> packageFiles;

        public String getDescription() {
            return desc;
        }

        public void setDescription(String desc) {
            this.desc = desc;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setCommand(String cmd) {
            this.cmd = cmd;
        }

        public String getCommand() {
            return this.cmd;
        }

        public PackageFile getFileForArch(String arch) {
            return packageFiles.get(arch);
        }

        public void addPackageFile(PackageFile pkgFile) {
            packageFiles.put(pkgFile.getArchitecture(), pkgFile);
        }

        public Package() {
            this.packageFiles = new HashMap<String, PackageFile>();
        }
    }

    private HashMap<String, Package> packages;

    private ZipFile apkZip;

    public String getDefaultAppId() {
        return this.defaultAppId;
    }

    //public Descriptor() {
    //    this.packages = new HashMap<>();
    //}

    public Descriptor(ZipFile apkZip, String defaultAppId) {
        this.packages = new HashMap<String, Package>();
        this.apkZip = apkZip;
        this.defaultAppId = defaultAppId;
    }

    public ZipFile getAPK() {
        return this.apkZip;
    }

    public Package[] getPackages() {
        return packages.values().toArray(new Package[packages.size()]);
    }

    public Package addPackage(Package pkg) {
        packages.put(pkg.getId(), pkg);
        return pkg;
    }

    public Package getPackage(String id) {
        return packages.get(id);
    }
}
