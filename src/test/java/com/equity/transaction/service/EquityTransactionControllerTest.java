package com.equity.transaction.service;

import com.equity.transaction.service.controller.EquityTransactionController;
import com.equity.transaction.service.service.ExcelService;
import com.equity.transaction.service.service.MongoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(controllers = EquityTransactionController.class)
public class EquityTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExcelService excelService;

    @MockBean
    private MongoService mongoService;

    @Test
    void testUploadExcelFile() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "dummy content".getBytes()
        );

        mockMvc.perform(multipart("/transactions/upload")
                        .file(mockFile))
                .andExpect(status().isOk());
    }
}
