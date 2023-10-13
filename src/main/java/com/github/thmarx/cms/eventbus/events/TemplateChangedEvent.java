package com.github.thmarx.cms.eventbus.events;

import com.github.thmarx.cms.eventbus.Event;
import java.nio.file.Path;

/**
 *
 * @author t.marx
 */
public record TemplateChangedEvent (Path templatePath) implements Event {}
