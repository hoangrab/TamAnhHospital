package com.n7.service.impl;

import com.n7.dto.ArticleDTO;
import com.n7.dto.UserDTO;
import com.n7.entity.Article;
import com.n7.entity.Major;
import com.n7.entity.Role;
import com.n7.entity.User;
import com.n7.exception.ResourceAlreadyExitsException;
import com.n7.exception.ResourceNotFoundException;
import com.n7.model.CustomUserDetail;
import com.n7.model.MajorModel;
import com.n7.model.UserModel;
import com.n7.repository.MajorRepo;
import com.n7.repository.RoleRepo;
import com.n7.repository.UserRepo;
import com.n7.request.LoginRequest;
import com.n7.request.RegisterRequest;
import com.n7.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final MajorRepo majorRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public Map<String,String> login(LoginRequest loginRequest) {
        try{
            Map<String,String> map = new HashMap<>();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken((CustomUserDetail) userDetails);
            map.put("accessToken",jwt);
            map.put("roleId",String.valueOf(((CustomUserDetail) userDetails).getRoleId()));
            map.put("userId",String.valueOf(((CustomUserDetail) userDetails).getUserId()));
            return map;
        }catch (BadCredentialsException ex){
            throw new BadCredentialsException("Invalid username or password");
        }
    }

//    @Transactional
//    public UserModel register(UserDTO userDTO) {
//        Optional<Role> role = roleRepo.findById(1L);
//        if(!role.isPresent()) throw new ResourceNotFoundException("Invalid id role");
//        User u = new User();
//        u.setPassword(passwordEncoder.encode(userDTO.getPassword()));
//        u.setUsername(userDTO.getUserName());
//        u.setFullname(userDTO.getFullName());
//        u.setRole(role.get());
//        u.setEnabled(true);
//        UserModel userModel = convertEntityToModel(u);
//        userRepo.save(u);
//        return userModel;
//    }

    public boolean checkName(String name) {
        return userRepo.findByUsername(name)!=null;
    }

    public void changePass(String pass, Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found Doctor with id :" + id));
        user.setPassword(passwordEncoder.encode(pass));
        userRepo.save(user);
    }

    public void restore(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found Doctor with id :" + id));
        user.setEnabled(true);
        userRepo.save(user);
    }


    public List<UserModel> getAllDoctorBy(Long majorId,String name, Pageable pageable,String status) {
        Boolean enabled = null;
        if (status != null) enabled = status.equals("true");
        return userRepo.findByCustom(majorId,enabled,name,pageable)
                .getContent().stream()
                .filter(e -> e.getRole().getId() != 1)
                .map(this::convertEntityToModel).collect(Collectors.toList());
    }

    public Optional<UserModel> getDoctorById(Long id) {
        return userRepo.findById(id).map(this::convertEntityToModel);
    }

    @Transactional
    public void saveDoctor(UserDTO userDTO, String image, String idImage) {
        User user = new User();
        Role role = roleRepo.findById(2L).get();
        Major major = majorRepo.findById(userDTO.getMajorId()).get();
        user.setRole(role);
        user.setMajor(major);
        convertDTOtoEntity(userDTO,user);
        if(idImage != null && image != null) {
            user.setUrlId(idImage);
            user.setAvatar(image);
        }
        user.setTrangthai("dl");
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userRepo.save(user);
    }

    @Transactional
    public void updateDoctor(UserDTO userDTO, Long id, String image, String idImage) {
        User user = userRepo.findById(id).get();
        if(user.getUsername().equals(userDTO.getUserName())==false && userRepo.findByUsername(userDTO.getUserName())!=null){
            throw new ResourceAlreadyExitsException("UserName đã bị trùng");
        }
        if(image != null && idImage != null) {
            user.setUrlId(idImage);
            user.setAvatar(image);
        }
        Major major = majorRepo.findById(userDTO.getMajorId()).get();
        if(major==null) {throw new ResourceNotFoundException("Không tìm thấy khoa cần lưu");}
        else user.setMajor(major);
        convertDTOtoEntity(userDTO,user);
        if(!userDTO.getTrangthai().equals("dl")) {
            user.setEnabled(false);
            user.setTrangthai(userDTO.getTrangthai());
        }
        else {
            user.setTrangthai("dl");
            user.setEnabled(true);
        }
        userRepo.save(user);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        User user = userRepo.findById(id).get();
//        if(user.isEnabled()==true) {
//            user.setEnabled(false);
//            userRepo.save(user);
//        }
//        else {
//            userRepo.deleteById(id);
//        }
        userRepo.deleteById(id);
    }

    public void convertDTOtoEntity(UserDTO userDTO, User user) {
        user.setFullname(userDTO.getFullName());
        user.setUsername(userDTO.getUserName());
        user.setGmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setDescription(userDTO.getDescription());
    }



    public UserModel convertEntityToModel(User user) {
        MajorModel majorModel = new MajorModel();
        if(user.getMajor()!=null) {
            majorModel.setId(user.getMajor().getId());
            majorModel.setName(user.getMajor().getName());
        }
        UserModel userModel = new UserModel(user.getId(),user.getAvatar(),user.getFullname(),user.getUsername(),user.getPhone(),
                user.getGmail(), user.getDescription(), user.getRole().getId(),user.isEnabled(),majorModel,user.getTrangthai());
        return userModel;
    }
}
