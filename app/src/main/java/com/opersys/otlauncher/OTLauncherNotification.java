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

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

/**
 * Date: 09/04/15
 * Time: 4:07 PM
 */
public class OTLauncherNotification {

    private static final int SERVICE_NOTIFICATION_ID = 1;

    protected NotificationCompat.Builder notifBuilder;

    protected NotificationManager notifService;

    public OTLauncherNotification(OTLauncherService nodeService) {
        notifBuilder = new NotificationCompat.Builder(nodeService)
                .setContentText("Process Explorer")
                .setOngoing(true);
    }

    public int getForegroundNotificationId() {
        return SERVICE_NOTIFICATION_ID;
    }

    // Returns the notification object the service can use to call startForeground.
    public Notification getForegroundNotification() {
        notifBuilder.setContentText("Stopped. Ready to start.");
        return notifBuilder.build();
    }
}
