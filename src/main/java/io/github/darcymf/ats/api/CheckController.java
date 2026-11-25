package io.github.darcymf.ats.api;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.darcymf.ats.check.ResumeAnalyzer;
import io.github.darcymf.ats.check.ResumeAnalyzer.Report;
import io.github.darcymf.ats.pdf.PdfExtractor;

@RestController
@RequestMapping("/api")
public class CheckController {

    static final int MAX_JOB_AD_CHARS = 20_000;

    private final PdfExtractor extractor;
    private final ResumeAnalyzer analyzer;
    // Just a count so I know if people use it. Nothing about the resume is kept.
    private final AtomicLong checksRun = new AtomicLong();

    public CheckController(PdfExtractor extractor, ResumeAnalyzer analyzer) {
        this.extractor = extractor;
        this.analyzer = analyzer;
    }

    @PostMapping(value = "/check", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Report check(@RequestPart("file") MultipartFile file,
                        @RequestParam(value = "jobAd", required = false) String jobAd) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("Choose a PDF to check.");
        }
        byte[] bytes = file.getBytes();
        if (!isPdf(bytes)) {
            throw new BadRequestException("That file isn't a PDF. Save your resume as a PDF and try again.");
        }
        if (jobAd != null && jobAd.length() > MAX_JOB_AD_CHARS) {
            throw new BadRequestException("That job ad is too long. Paste just the ad itself.");
        }
        Report report = analyzer.analyze(extractor.extract(bytes), jobAd);
        checksRun.incrementAndGet();
        return report;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "checksRun", checksRun.get());
    }

    static boolean isPdf(byte[] bytes) {
        // PDFs start with "%PDF-", sometimes after a little junk at the start
        int limit = Math.min(bytes.length - 5, 1024);
        for (int i = 0; i <= limit; i++) {
            if (bytes[i] == '%' && bytes[i + 1] == 'P' && bytes[i + 2] == 'D' && bytes[i + 3] == 'F' && bytes[i + 4] == '-') {
                return true;
            }
        }
        return false;
    }
}
