package com.condation.cms.media.processor;

/*-
 * #%L
 * CMS Media
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

import com.condation.cms.api.media.MediaFormat;
import com.condation.cms.media.CropCalculator.CropArea;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Image processor using ImageMagick CLI.
 * Uses the configured bin path, or auto-detects {@code magick} (IM 7+) / {@code convert} (IM 6).
 */
@Slf4j
public class ImageMagickProcessor implements ImageProcessor {

    private Boolean available = null;
    private String executable;

    public ImageMagickProcessor() {
        this(null);
    }

    public ImageMagickProcessor(String binPath) {
        this.executable = binPath;
    }

    @Override
    public String name() {
        return "imagemagick";
    }

    @Override
    public boolean isAvailable() {
        if (available == null) {
            available = checkAvailable();
        }
        return available;
    }

    private boolean checkAvailable() {
        if (executable != null) {
            try {
                int exit = new ProcessBuilder(executable, "--version")
                        .redirectErrorStream(true)
                        .start()
                        .waitFor();
                if (exit == 0) {
                    log.debug("ImageMagick available as '{}'", executable);
                    return true;
                }
            } catch (Exception e) {
                log.debug("ImageMagick not available at '{}': {}", executable, e.getMessage());
            }
            return false;
        }
        for (String candidate : List.of("magick", "convert")) {
            try {
                int exit = new ProcessBuilder(candidate, "--version")
                        .redirectErrorStream(true)
                        .start()
                        .waitFor();
                if (exit == 0) {
                    executable = candidate;
                    log.debug("ImageMagick available as '{}'", executable);
                    return true;
                }
            } catch (Exception e) {
                log.debug("ImageMagick candidate '{}' not available: {}", candidate, e.getMessage());
            }
        }
        return false;
    }

    @Override
    public void process(Path source, Path target, MediaFormat format, CropArea crop) throws IOException {
        // magick input.jpg [-crop WxH+X+Y +repage] -resize WxH output.png
        List<String> cmd = new ArrayList<>();
        cmd.add(executable);
        cmd.add(source.toString());

        if (crop != null) {
            cmd.add("-crop");
            cmd.add(crop.width() + "x" + crop.height() + "+" + crop.x() + "+" + crop.y());
            cmd.add("+repage");
        }

        cmd.add("-resize");
        cmd.add(format.width() + "x" + format.height());

        if (!format.compression()) {
            cmd.add("-quality");
            cmd.add("100");
        }

        cmd.add(target.toString());

        log.debug("imagemagick: {}", String.join(" ", cmd));
        try {
            int exit = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();
            if (exit != 0) {
                throw new IOException("imagemagick exited with code " + exit + " for: " + String.join(" ", cmd));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("imagemagick interrupted", e);
        }
    }
}
