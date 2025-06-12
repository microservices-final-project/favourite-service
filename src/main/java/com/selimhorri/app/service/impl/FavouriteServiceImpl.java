package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.DuplicateEntityException;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.exception.wrapper.UserNotFoundException;
import com.selimhorri.app.helper.FavouriteMappingHelper;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.FavouriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FavouriteServiceImpl implements FavouriteService {

	private final FavouriteRepository favouriteRepository;
	private final RestTemplate restTemplate;

	@Override
	public List<FavouriteDto> findAll() {
		log.info("*** FavouriteDto List, service; fetch all favourites *");
		return this.favouriteRepository.findAll()
				.stream()
				.map(FavouriteMappingHelper::map)
				.map(f -> {
					try {
						UserDto userDto = this.restTemplate
								.getForObject(
										AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + f.getUserId(),
										UserDto.class);
						ProductDto productDto = this.restTemplate
								.getForObject(
										AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/"
												+ f.getProductId(),
										ProductDto.class);

						if (userDto == null || productDto == null) {
							log.warn("User {} or product {} not found, excluding favourite", f.getUserId(),
									f.getProductId());
							return null;
						}

						f.setUserDto(userDto);
						f.setProductDto(productDto);
						return f;
					} catch (Exception e) {
						log.warn("Error fetching details for favourite (user: {}, product: {}), excluding: {}",
								f.getUserId(), f.getProductId(), e.getMessage());
						return null;
					}
				})
				.filter(Objects::nonNull) // Filtra los elementos nulos (los que fallaron)
				.distinct()
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public FavouriteDto findById(final FavouriteId favouriteId) {
		log.info("*** FavouriteDto, service; fetch favourite by userId and productId *");
		FavouriteDto favouriteDto = this.favouriteRepository
				.findByUserIdAndProductId(favouriteId.getUserId(), favouriteId.getProductId())
				.map(FavouriteMappingHelper::map)
				.orElseThrow(() -> new FavouriteNotFoundException(
						String.format("Favourite with userId: [%s] and productId: [%s] not found in database!",
								favouriteId.getUserId(),
								favouriteId.getProductId())));

		try {
			UserDto userDto = this.restTemplate
					.getForObject(
							AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + favouriteDto.getUserId(),
							UserDto.class);
			if (userDto == null) {
				throw new FavouriteNotFoundException(
						String.format("User with id: [%s] not found!", favouriteDto.getUserId()));
			}
			favouriteDto.setUserDto(userDto);
		} catch (Exception e) {
			throw new FavouriteNotFoundException(
					String.format("Error fetching user with id: [%s]", favouriteDto.getUserId()), e);
		}

		try {
			ProductDto productDto = this.restTemplate
					.getForObject(
							AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/"
									+ favouriteDto.getProductId(),
							ProductDto.class);
			if (productDto == null) {
				throw new FavouriteNotFoundException(
						String.format("Product with id: [%s] not found!", favouriteDto.getProductId()));
			}
			favouriteDto.setProductDto(productDto);
		} catch (Exception e) {
			throw new FavouriteNotFoundException(
					String.format("Error fetching product with id: [%s]", favouriteDto.getProductId()), e);
		}

		return favouriteDto;
	}

	@Override
	public FavouriteDto save(final FavouriteDto favouriteDto) {
		// Verificar usuario
		try {
			ResponseEntity<UserDto> response = this.restTemplate.getForEntity(
					AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/" + favouriteDto.getUserId(),
					UserDto.class);

			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				throw new UserNotFoundException(
						String.format("User with id [%s] not found", favouriteDto.getUserId()));
			}
		} catch (RestClientException e) {
			throw new UserNotFoundException(
					String.format("Error verifying user with id [%s]", favouriteDto.getUserId()), e);
		}

		// Verificar producto
		try {
			ResponseEntity<ProductDto> response = this.restTemplate.getForEntity(
					AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/" + favouriteDto.getProductId(),
					ProductDto.class);

			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				throw new ProductNotFoundException(
						String.format("Product with id [%s] not found", favouriteDto.getProductId()));
			}

			boolean favouriteExists = this.favouriteRepository.existsByUserIdAndProductId(
					favouriteDto.getUserId(),
					favouriteDto.getProductId());

			if (favouriteExists) {
				throw new DuplicateEntityException(
						String.format("Favourite already exists for user [%s] and product [%s]",
								favouriteDto.getUserId(),
								favouriteDto.getProductId()));
			}

		} catch (RestClientException e) {
			throw new ProductNotFoundException(
					String.format("Error verifying product with id [%s]", favouriteDto.getProductId()), e);
		}
	
		return FavouriteMappingHelper.map(
				this.favouriteRepository.save(FavouriteMappingHelper.map(favouriteDto)));
	}

	@Override
	@Transactional
	public void deleteById(FavouriteId favouriteId) {
		// Verificar si existe
		if (!favouriteRepository.existsByUserIdAndProductId(favouriteId.getUserId(), favouriteId.getProductId())) {
			throw new FavouriteNotFoundException(
					String.format("Favourite not found with userId: %s and productId: %s",
							favouriteId.getUserId(),
							favouriteId.getProductId()));
		}

		// Si existe, eliminar
		favouriteRepository.deleteByUserIdAndProductId(favouriteId.getUserId(), favouriteId.getProductId());
	}

}
