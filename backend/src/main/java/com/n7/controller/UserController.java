package com.n7.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n7.dto.MajorDTO;
import com.n7.dto.UserDTO;
import com.n7.entity.Major;
import com.n7.entity.User;
import com.n7.exception.ResourceAlreadyExitsException;
import com.n7.model.UserModel;
import com.n7.repository.UserRepo;
import com.n7.response.ErrorResponse;
import com.n7.response.SuccessResponse;
import com.n7.service.impl.CloudinaryService;
import com.n7.service.impl.MajorService;
import com.n7.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final MajorService majorService;
    private final CloudinaryService cloudinaryService;
    private final UserRepo userRepo;

    @GetMapping("/doctor/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        try{
            Optional<UserModel> userModel = userService.getDoctorById(id);
            if(!userModel.isPresent()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse<>("Not found doctor have id: " + id));
            }
            return ResponseEntity.ok().body(new SuccessResponse<>("Get data success",userModel.get()));
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>("Server Error"));
        }
    }
    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctor(@RequestParam(value = "page",defaultValue = "1") int page,
                                         @RequestParam(value = "size",defaultValue = "100") int size,
                                        @RequestParam(value = "majorId",required = false) Long majorId,
                                         @RequestParam(value = "name",required = false) String name,
                                         @RequestParam(value = "status") String status) {
        try {
            if(majorId != null && !majorService.findById(majorId).isPresent()){
                return ResponseEntity.badRequest().body(new ErrorResponse<>("Not found Id Major"));
            }
            Pageable pageable = PageRequest.of(page-1,size);
            List<UserModel> list = userService.getAllDoctorBy(majorId,name,pageable,status);
            return ResponseEntity.ok().body(new SuccessResponse<>("Get data success",list));
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>("Server Error"));
        }
    }
    @PostMapping(value = "doctor")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createDoctor(@RequestPart("file") MultipartFile multipartFile,
                                         @RequestPart("doctorDto") String object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytesGiaiMa = Base64.getDecoder().decode(object);
            String giaiMaBase64 = new String(bytesGiaiMa);
            UserDTO userDTO = objectMapper.readValue(giaiMaBase64,UserDTO.class);
            if(userService.checkName(userDTO.getUserName())) {
                return ResponseEntity.badRequest().body(new ErrorResponse<>("UserName đã tồn tại!!"));
            }
            Map data = cloudinaryService.upload(multipartFile);
            userService.saveDoctor(userDTO,data.get("url").toString(),data.get("public_id").toString());
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã tạo thành công"));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping(value = "doctor/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateDoctor(@RequestPart(value = "file",required = false) MultipartFile multipartFile,
                                         @RequestPart(value = "doctorDto") String object,
                                         @PathVariable("id") Long id) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytesGiaiMa = Base64.getDecoder().decode(object);
            String giaiMaBase64 = new String(bytesGiaiMa);
            UserDTO userDTO = objectMapper.readValue(giaiMaBase64,UserDTO.class);
            System.out.println(userDTO);
            String image = null, idImage = null;
            User user = userRepo.findById(id).get();
            if(user==null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse<>("Doctor không tồn tại!!!"));
            }
            if(multipartFile!=null){
                Map data = cloudinaryService.upload(multipartFile);
                cloudinaryService.delete(user.getUrlId());
                image = data.get("url").toString();
                idImage = data.get("public_id").toString();
                userService.updateDoctor(userDTO,id,image,idImage);
            }
            userService.updateDoctor(userDTO,id,null,null);
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã cập nhật thành công"));
        }
        catch (ResourceAlreadyExitsException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse<>(ex.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/change-pass/{id}")
    public ResponseEntity<?> changePass(@PathVariable("id") Long id,
                                        @RequestBody String pass) {
        try{
            userService.changePass(pass,id);
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã cập nhật thành công"));
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("restore-status/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> restoreStatus(@PathVariable("id") Long id){
        try{
            userService.restore(id);
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã cập nhật thành công"));
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @DeleteMapping(value = "doctor/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteDoctor(@PathVariable("id") Long id) {
        try{
            Optional<User> user = userRepo.findById(id);
            if(user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse<>("Id Doctor không tồn tại!!!"));
            }
            if(!user.get().isEnabled()) cloudinaryService.delete(user.get().getUrlId());
            userService.deleteDoctor(id);
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã xóa thành công Doctor có id: " + id));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
