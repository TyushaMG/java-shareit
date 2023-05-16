package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingOutputDto {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingItemDto item;
    private BookerDto booker;
    private String status;
}
