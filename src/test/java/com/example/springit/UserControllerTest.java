package com.example.springit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    public void testCreateUser() throws Exception {
        User newUser = new User();
        newUser.setFirstName("John");
        newUser.setLastName("Doe");

        ResultActions resultActions = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    public void testUpdateUser() throws Exception {
        // Assumes there is at least one user in the database
        User existingUser = getExistingUserFromDatabase();

        User updatedUser = new User();
        updatedUser.setFirstName("UpdatedFirstName");
        updatedUser.setLastName("UpdatedLastName");

        ResultActions resultActions = mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingUser.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("UpdatedFirstName")))
                .andExpect(jsonPath("$.lastName", is("UpdatedLastName")));
    }

    @Test
    public void testDeleteUser() throws Exception {
        // Assumes there is at least one user in the database
        User existingUser = getExistingUserFromDatabase();

        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isOk());
    }

    private User getExistingUserFromDatabase() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        String content = resultActions.andReturn().getResponse().getContentAsString();
        String user = content.substring(content.indexOf('{'), content.indexOf('}') + 1);
        System.out.println(user);
        return objectMapper.readValue(user, User.class);
    }
}