package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;
	public static final String USER_ID_HEADER = "X-Sharer-User-Id";

	@PostMapping
	public ResponseEntity<Object> bookItem(@RequestHeader(name = USER_ID_HEADER) Long userId,
										   @RequestBody @Valid BookItemRequestDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.bookItem(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateStatus(@RequestHeader(name = USER_ID_HEADER) Long userId,
											   @PathVariable Long bookingId,
											   @RequestParam boolean approved) {
		return bookingClient.updateStatus(bookingId, userId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader(name = USER_ID_HEADER) Long userId,
											 @PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader(name = USER_ID_HEADER) Long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnersBookings(@RequestHeader(name = USER_ID_HEADER) Long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, owned by userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getOwnersBookings(userId, state, from, size);
	}
}
