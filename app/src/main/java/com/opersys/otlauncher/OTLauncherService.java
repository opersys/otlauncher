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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.opersys.otlauncher.service.PlatformServer;

/**
 * Date: 09/04/15
 * Time: 3:37 PM
 */
public class OTLauncherService extends Service {

    private static final String TAG = "OTLauncherService";

    private PlatformServer platformServer;

    private OTLauncherNotification notifMgr;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notifMgr = new OTLauncherNotification(this);
    }

    protected void startPlatformServer() {
        if (platformServer != null) {
            Log.w(TAG, "Platform information restlet already started");
            return;
        }

        Log.i(TAG, "Asked to start platform information restlet");

        platformServer = new PlatformServer(getPackageManager());
        platformServer.startServer();
    }

    protected void stopPlatformServer() {
        if (platformServer == null) return;

        Log.i(TAG, "Asked to stop platform information restlet");

        platformServer.stopServer();
        platformServer = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(
                notifMgr.getForegroundNotificationId(),
                notifMgr.getForegroundNotification());

        startPlatformServer();

        return super.onStartCommand(intent, flags, startId);
    }
}
