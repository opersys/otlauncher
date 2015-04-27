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

import android.os.Build;

import java.io.File;
import java.io.IOException;

/**
 * Date: 09/04/15
 * Time: 1:06 PM
 */
public class Utils {

    /**
     * Returns the architecture of the device on which we are running.
     *
     * @return arm | ia32 | arm64 | amd64 | unknown
     */
    public static String getArchitecture() {
        if (Build.CPU_ABI.contains("arm"))
            return "arm";
        else if (Build.CPU_ABI.contains("x86"))
            return "ia32";
        else
            return null;
    }

    /**
     * Deletes a file or directory, allowing recursive directory deletion. This is an
     * improved version of File.delete() method.
     */
    public static boolean delete(String filePath, boolean recursive) {
        File file = new File(filePath);

        if (!file.exists()) {
            return true;
        }

        if (!recursive || !file.isDirectory())
            return file.delete();

        String[] list = file.list();
        for (int i = 0; i < list.length; i++) {
            if (!delete(filePath + File.separator + list[i], true))
                return false;
        }

        return file.delete();
    }

    public static void chmod(String mode, String target) throws IOException {
        Process chmodProcess;
        String[] chmodArr = { null, mode, target }, paths;
        String pathEnv;

        pathEnv = System.getenv("PATH");
        paths = pathEnv.split(":");

        for (String path : paths) {
            File chmodFile = new File(path + "/chmod");

            if (chmodFile.exists() && chmodFile.canExecute()) {
                chmodArr[0] = chmodFile.getAbsolutePath();
                break;
            }
        }

        if (chmodArr[0] == null)
            throw new IOException("Could not find an executable 'chmod' binary");

        try {
            chmodProcess = Runtime.getRuntime().exec(chmodArr);
            chmodProcess.waitFor();

        } catch (InterruptedException e) {
            System.err.println("waitfor() interrupted.");
        }
    }

}
