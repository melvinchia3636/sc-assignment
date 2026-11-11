package com.groupone.assignment.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void unknownPathReturnsJson404() throws Exception {
		mockMvc.perform(get("/does-not-exist").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("No endpoint found for this request"))
				.andExpect(jsonPath("$.path").value("/does-not-exist"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void unknownPathReturnsJson404EvenForHtmlClients() throws Exception {
		mockMvc.perform(get("/nope").accept(MediaType.TEXT_HTML))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void wrongMethodReturnsJson405() throws Exception {
		mockMvc.perform(post("/health").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.status").value(405))
				.andExpect(jsonPath("$.error").value("Method Not Allowed"));
	}

}
