package com.example.springit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
public class UserControllerTest {
    // EntityController -> EntityService -> EntityRepository<Entity> -> DB
    // Test coverage -> functionality testing -> from url to database?!
    // mapping and testing based on url address spaces
    @Autowired
    private MockMvc mockMvc; // mock fot it testing that creates isolated enviroment
    @Autowired
    private ObjectMapper objectMapper; // from Jackson lib for serialization and deserialization

    @Test
    public void testCreateUser() throws Exception {
        User newUser = new User();
        newUser.setFirstName("Alan");
        newUser.setLastName("Żółw");

        ResultActions resultActions = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)));
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(newUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(newUser.getLastName())));
    }
    @Test
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }
    @Test
    public void testGetUserById() throws Exception {
        User existingUser = getExistingUserFromDb();
        mockMvc.perform(get("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingUser.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is(existingUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(existingUser.getLastName())));
    }
    @Test
    public void testUpdateUser() throws Exception {
        User existingUser = getExistingUserFromDb();
        User updatedUser = new User();
        updatedUser.setFirstName("Anna");
        updatedUser.setLastName("Pies");

        ResultActions resultActions = mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)));
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingUser.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is(updatedUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(updatedUser.getLastName())));
    }
    @Test
    public void testDeleteUser() throws Exception {
        User existingUser = getExistingUserFromDb();
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isOk());
    }

    private User getExistingUserFromDb() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
        String content = resultActions.andReturn().getResponse().getContentAsString();
        String user = content.substring(content.indexOf('{'), content.indexOf('}') + 1);
        System.out.println("CONTENT: " + content);
        System.out.println("USER: " + user);
        return objectMapper.readValue(user, User.class);
    }


}