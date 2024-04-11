package com.n7.model;

import com.n7.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingModel {
    private Long id;
    private String name;
    private Gender gender;
    private String dob;
    private String email;
    private String phone;
    private String date;
    private Long idHour;
    private String status;
    private String note;
    private String nameDoctor;
    private String major;
}
