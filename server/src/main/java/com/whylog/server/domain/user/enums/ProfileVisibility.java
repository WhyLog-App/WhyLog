package com.whylog.server.domain.user.enums;

public enum ProfileVisibility {
    PUBLIC,
    PRIVATE;

    public boolean isPublic() {
        return this == PUBLIC;
    }
}
