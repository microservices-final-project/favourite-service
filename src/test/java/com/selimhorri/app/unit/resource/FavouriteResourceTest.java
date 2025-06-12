package com.selimhorri.app.unit.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.resource.FavouriteResource;
import com.selimhorri.app.service.FavouriteService;

@ExtendWith(MockitoExtension.class)
class FavouriteResourceTest {

    @Mock
    private FavouriteService favouriteService;

    @InjectMocks
    private FavouriteResource favouriteResource;

    private FavouriteDto favouriteDto;
    @SuppressWarnings("unused")
    private FavouriteId favouriteId;

    @BeforeEach
    void setUp() {
        favouriteId = new FavouriteId(1, 1, LocalDateTime.now());
        
        favouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(1)
                .likeDate(LocalDateTime.now())
                .build();
    }

    @Test
    void findAll_ShouldReturnAllFavourites() {
        // Arrange
        when(favouriteService.findAll()).thenReturn(List.of(favouriteDto));

        // Act
        ResponseEntity<DtoCollectionResponse<FavouriteDto>> response = favouriteResource.findAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getCollection().size());
        verify(favouriteService, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnFavourite() {
        // Arrange
        when(favouriteService.findById(any(FavouriteId.class))).thenReturn(favouriteDto);

        // Act
        ResponseEntity<FavouriteDto> response = favouriteResource.findById("1", "1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(favouriteDto.getUserId(), response.getBody().getUserId());
        verify(favouriteService, times(1)).findById(any(FavouriteId.class));
    }

    @Test
    void save_ShouldSaveFavourite() {
        // Arrange
        when(favouriteService.save(any(FavouriteDto.class))).thenReturn(favouriteDto);

        // Act
        ResponseEntity<FavouriteDto> response = favouriteResource.save(favouriteDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(favouriteDto.getUserId(), response.getBody().getUserId());
        verify(favouriteService, times(1)).save(any(FavouriteDto.class));
    }

    @Test
    void deleteById_ShouldDeleteFavourite() {
        // Arrange
        doNothing().when(favouriteService).deleteById(any(FavouriteId.class));

        // Act
        ResponseEntity<Boolean> response = favouriteResource.deleteById("1", "1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(favouriteService, times(1)).deleteById(any(FavouriteId.class));
    }
}