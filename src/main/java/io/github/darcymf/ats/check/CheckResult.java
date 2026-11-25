package io.github.darcymf.ats.check;

import java.util.List;

/**
 * @param details short examples from the resume, e.g. the words that were broken
 * @param fix     what to change, or null if it passed
 */
public record CheckResult(String id, String title, Status status, String message, List<String> details, String fix) {

    public enum Status { PASS, WARN, FAIL }

    public static CheckResult pass(String id, String title, String message) {
        return new CheckResult(id, title, Status.PASS, message, List.of(), null);
    }

    public static CheckResult warn(String id, String title, String message, List<String> details, String fix) {
        return new CheckResult(id, title, Status.WARN, message, details, fix);
    }

    public static CheckResult fail(String id, String title, String message, List<String> details, String fix) {
        return new CheckResult(id, title, Status.FAIL, message, details, fix);
    }
}
