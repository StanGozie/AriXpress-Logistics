package com.example.demo.utils;

import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.model.Customer;
import com.example.demo.model.Staff;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.StaffRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class AppUtil {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;


    public Staff getLoggedInStaff() throws ResourceNotFoundException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return staffRepository.findByEmail(((UserDetails)principal).getUsername())
                .orElseThrow(() -> new UserNotFoundException("Error getting logged in user"));
    }

    public Customer getLoggedInCustomer() throws ResourceNotFoundException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return customerRepository.findByEmail(((UserDetails)principal).getUsername())
                .orElseThrow(() -> new UserNotFoundException("Error getting logged in user"));
    }


    public List<String> splitStringIntoAList(String delimitedString){

        if (delimitedString!=null)
            return  Arrays.stream(delimitedString.split(",")).collect(Collectors.toList());
        return null;
    }

    private final Logger logger = LoggerFactory.getLogger(AppUtil.class);

    public void log(String message) {
        logger.info(message);
    }
    public void print(Object obj){
        try {
            logger.info(new ObjectMapper().writeValueAsString(obj));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public  String generateSerialNumber(String prefix) {
        Random rand = new Random();
        long x = (long)(rand.nextDouble()*100000000000L);
        return  prefix + String.format("%014d", x);
    }


    public boolean isValidImage(String fileName) {

        String regex = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|JPEG)$";
        Pattern p = Pattern.compile(regex);
        if (fileName == null) {
            return false;
        }
        Matcher m = p.matcher(fileName);
        return m.matches();
    }


    public boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }


    public boolean isValidPassword(String password){
        String regex = "^(?=.{8,15}$)(?=.*[A-Z])(?=.*[@#$%])(?=.*[a-z])(?=.*[0-9]).*";
        return password.matches(regex);
    }

    public String getFormattedNumber(String number){
        number=number.trim();
        if(number.startsWith("0"))
            number="+234"+number.substring(1);
        else if(number.startsWith("234"))
            number="+"+number;
        else {
            if (!number.startsWith("+")) {
                if (Integer.parseInt(String.valueOf(number.charAt(0))) > 0) {
                    number = "+234" + number;
                }
            }
        }
        return  number;
    }

    public Long generateRandomCode(){
        Random rnd = new Random();
        Long number = (long) rnd.nextInt(999);
        return  number;
    }

    public String  getStringFromObject(Object o){

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(o);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public  Object getObjectFromString(String content, Class cls){

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content,cls);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public String getReference() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return now.format(formatter);
    }

    public ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }

    public String generateReference() {
        // Generate a random UUID
        UUID uuid = UUID.randomUUID();

        // Remove the hyphens and convert to uppercase
        String reference = uuid.toString().replaceAll("-", "").toUpperCase();

        // Return the first 10 characters
        return reference.substring(0, 7);
    }
}

//^(?=.{8,32}$)(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9]).*

//^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20}$