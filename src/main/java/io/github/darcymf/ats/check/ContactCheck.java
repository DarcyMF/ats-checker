package io.github.darcymf.ats.check;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.darcymf.ats.pdf.ResumeDocument;

/** The ATS fills in the candidate's contact fields from the text, so they need to be readable. */
public class ContactCheck implements Check {

    static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    static final Pattern PHONE_LIKE = Pattern.compile("\\+?\\(?\\d[\\d ()\\-.]{6,}\\d");
    static final Pattern YEAR_RANGE = Pattern.compile("(19|20)\\d{2}\\s*[-.]\\s*(19|20)\\d{2}");
    static final Pattern LINKEDIN = Pattern.compile("linkedin\\.com/in/[A-Za-z0-9_-]+", Pattern.CASE_INSENSITIVE);

    @Override
    public CheckResult run(ResumeDocument doc) {
        String text = doc.text();
        boolean email = EMAIL.matcher(text).find();
        boolean phone = findPhone(text);
        boolean linkedin = LINKEDIN.matcher(text).find();

        List<String> details = new ArrayList<>();
        details.add("Email: " + (email ? "found" : "not found"));
        details.add("Phone: " + (phone ? "found" : "not found"));
        details.add("LinkedIn: " + (linkedin ? "found" : "not found"));

        String title = "Contact details";
        if (!email && !phone) {
            return CheckResult.fail("contact", title,
                    "No email or phone number could be read, so a recruiter would have no way to contact you.",
                    details,
                    "Type your email and phone number as plain text at the top. Don't put them in an image, icon or the page header.");
        }
        if (!email || !phone) {
            return CheckResult.warn("contact", title,
                    "Found your " + (email ? "email" : "phone number") + " but not your " + (email ? "phone number" : "email") + ".",
                    details,
                    "Add both as plain text near your name.");
        }
        return new CheckResult("contact", title, CheckResult.Status.PASS,
                "Email and phone number both read correctly.", details, null);
    }

    public static boolean findPhone(String text) {
        Matcher m = PHONE_LIKE.matcher(text);
        while (m.find()) {
            String candidate = m.group();
            long digits = candidate.chars().filter(Character::isDigit).count();
            if (digits >= 8 && digits <= 15 && !YEAR_RANGE.matcher(candidate).find()) {
                return true;
            }
        }
        return false;
    }
}
