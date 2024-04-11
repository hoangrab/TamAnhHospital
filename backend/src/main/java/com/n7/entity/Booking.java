package com.n7.entity;

import com.n7.constant.Gender;
import com.n7.constant.Status;
import com.n7.constant.TimeChoose;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String fullName;

    @Column(length = 20)
    private String dob;

    @Column(length = 20)
    private String phone;

    @Column(length = 30)
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 100)
    private String address;

    @Column
    private Date date;

    @Column
    private Long idHour;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 255)
    private String note;

    @Column
    private String token;

    @UpdateTimestamp
    private LocalDateTime updateAt;

    @CreationTimestamp
    private LocalDateTime createAt;

    @ManyToOne
    @JoinColumn(name = "doctor_id",referencedColumnName = "id")
    private User user;

    public Booking(String fullName, String dob, String phone, String email, Gender gender,
                   String address, Date date, Long idHour, Status status, String note, String token) {
        this.fullName = fullName;
        this.dob = dob;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.date = date;
        this.idHour = idHour;
        this.status = status;
        this.note = note;
        this.token = token;
    }
}
