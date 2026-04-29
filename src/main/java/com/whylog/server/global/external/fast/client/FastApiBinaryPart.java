package com.whylog.server.global.external.fast.client;

import org.springframework.core.io.Resource;

public record FastApiBinaryPart(
        String partName,
        Resource resource,
        String filename,
        String contentType
) {
}
