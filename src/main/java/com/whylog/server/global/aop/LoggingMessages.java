package com.whylog.server.global.aop;

public enum LoggingMessages {

    API_START("[API START] requestId={} member={} api={} method={} uri={} startedAt={}"),
    API_END("[API END] requestId={} member={} api={} method={} uri={} endedAt={} durationMs={} status={} outcome={}"),
    METHOD_START("[METHOD START] requestId={} methodId={} member={} method={} startedAt={}"),
    METHOD_END("[METHOD END] requestId={} methodId={} member={} method={} endedAt={} durationMs={} outcome={}");

    private final String template;

    LoggingMessages(String template) {
        this.template = template;
    }

    public String template() {
        return template;
    }
}
