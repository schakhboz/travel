package uz.insonline.travel.commons.config;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.insonline.travel.commons.exceptions.ForbiddenException;

import java.security.MessageDigest;

@Service
public class Md5Encoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null)
            throw new ForbiddenException("Password cannot be null");

        StringBuilder builder = new StringBuilder();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(rawPassword.toString().getBytes());
            for (byte b : bytes) {
                builder.append(String.format("%2X", b));
            }
        } catch (Exception ignored) {
        }
        return builder.toString().replace(" ", "0");
    }


    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        return encode(rawPassword).equals(encodedPassword);
    }


    public String sh1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHARu1");
            md.update(input.getBytes());
            byte[] output = md.digest();
            return bytesToHex(output);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return "";
    }

    public String bytesToHex(byte[] b) {
        char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < b.length; j++) {
            buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
            buf.append(hexDigit[b[j] & 0x0f]);
        }
        return buf.toString();
    }


}
