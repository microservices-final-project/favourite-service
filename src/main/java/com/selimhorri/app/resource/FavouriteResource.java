package com.selimhorri.app.resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.FavouriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/favourites")
@Slf4j
@RequiredArgsConstructor
public class FavouriteResource {
	
	private final FavouriteService favouriteService;
	
	@GetMapping
	public ResponseEntity<DtoCollectionResponse<FavouriteDto>> findAll() {
		log.info("*** FavouriteDto List, controller; fetch all favourites *");
		return ResponseEntity.ok(new DtoCollectionResponse<>(this.favouriteService.findAll()));
	}
	
	@GetMapping("/{userId}/{productId}")
	public ResponseEntity<FavouriteDto> findById(
			@PathVariable("userId") final String userId, 
			@PathVariable("productId") final String productId) {
		log.info("*** FavouriteDto, resource; fetch favourite by id *");
		return ResponseEntity.ok(this.favouriteService.findById(
				new FavouriteId(Integer.parseInt(userId), Integer.parseInt(productId), null )));
	}
	
	@PostMapping
	public ResponseEntity<FavouriteDto> save(
			@RequestBody 
			@NotNull(message = "Input must not be NULL") 
			@Valid final FavouriteDto favouriteDto) {
		log.info("*** FavouriteDto, resource; save favourite *");
		return ResponseEntity.ok(this.favouriteService.save(favouriteDto));
	}
	
	
	@DeleteMapping("/{userId}/{productId}")
	public ResponseEntity<Boolean> deleteById(
			@PathVariable("userId") final String userId, 
			@PathVariable("productId") final String productId) {
		log.info("*** Boolean, resource; delete favourite by id *");
		this.favouriteService.deleteById(new FavouriteId(Integer.parseInt(userId), Integer.parseInt(productId), null));
		return ResponseEntity.ok(true);
	}	
	
}










