package com.selimhorri.app.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.DuplicateEntityException;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.exception.wrapper.UserNotFoundException;
import com.selimhorri.app.service.FavouriteService;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
class FavouriteResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FavouriteService favouriteService;

    private FavouriteDto favouriteDto;
    @SuppressWarnings("unused")
    private List<FavouriteDto> favouriteDtos;
    @SuppressWarnings("unused")
    private FavouriteId favouriteId;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        UserDto userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Smartphone")
                .priceUnit(599.99)
                .quantity(10)
                .build();

        this.favouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(1)
                .likeDate(LocalDateTime.now())
                .userDto(userDto)
                .productDto(productDto)
                .build();

        this.favouriteDtos = Collections.singletonList(this.favouriteDto);
        this.favouriteId = new FavouriteId(1, 1, null);
    }

    @Test
    void testFindAll() throws Exception {
        // Configurar mock con datos completos
        UserDto userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .build();

        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Smartphone")
                .build();

        FavouriteDto completeFavouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(1)
                .userDto(userDto)
                .productDto(productDto)
                .build();

        when(favouriteService.findAll())
                .thenReturn(List.of(completeFavouriteDto));

        mockMvc.perform(get("/api/favourites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection[0].userId").value(1));
    }

    @Test
    void testFindById() throws Exception {
        // Configurar mock con datos completos
        UserDto userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .build();

        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Smartphone")
                .build();

        FavouriteDto completeFavouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(1)
                .userDto(userDto)
                .productDto(productDto)
                .build();

        when(favouriteService.findById(any(FavouriteId.class)))
                .thenReturn(completeFavouriteDto);

        mockMvc.perform(get("/api/favourites/{userId}/{productId}", 1, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        when(favouriteService.findById(any(FavouriteId.class)))
                .thenThrow(new FavouriteNotFoundException("Favourite not found"));

        mockMvc.perform(get("/api/favourites/{userId}/{productId}", 999, 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSave() throws Exception {
        when(favouriteService.save(any(FavouriteDto.class)))
                .thenReturn(favouriteDto);

        mockMvc.perform(post("/api/favourites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(favouriteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(favouriteDto.getUserId()))
                .andExpect(jsonPath("$.productId").value(favouriteDto.getProductId()));
    }

    @Test
    void testSaveValidationFailed() throws Exception {
        FavouriteDto invalidDto = FavouriteDto.builder().build(); // Invalid DTO

        mockMvc.perform(post("/api/favourites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSaveUserNotFound() throws Exception {
        when(favouriteService.save(any(FavouriteDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/favourites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(favouriteDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveProductNotFound() throws Exception {
        when(favouriteService.save(any(FavouriteDto.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/favourites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(favouriteDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveDuplicateFavourite() throws Exception {
        when(favouriteService.save(any(FavouriteDto.class)))
                .thenThrow(new DuplicateEntityException("Favourite already exists"));

        mockMvc.perform(post("/api/favourites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(favouriteDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testDeleteById() throws Exception {
        doNothing().when(favouriteService).deleteById(any(FavouriteId.class));

        mockMvc.perform(delete("/api/favourites/{userId}/{productId}", 1, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void testDeleteByIdNotFound() throws Exception {
        doThrow(new FavouriteNotFoundException("Favourite not found"))
                .when(favouriteService)
                .deleteById(any(FavouriteId.class));

        mockMvc.perform(delete("/api/favourites/{userId}/{productId}", 999, 999))
                .andExpect(status().isNotFound());
    }
}