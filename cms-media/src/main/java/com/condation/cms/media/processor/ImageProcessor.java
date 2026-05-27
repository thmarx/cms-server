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

/**
 * Processes (scales + optionally crops) a source image into a target file.
 *
 * Implementations exist for ImageIO (always available), libvips, and ImageMagick.
 * New image format support should be added to each implementation's format-mapping.
 */
public interface ImageProcessor {

    /** Unique name used for configuration and logging. */
    String name();

    /**
     * Returns true if this processor's underlying tool is available on the current system.
     * The result should be cached after the first check.
     */
    boolean isAvailable();

    /**
     * Processes a source image and writes the result to target.
     *
     * @param source     source image path
     * @param target     output file path (will be created or overwritten)
     * @param format     target format including dimensions and output format
     * @param crop       optional pre-scale crop region, null means no crop
     * @throws IOException on I/O or processing errors
     */
    void process(Path source, Path target, MediaFormat format, CropArea crop) throws IOException;
}
