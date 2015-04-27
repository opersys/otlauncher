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

package com.opersys.otlauncher.tools;

import java.io.IOException;
import java.io.InputStream;

/**
 * On the Android console, we don't have access to the PackageManager service but
 * we can still call the 'Pm' command to at least get some informations.
 *
 * This class is limited to the subset of command needed by the rest of the program.
 */
public class Pm {

    private static String getPmOutput(String pmCommand) throws IOException {
        int n;
        byte[] buf = new byte[1024];
        String[] pmc;
        StringBuffer pmOutput;
        ProcessBuilder pmBuilder;
        InputStream pmIn;
        Process pm;

        pmc = ("pm " + pmCommand).split(" ");
        pmBuilder = new ProcessBuilder()
                .command(pmc);
        pm = pmBuilder.start();

        try {
            pmIn = pm.getInputStream();
            pmOutput = new StringBuffer();

            while ((n = pmIn.read(buf)) != -1) {
                pmOutput.append(new String(buf, 0, n));
            }
        } finally {
            pm.destroy();
        }

        return pmOutput.toString();
    }

    public static boolean isPackageInstalled(String pkgName) throws IOException {
        String[] pkgList;

        pkgList = getPackages();
        for (String pkg : pkgList)
            if (pkgName.equals(pkg))
                return true;

        return false;
    }

    public static String[] getPackages() throws IOException {
        String pmOut;
        String[] pmList, pmListOut;

        pmOut = getPmOutput("list packages");
        pmList = pmOut.split("\n");
        pmListOut = new String[pmList.length];

        for (int i = 0; i < pmList.length; i++)
            pmListOut[i] = pmList[i].substring(8);

        return pmListOut;
    }
}
