package com.example.springit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = "classpath:application-test.properties")
//@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;  // obiekt w Spring Framework, który umożliwia testowanie kontrolerów w izolowanym środowisku testowym,  bez potrzeby uruchamiania
    // aplikacji w pełni. Pozwala na symulację żądań HTTP (GET, POST, itp.) do określonych endpointów w kontrolerach i sprawdzanie odpowiedzi na te żądania.
    @Autowired
    private ObjectMapper objectMapper;  // obiekt z biblioteki Jackson, który jest używany do przekształcania obiektów Java na format JSON (serializacja i deserializacja).

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
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }
    @Test
    public void testGetSelectedUser() throws Exception {
        User existingUser = getExistingUserFromDatabase();
        mockMvc.perform(get("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingUser.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is(existingUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(existingUser.getLastName())));
    }



    @Test
    public void testUpdateUser() throws Exception {
        User existingUser = getExistingUserFromDatabase(); // musi być co najmniej jeden wpis w bazie

        User updatedUser = new User();
        updatedUser.setFirstName("Jan");
        updatedUser.setLastName("Kot");

        ResultActions resultActions = mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingUser.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("Jan")))
                .andExpect(jsonPath("$.lastName", is("Kot")));
    }

    @Test
    public void testDeleteUser() throws Exception {
        User existingUser = getExistingUserFromDatabase();
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isOk());
    }

    private User getExistingUserFromDatabase() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        String content = resultActions.andReturn().getResponse().getContentAsString();
        String user = content.substring(content.indexOf('{'), content.indexOf('}') + 1);
        System.out.println("USER" + user);
        return objectMapper.readValue(user, User.class);
    }
}