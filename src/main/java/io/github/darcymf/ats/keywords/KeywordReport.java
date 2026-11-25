package io.github.darcymf.ats.keywords;

import java.util.List;

/**
 * @param found   skills from the ad that the ATS can read in the resume
 * @param broken  skills that are in the resume but get missed because of joined letters
 * @param missing skills from the ad that aren't in the resume at all
 * @param score   found as a percentage of every skill the ad mentions
 */
public record KeywordReport(List<String> found, List<String> broken, List<String> missing, int score) {
}
