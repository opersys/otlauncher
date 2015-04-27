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

package com.opersys.otlauncher.service;

import android.content.pm.PackageManager;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class PlatformApp extends Application {

    protected Context appCtx;

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router();

        router.setContext(appCtx);

        // Routes
        router.attach("/icon/", IconResource.class);
        router.attach("/icon/{app}", IconResource.class);
        router.attachDefault(DefaultResource.class);

        return router;
    }

    public PlatformApp(PackageManager pm) {
        appCtx = new Context();
        appCtx.getAttributes().put("pm", pm);
    }
}
