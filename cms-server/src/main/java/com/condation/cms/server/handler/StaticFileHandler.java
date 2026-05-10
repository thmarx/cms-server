package com.condation.cms.server.handler;

/*-
 * #%L
 * CMS Server
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.api.utils.RequestUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

@Slf4j
public class StaticFileHandler extends AbstractHandler {

    private final List<Path> bases;

    public StaticFileHandler(List<Path> bases) {
        this.bases = bases;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {

        String relativePath = getRelativePath(request);

        for (Path base : bases) {
            Path requested = base.resolve(relativePath).normalize();

            if (!Files.exists(requested)
                    || !Files.isRegularFile(requested)
                    || !PathUtil.isChild(base, requested)) {
                continue; // 👉 nächster Base-Pfad
            }

            return serveFile(request, response, callback, requested);
        }

        return false; // 👉 nichts gefunden → nächster Handler
    }

    private boolean serveFile(Request request, Response response, Callback callback, Path requested) {

        try {
            long size = Files.size(requested);

            response.setStatus(200);
            response.getHeaders().put("Content-Type", guessContentType(requested));
            response.getHeaders().put("Content-Length", String.valueOf(size));

            // HEAD support
            if ("HEAD".equalsIgnoreCase(request.getMethod())) {
                callback.succeeded();
                return true;
            }

            var in = Files.newInputStream(requested);

            Content.copy(Content.Source.from(in), response, new Callback() {
                @Override
                public void succeeded() {
                    try { in.close(); } catch (IOException ignore) {}
                    callback.succeeded();
                }

                @Override
                public void failed(Throwable x) {
                    try { in.close(); } catch (IOException ignore) {}
                    callback.failed(x);
                }
            });

            return true;

        } catch (IOException e) {
            log.error("Error serving static file {}", requested, e);
            callback.failed(e);
            return true;
        }
    }

    private String getRelativePath(Request request) {
        String path = request.getHttpURI().getPath();
        String contextPath = RequestUtil.getContextPath(request);

        if (!contextPath.endsWith("/")) {
            contextPath += "/";
        }

        return path.replaceFirst("^" + contextPath, "");
    }

    private String guessContentType(Path path) {
        try {
            String type = Files.probeContentType(path);
            if (type != null) return type;

            String p = path.toString();
            if (p.endsWith(".js")) return "application/javascript";
            if (p.endsWith(".css")) return "text/css";
            if (p.endsWith(".html")) return "text/html";

            return "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
