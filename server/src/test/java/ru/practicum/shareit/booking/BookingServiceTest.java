package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingServiceTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private BookingServiceImpl bookingService;

    private final UserDto owner = UserDto
            .builder()
            .id(1L)
            .name("donald")
            .email("duck@mail.ru")
            .build();

    private final UserDto booker = UserDto
            .builder()
            .id(2L)
            .name("Monterey Jack")
            .email("ilovecheese@mail.ru")
            .build();

    private final ItemDto item = ItemDto.builder()
            .id(1L)
            .name("Cheese")
            .description("I'm not real hungry")
            .available(true)
            .build();

    private final ItemDto itemNotAvailable = ItemDto.builder()
            .id(2L)
            .name("Mouse")
            .description("You are what ya eat, ya know.")
            .available(false)
            .build();

    private LocalDateTime testTime;

    private BookingDto bookingDto;

    @BeforeEach
    void setBooking() {
        testTime = LocalDateTime.now();
        bookingDto = BookingDto.builder()
                .id(1L)
                .itemId(item.getId())
                .start(testTime.plusSeconds(1))
                .end(testTime.plusSeconds(10))
                .build();
        userService.create(owner);
        userService.create(booker);
        itemService.addItem(item, owner.getId());
        itemService.addItem(itemNotAvailable, owner.getId());
    }

    @Test
    @DisplayName("Тест добавить бронирование")
    void testAddBooking() {
        BookingDto booking = bookingService.addBooking(bookingDto, booker.getId());

        assertEquals(bookingDto.getId(), booking.getId(),
                 "ID expected to be " + bookingDto.getId() + ", but was " + booking.getId());
        assertEquals(bookingDto.getItemId(), booking.getItem().getId(),
                "Item Id expected to be " + bookingDto.getItemId() + ", but was " + booking.getItemId());
        assertEquals(bookingDto.getStart(), booking.getStart(),
                "Start date expected to be " + bookingDto.getStart() + ", but was " + booking.getStart());
        assertEquals(bookingDto.getEnd(), booking.getEnd(),
                "End date expected to be " + bookingDto.getEnd() + ", but was " + booking.getEnd());
        assertEquals(BookingStatus.WAITING, booking.getStatus(),
                "Booking Status expected to be " + BookingStatus.WAITING + ", but was " + booking.getStatus());
    }

    @Test
    @DisplayName("Тест бронирование несуществующей вещи")
    void testAddBookingWrongItem() {
        bookingDto.setItemId(99L);
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.addBooking(bookingDto, booker.getId()));
    }

    @Test
    @DisplayName("Тест попытка бронирования своей вещи; несуществующий пользователь")
    void testAddBookingWrongUser() {
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.addBooking(bookingDto, owner.getId()));
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.addBooking(bookingDto, 99L));
    }

    @Test
    @DisplayName("Тест попытка создания бронирования с невалидной датой начала/конца")
    void testAddBookingWrongData() {
        bookingDto.setStart(testTime.plusSeconds(10));
        assertThrows(BookingException.class,
                () -> bookingService.addBooking(bookingDto, booker.getId()));
        bookingDto.setEnd(testTime.minusMinutes(10L));
        assertThrows(BookingException.class,
                () -> bookingService.addBooking(bookingDto, booker.getId()));
    }

    @Test
    @DisplayName("Тест попытка забронировать недоступную вещь")
    void testAddBookingUnavailable() {
        BookingDto unavailableBooking = BookingDto.builder()
                .id(2L)
                .itemId(itemNotAvailable.getId())
                .start(testTime.plusSeconds(1))
                .end(testTime.plusSeconds(10))
                .build();
        assertThrows(BookingException.class,
                () -> bookingService.addBooking(unavailableBooking, booker.getId()));
    }

    @Test
    @DisplayName("Тест поменять статус бронирования APPROVED")
    void testUpdateStatusApproved() {
        BookingDto booking = bookingService.addBooking(bookingDto, booker.getId());
        BookingDto bookingApproved = bookingService.updateStatus(booking.getId(), owner.getId(), true);
        assertEquals(BookingStatus.APPROVED, bookingApproved.getStatus(),
                "Booking Status expected to be " + BookingStatus.APPROVED
                        + ", but was " + bookingApproved.getStatus());
    }

    @Test
    @DisplayName("Тест поменять статус бронирования REJECTED")
    void testUpdateStatusRejected() {
        BookingDto booking = bookingService.addBooking(bookingDto, booker.getId());
        BookingDto bookingApproved = bookingService.updateStatus(booking.getId(), owner.getId(), false);
        assertEquals(BookingStatus.REJECTED, bookingApproved.getStatus(),
                "Booking Status expected to be " + BookingStatus.REJECTED
                        + ", but was " + bookingApproved.getStatus());
    }

    @Test
    @DisplayName("Тест попытка изменения статуса несуществующего бронирования")
    void testUpdateStatusWrongBooking() {
        bookingService.addBooking(bookingDto, booker.getId());
        assertThrows(BookingException.class,
                () -> bookingService.updateStatus(99L, owner.getId(), true));
    }

    @Test
    @DisplayName("Тест попытка изменения статуса не владельцом")
    void testUpdateStatusWrongUser() {
        bookingService.addBooking(bookingDto, booker.getId());
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.updateStatus(bookingDto.getId(), booker.getId(), true));
    }

    @Test
    @DisplayName("Тест получение бронирования по ID")
    void testGetBooking() {
        BookingDto booking = bookingService.addBooking(bookingDto, booker.getId());
        BookingDto bookingGetByBooker = bookingService.getBooking(booking.getId(), booker.getId());
        BookingDto bookingGetByOwner = bookingService.getBooking(booking.getId(), owner.getId());

        assertEquals(bookingGetByOwner.getId(), bookingGetByBooker.getId());
        assertEquals(bookingGetByOwner.getItem().getName(), bookingGetByBooker.getItem().getName());
        assertEquals(bookingDto.getId(), bookingGetByBooker.getId());
    }

    @Test
    @DisplayName("Тест попытка получения бронирования по ID невалидный пользователь")
    void testGetBookingByIdWrongUser() {
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBooking(bookingDto.getId(), 99L));
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBooking(99L, owner.getId()));

        UserDto someUser = UserDto
                .builder()
                .id(3L)
                .name("someUser")
                .email("someUser@mail.ru")
                .build();

        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBooking(bookingDto.getId(), someUser.getId()));
    }

    @Test
    @DisplayName("Тест получения всех бронирований, которые совершил пользователь")
    void testGetAllBookersBooking() {
        BookingDto currentBooking = BookingDto.builder()
                .id(2L)
                .itemId(item.getId())
                .start(testTime.minusMinutes(10))
                .end(testTime.plusMinutes(10))
                .build();

        BookingDto pastBooking = BookingDto.builder()
                .id(3L)
                .itemId(item.getId())
                .start(testTime.minusMinutes(100))
                .end(testTime.minusMinutes(50))
                .build();

        BookingDto futureBooking = BookingDto.builder()
                .id(4L)
                .itemId(item.getId())
                .start(testTime.plusMinutes(10))
                .end(testTime.plusMinutes(50))
                .build();

        BookingDto rejectedBooking = BookingDto.builder()
                .id(5L)
                .itemId(item.getId())
                .start(testTime.plusMinutes(10))
                .end(testTime.plusMinutes(50))
                .build();

        bookingService.addBooking(bookingDto, booker.getId());
        bookingService.addBooking(currentBooking, booker.getId());
        bookingService.addBooking(pastBooking, booker.getId());
        bookingService.addBooking(futureBooking, booker.getId());
        bookingService.addBooking(rejectedBooking, booker.getId());

        bookingService.updateStatus(rejectedBooking.getId(), owner.getId(), false);

        List<BookingDto> bookings = bookingService.getBookersBooking(booker.getId(),
                BookingState.ALL, 0, 20);
        assertEquals(5, bookings.size());

        List<BookingDto> currentBookings = bookingService.getBookersBooking(booker.getId(),
                BookingState.CURRENT, 0, 20);
        assertEquals(1, currentBookings.size());

        List<BookingDto> pastBookings = bookingService.getBookersBooking(booker.getId(),
                BookingState.PAST, 0, 20);
        assertEquals(1, pastBookings.size());

        List<BookingDto> futureBookings = bookingService.getBookersBooking(booker.getId(),
                BookingState.FUTURE, 0, 20);
        assertEquals(3, futureBookings.size());

        List<BookingDto> rejectedBokings = bookingService.getBookersBooking(booker.getId(),
                BookingState.REJECTED, 0, 20);
        assertEquals(1, rejectedBokings.size());
    }

    @Test
    void testGetBookersBookingWrongUser() {
        bookingService.addBooking(bookingDto, booker.getId());
        assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getBookersBooking(99L, BookingState.ALL, 0, 20));
    }

    @Test
    @DisplayName("Тест получения всех бронирований владельца")
    void testGetAllOwnerBooking() {
        BookingDto currentBooking = BookingDto.builder()
                .id(2L)
                .itemId(item.getId())
                .start(testTime.minusMinutes(10))
                .end(testTime.plusMinutes(10))
                .build();

        BookingDto pastBooking = BookingDto.builder()
                .id(3L)
                .itemId(item.getId())
                .start(testTime.minusMinutes(100))
                .end(testTime.minusMinutes(50))
                .build();

        BookingDto futureBooking = BookingDto.builder()
                .id(4L)
                .itemId(item.getId())
                .start(testTime.plusMinutes(10))
                .end(testTime.plusMinutes(50))
                .build();

        BookingDto rejectedBooking = BookingDto.builder()
                .id(5L)
                .itemId(item.getId())
                .start(testTime.plusMinutes(10))
                .end(testTime.plusMinutes(50))
                .build();

        bookingService.addBooking(bookingDto, booker.getId());
        bookingService.addBooking(currentBooking, booker.getId());
        bookingService.addBooking(pastBooking, booker.getId());
        bookingService.addBooking(futureBooking, booker.getId());
        bookingService.addBooking(rejectedBooking, booker.getId());

        bookingService.updateStatus(rejectedBooking.getId(), owner.getId(), false);

        List<BookingDto> bookings = bookingService.getOwnersBooking(owner.getId(),
                BookingState.ALL, 0, 20);
        assertEquals(5, bookings.size());

        List<BookingDto> currentBookings = bookingService.getOwnersBooking(owner.getId(),
                BookingState.CURRENT, 0, 20);
        assertEquals(1, currentBookings.size());

        List<BookingDto> pastBookings = bookingService.getOwnersBooking(owner.getId(),
                BookingState.PAST, 0, 20);
        assertEquals(1, pastBookings.size());

        List<BookingDto> futureBookings = bookingService.getOwnersBooking(owner.getId(),
                BookingState.FUTURE, 0, 20);
        assertEquals(3, futureBookings.size());

        List<BookingDto> rejectedBokings = bookingService.getOwnersBooking(owner.getId(),
                BookingState.REJECTED, 0, 20);
        assertEquals(1, rejectedBokings.size());
    }

}
