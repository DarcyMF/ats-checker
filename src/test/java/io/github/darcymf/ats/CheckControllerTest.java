package io.github.darcymf.ats;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.github.darcymf.ats.api.ApiErrors;
import io.github.darcymf.ats.api.CheckController;
import io.github.darcymf.ats.check.ResumeAnalyzer;
import io.github.darcymf.ats.keywords.KeywordMatcher;
import io.github.darcymf.ats.pdf.PdfExtractor;

class CheckControllerTest {

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new CheckController(new PdfExtractor(), new ResumeAnalyzer(new KeywordMatcher())))
            .setControllerAdvice(new ApiErrors())
            .build();

    @Test
    void checksAResume() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", TestPdfs.singleColumn());

        mvc.perform(multipart("/api/check").file(file).param("jobAd", "Java and Kubernetes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pages").value(1))
                .andExpect(jsonPath("$.checks.length()").value(9))
                .andExpect(jsonPath("$.checks[0].id").value("text-layer"))
                .andExpect(jsonPath("$.checks[0].status").value("PASS"))
                .andExpect(jsonPath("$.keywords.found[0]").value("Java"))
                .andExpect(jsonPath("$.keywords.missing[0]").value("Kubernetes"))
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.startsWith("Jordan Smith")));
    }

    @Test
    void noJobAdMeansNoKeywords() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", TestPdfs.singleColumn());

        mvc.perform(multipart("/api/check").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keywords").doesNotExist());
    }

    @Test
    void rejectsFilesThatArentPdfs() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.docx", "application/octet-stream", "PK zip".getBytes());

        mvc.perform(multipart("/api/check").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("isn't a PDF")));
    }

    @Test
    void brokenPdfGivesAClearError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "%PDF-1.7 nope".getBytes());

        mvc.perform(multipart("/api/check").file(file))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void healthCheck() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }
}
