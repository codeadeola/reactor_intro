package org.example.domain;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class Part {
    String uuid;
    Integer number;
    LocalDate date;
    Double amount;
}
