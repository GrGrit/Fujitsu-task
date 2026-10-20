package com.fujitsu.task.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Cities where delivery is available, each mapped to the weather station whose data is used for that city.
 */
@Getter
@RequiredArgsConstructor
public enum City {

    TALLINN("Tallinn-Harku"),
    TARTU("Tartu-Tõravere"),
    PARNU("Pärnu");

    private final String stationName;
}
