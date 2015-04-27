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

import android.util.Log;
import com.opersys.otlauncher.tools.Am;
import com.opersys.otlauncher.tools.Pm;
import org.json.JSONException;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Console entry point of the Opersys Tools Launcher.
 */
public class OTLauncher {

    /**
     * Flag that is set to 'true' when the debug (-d) has been set on the command
     * line.
     */
    private static boolean isDebug;

    /**
     * Default extraction path for application when run from the command line.
     */
    private static File extractPath = new File("/data/local/tmp");

    // Lists the module that can be run from this APK file.
    private static void doList(Descriptor descriptor) {
        for (Descriptor.Package pkg : descriptor.getPackages())
            System.out.println(pkg.getId() + ": "+ pkg.getDescription());
    }

    private static void Log(String msg) {
        if (isDebug)
            System.out.println(msg);
        else
            Log.d("OTLauncher", msg);
    }

    /**
     * Verify the extracted application MD5 sum.
     *
     * @return true if the extracted application doesn't need to be updated
     */
    private static boolean isExtractedOk(Descriptor descriptor, String id, File pkgAppExtractPath)
            throws IOException {
        File md5sumPath;
        String arch;

        if (pkgAppExtractPath.exists()) {
            md5sumPath = new File(pkgAppExtractPath + File.separator + "MD5SUM");
            arch = Utils.getArchitecture();

            // Check the MD5 sum of the extracted application.
            if (md5sumPath.exists()) {
                FileInputStream md5stream;
                String md5sum;
                byte[] md5buf;

                md5buf = new byte[1024];
                md5stream = new FileInputStream(md5sumPath);
                md5stream.read(md5buf);
                md5sum = new String(md5buf).trim();

                return md5sum.equals(descriptor.getPackage(id).getFileForArch(arch).getMd5Sum());

            } else
                Log("No MD5SUM file found.");

        } else {
            Log("Target directory " + pkgAppExtractPath + " not found.");
        }

        return false;
    }

    /**
     * Runs an extracted application.
     */
    private static void doRun(Descriptor descriptor, String id, File pkgExtractAppPath) throws IOException {
        String cmd;
        Process runProc;
        ProcessBuilder runProcBuilder;
        Map<String, String> env;

        cmd = descriptor.getPackage(id).getCommand();
        try {
            Log("Running command: " + cmd);

            runProcBuilder = new ProcessBuilder()
                    .command(cmd.split(" "))
                    .directory(pkgExtractAppPath);

            env = runProcBuilder.environment();
            env.put("PORT", "3000");

            runProc = runProcBuilder.start();
            runProc.waitFor();

        } catch (InterruptedException e) {
            System.err.println("waitfor() interrupted.");
        }
    }

    private static long copyStream(final InputStream input, final OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int n = 0;
        long count = 0;

        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
            count += n;
        }

        return count;
    }

    /**
     * Extract a packaged application on the device.
     */
    private static void doExtract(Descriptor descriptor, String id, File pkgExtractAppPath)
            throws IOException {
        String arch = Utils.getArchitecture();
        File targetFile, md5File;
        ZipFile apkFile;
        ZipInputStream pkgZipStream;
        FileOutputStream pkgFileOutStream, md5OutStream;
        ZipEntry currentEntry, pkgEntry;
        Descriptor.PackageFile pkgFile;

        // At this point, we need to rm -rf the target directory.
        if (pkgExtractAppPath.exists())
            Utils.delete(pkgExtractAppPath.toString(), true);

        apkFile = descriptor.getAPK();
        pkgFile = descriptor.getPackage(id).getFileForArch(arch);
        pkgEntry = apkFile.getEntry(pkgFile.getFile());
        pkgZipStream = new ZipInputStream(apkFile.getInputStream(pkgEntry));

        while ((currentEntry = pkgZipStream.getNextEntry()) != null) {
            targetFile = new File(pkgExtractAppPath + File.separator + currentEntry.getName());

            Log("Extracting " + targetFile);

            if (currentEntry.isDirectory()) {

                if (!targetFile.exists()) {
                    if (!targetFile.mkdirs()) {
                        String s = String.format("Couldn't create directory %s.", targetFile.getAbsolutePath());
                        throw new IllegalStateException(s);
                    }
                }
            } else {
                final File parentTarget = new File(targetFile.getParent());

                if (!parentTarget.exists()) {
                    if (!parentTarget.mkdirs()) {
                        String s = String.format("Couldn't create directory %s.", parentTarget.toString());
                        throw new IllegalStateException(s);
                    }
                }

                pkgFileOutStream = new FileOutputStream(targetFile);
                copyStream(pkgZipStream, pkgFileOutStream);
                pkgFileOutStream.close();
            }
        }

        //
        for (Descriptor.PackageFileMode pkgFileMode : pkgFile.getPackageFileModes()) {
            targetFile = new File(pkgExtractAppPath + File.separator + pkgFileMode.getFile());

            Log("Setting file mode of " + targetFile.toString() + " to " + pkgFileMode.getMode());

            Utils.chmod(pkgFileMode.getMode(), targetFile.toString());
        }

        // Write the MD5SUM file.
        md5File = new File(pkgExtractAppPath + File.separator + "MD5SUM");
        md5OutStream = new FileOutputStream(md5File);
        md5OutStream.write(descriptor.getPackage(id).getFileForArch(arch).getMd5Sum().getBytes());

        md5OutStream.close();
        pkgZipStream.close();
    }

    private static void doStartService() throws IOException {
        if (Pm.isPackageInstalled("com.opersys.otlauncher")) {
            Log("Triggering launch of UI-side service");
            Am.startService("com.opersys.otlauncher/.OTLauncherService");
        }
        else {
            System.out.println("OTLauncherService not found (.apk not installed?)");
            System.out.println("Some features of the web application might not unavailable.");
        }
    }

    private static void doStopService() throws IOException {
        if (Pm.isPackageInstalled(("com.opersys.otlauncher"))) {
            Log("Shutting down UI-side service");
            Am.stopService("com.opersys.otlauncher/.OTLauncherService");
        }
    }

    /**
     * Executes an application that is packaged as an asset.
     */
    private static void doRunAndExtract(Descriptor descriptor, String id) throws IOException {
        File pkgExtractAppPath;
        String actualId;

        if (id == null && descriptor.getDefaultAppId() == null) {
            System.err.println("No default application to run.");
            System.exit(1);
        }

        if (id == null)
            actualId = descriptor.getDefaultAppId();
        else
            actualId = id;

        if (descriptor.getPackage(actualId) != null) {
            // Check if an extracted directory for the app ID exists.
            pkgExtractAppPath = new File(extractPath + File.separator + actualId);

            if (!isExtractedOk(descriptor, actualId, pkgExtractAppPath)) {
                Log("Extracting application " + actualId + " to " + pkgExtractAppPath);
                doExtract(descriptor, actualId, pkgExtractAppPath);
            }

            Log("Running application " + actualId + " from " + pkgExtractAppPath);
            doStartService();

            System.out.println("Running application " + actualId);
            System.out.println("Don't forget to forward port 3000 using adb:");
            System.out.println("adb forward tcp:3000 tcp:3000");

            doRun(descriptor, actualId, pkgExtractAppPath);
        }
        else {
            System.err.println("Application " + actualId + " not found");
            System.exit(1);
        }
    }

    /**
     * This is the entry point of the launcher when it's used in command line
     * mode. The program accesses the resources in the APK file as if it was
     * a plain old .zip file.
     */
    public static void main(String[] args) {
        File apkFile = null;
        String runId = null;
        boolean doList = false,
                doRun = false;
        Descriptor descriptor;
        DescriptorReader descriptorReader = null;

        if (args.length == 0) {
            System.err.println("No arguments provided...");
            System.exit(1);
        }

        // FIXME: This is not working.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Log("Bailing out");

                try {
                    doStopService();
                } catch (IOException ex) {
                    System.err.println("Error while trying to shut down the UI-side service");
                }
            }
        });

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-f"))
                apkFile = new File(args[++i]);

            if (args[i].equals("-l"))
                doList = true;

            if (args[i].equals("-d"))
                isDebug = true;

            if (args[i].equals("-r")) {
                runId = args[++i];
                doRun = true;
            }
        }

        if (doRun && doList) {
            System.err.println("List and Run conflicting.");
            System.exit(1);
        }

        if (isDebug)
            System.out.println("Debug mode enabled");

        try {
            descriptorReader = new DescriptorReader(apkFile);
            descriptor = descriptorReader.getDescriptor();

            if (apkFile != null && !apkFile.exists()) {
                System.err.println("File " + apkFile.getName() + " doesn't exists.");
                System.exit(1);
            }

            if (apkFile == null) {
                System.err.println("No .apk file specified.");
                System.exit(1);
            }

            if (descriptor != null) {
                if (doList)
                    doList(descriptor);
                else
                    doRunAndExtract(descriptor, runId);

            } else {
                System.err.println("No descriptor found");
                System.exit(1);
            }
        } catch (IOException ex) {
            System.err.println("IO error reading descriptor");
            ex.printStackTrace();

        } catch (JSONException ex) {
            System.err.println("Syntax error in descriptor");
            ex.printStackTrace();
        } finally {
            if (descriptorReader != null)
                descriptorReader.close();
        }
    }
}
