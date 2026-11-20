package cl.duoc.andesstay.report;

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
class ReportLocalTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin@duoc.cl", roles = {"Admin"})
    void kpisAvailable() throws Exception {
        mockMvc.perform(get("/api/report/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ocupacionActivaPct").value(72));
    }

    @Test
    @WithMockUser(username = "operador@duoc.cl", roles = {"Operador"})
    void kpisForbiddenForOperador() throws Exception {
        mockMvc.perform(get("/api/report/kpis"))
                .andExpect(status().isForbidden());
    }
}
