package com.n7.service.impl;

import com.n7.constant.Status;
import com.n7.dto.BookingDTO;
import com.n7.entity.*;
import com.n7.exception.ResourceAlreadyExitsException;
import com.n7.exception.ResourceNotFoundException;
import com.n7.model.BookingModel;
import com.n7.model.ScheduleModel;
import com.n7.repository.*;
import com.n7.service.IBookingService;
import com.n7.utils.ConvertTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {
    private final BookingRepo bookingRepo;
    private final UserRepo userRepo;
    private final ScheduleRepo scheduleRepo;
    private final ScheduleUserRepo scheduleUserRepo;
    private final ScheduleService scheduleService;
    private final HourRepo hourRepo;

    public Optional<Booking> findById(Long id) {
        return bookingRepo.findById(id);
    }

    public List<BookingModel> findByParam(Status status, Long id, String start, String end) {
        Date en = null, st = null;
        if(start!=null) st = ConvertTimeUtils.stringToDate(start);
        if(end!=null) en = ConvertTimeUtils.stringToDate(end);
        return bookingRepo.findByCustom(id,status,st,en).stream().map(this::convertEntityToModel).collect(Collectors.toList());
    }

    @Transactional
    public String creatBooking(BookingDTO bookingDTO) {
        Optional<User> user = userRepo.findById(bookingDTO.getIdUser());
        if(user.isEmpty()) {
            throw new ResourceNotFoundException("Khong tim thay id Doctor");
        }
        user.get().getListSchedule().stream().forEach( e -> {
            if(e.getSchedule().equals(bookingDTO.getDate()) && e.getSchedule().getHour().getId() == bookingDTO.getIdHour()) {
                throw new ResourceAlreadyExitsException("Lịch đặt đã bị trùng!!!");
            }
        });
        // Save Booking
        Booking booking = convertDtoToEntity(bookingDTO,Status.PENDING);
        booking.setUser(user.get());
        bookingRepo.save(booking);

        // Save Schedule of Doctor
        Optional<Hour> hour = hourRepo.findById(booking.getIdHour());
        if(hour.isEmpty()) {
            throw new ResourceNotFoundException("Lich dat với id không hợp lệ");
        }
        Schedule schedule = new Schedule(ConvertTimeUtils.stringToDate(bookingDTO.getDate()),hour.get());
        scheduleRepo.save(schedule);
        ScheduleUser scheduleUser = new ScheduleUser();
        scheduleUser.setUser(user.get());
        scheduleUser.setSchedule(schedule);
        scheduleUserRepo.save(scheduleUser);
        String s = "<h1>Thư xác nhận lịch khám</h1>\n" +
                "    <p>Tên người khám: "+bookingDTO.getName()+"</p>\n" +
                "    <p>Ngày sinh: "+bookingDTO.getDob()+"</p>\n" +
                "    <p>Giới tính: "+bookingDTO.getGender()+"</p>\n" +
                "    <p>Ngày khám: "+bookingDTO.getDate()+"</p>\n" +
                "    <p>Thòi gian khám: "+hour.get().getName()+"</p>\n" +
                "    <p>Tên bác sĩ khám: "+user.get().getFullname()+"</p>\n" +
                "    <p>Chuyên ngành: "+user.get().getMajor().getName()+"</p>\n" +
                "    <h4>Hãy nhấn xác nhận để lịch khám được hoàn thành đăng ký.</h4>\n" +
                "    <a href='http://localhost:8080/api/v1/confirm/"+booking.getId()+"?token="+booking.getToken()+"'><h2>Xác nhận</h2></a>";
        return s;
    }

    @Transactional
    public void updateBooking(Long id,Status status) {
        Optional<Booking> booking = bookingRepo.findById(id);
        if(booking.isPresent()) {
            booking.get().setStatus(status);
            if(status.equals(Status.CONFIRMING)) booking.get().setToken(null);
            if(status.equals(Status.SUCCESS) || status.equals(Status.FAILURE)){
                deleteScheduleOfDoctor(booking.get());
            }
            bookingRepo.save(booking.get());
        }
    }

    public void deleteBooking(Long id) {
        Optional<Booking> booking = bookingRepo.findById(id);
        booking.get().setStatus(Status.FAILURE);
        deleteScheduleOfDoctor(booking.get());
        bookingRepo.save(booking.get());
    }

    public Booking convertDtoToEntity(BookingDTO bookingDTO, Status status) {
        return new Booking(bookingDTO.getName(),bookingDTO.getDob(),bookingDTO.getPhone(),bookingDTO.getEmail(),bookingDTO.getGender(),
                bookingDTO.getAddress(),ConvertTimeUtils.stringToDate(bookingDTO.getDate()),bookingDTO.getIdHour(),status,bookingDTO.getNote(),
                UUID.randomUUID().toString());
    }

    @Transactional
    public void deleteScheduleOfDoctor(Booking booking) {
        ScheduleModel scheduleModel = scheduleService.getAllBy(booking.getDate(),booking.getIdHour());
        Optional<ScheduleUser> scheduleUser = scheduleUserRepo.
                findByUser_IdAndSchedule_Id(booking.getUser().getId(),scheduleModel.getId());
        scheduleUserRepo.deleteById(scheduleUser.get().getId());
        scheduleRepo.deleteById(scheduleModel.getId());
    }

    public BookingModel convertEntityToModel(Booking booking) {
        BookingModel bookingModel = new BookingModel();
        bookingModel.setId(booking.getId());
        bookingModel.setName(booking.getFullName());
        bookingModel.setGender(booking.getGender());
        bookingModel.setDob(booking.getDob());
        bookingModel.setEmail(booking.getEmail());
        bookingModel.setPhone(booking.getPhone());
        bookingModel.setDate(ConvertTimeUtils.dateToString(booking.getDate()));
        bookingModel.setIdHour(booking.getIdHour());
        bookingModel.setStatus(booking.getStatus().toString());
        bookingModel.setNote(booking.getNote());
        bookingModel.setNameDoctor(booking.getUser().getFullname());
        bookingModel.setMajor(booking.getUser().getMajor().getName());
        return bookingModel;
    }
}
