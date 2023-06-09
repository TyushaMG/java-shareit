package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto addBooking(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                 @RequestBody BookingDto booking) {
        return bookingService.addBooking(booking, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                   @PathVariable Long bookingId,
                                   @RequestParam boolean approved
                                   ) {
        return bookingService.updateStatus(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                 @PathVariable Long bookingId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookersBooking(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") BookingState state,
                                              @RequestParam(name = "from", defaultValue = "0") int from,
                                              @RequestParam(name = "size", defaultValue = "20") int size) {
        return bookingService.getBookersBooking(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnersBooking(@RequestHeader(name = USER_ID_HEADER) Long userId,
                                             @RequestParam(name = "state", defaultValue = "ALL") BookingState state,
                                             @RequestParam(name = "from", defaultValue = "0") int from,
                                             @RequestParam(name = "size", defaultValue = "20") int size) {
        return bookingService.getOwnersBooking(userId, state, from, size);
    }

}
