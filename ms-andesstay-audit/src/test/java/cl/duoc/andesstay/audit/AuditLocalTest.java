package cl.duoc.andesstay.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuditLocalTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "auditor@duoc.cl", roles = {"Auditor"})
    void timelineHasEvents() throws Exception {
        mockMvc.perform(get("/api/audit/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").exists());
    }

    @Test
    @WithMockUser(username = "cliente@duoc.cl", roles = {"Cliente"})
    void timelineForbiddenForCliente() throws Exception {
        mockMvc.perform(get("/api/audit/timeline"))
                .andExpect(status().isForbidden());
    }
}
