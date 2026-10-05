package vn.iotstar.ktqt03.service;
import vn.iotstar.ktqt03.dao.UserDao;
import vn.iotstar.ktqt03.dto.SessionUser;
import vn.iotstar.ktqt03.entity.User;
import vn.iotstar.ktqt03.exception.ValidationException;
import vn.iotstar.ktqt03.util.*;

public class AuthService {
    private final UserDao users = new UserDao();
    public void register(String username, String password, String fullName, String email, String phone) {
        String name = Validation.username(username), address = Validation.email(email);
        String full = Validation.required(fullName, 50, "Họ tên"), tel = Validation.text(phone);
        Validation.password(password);
        if (tel.length()>15) throw new ValidationException("Số điện thoại dài quá 15 ký tự.");
        Jpa.tx(em -> {
            User old = users.find(em, name);
            if (old != null) {
                if (!Boolean.TRUE.equals(old.getActive()) && address.equalsIgnoreCase(old.getEmail())
                        && PasswordUtil.matches(password, old.getPassword())) return null;
                throw new ValidationException("Tên đăng nhập đã tồn tại. Tài khoản chưa kích hoạt cần đúng email và mật khẩu cũ.");
            }
            if (users.existsEmail(em,address)>0) throw new ValidationException("Email đã được sử dụng.");
            User u = new User();
            u.setUsername(name); u.setPassword(PasswordUtil.hash(password));
            u.setFullName(full); u.setEmail(address); u.setPhone(tel);
            u.setAdmin(false); u.setActive(false);
            users.insert(em,u);
            return null;
        });
    }
    public void activate(String username, String email) {
        Jpa.tx(em -> {
            User u=users.find(em,username);
            if(u==null || !email.equalsIgnoreCase(u.getEmail()))
                throw new ValidationException("Tài khoản chờ kích hoạt không còn hợp lệ.");
            u.setActive(true);
            return null;
        });
    }
    public SessionUser login(String username,String password) {
        String name=Validation.text(username);
        if(name.isEmpty() || name.length()>50 || password==null || password.length()>128) return null;
        return Jpa.read(em -> {
            User u=users.find(em,name);
            if(u==null || !PasswordUtil.matches(password,u.getPassword())) return null;
            if(!Boolean.TRUE.equals(u.getActive()))
                throw new ValidationException("Tài khoản chưa kích hoạt. Đăng ký lại bằng email và mật khẩu cũ để nhận OTP.");
            return new SessionUser(u.getUsername(),u.getFullName(),Boolean.TRUE.equals(u.getAdmin()));
        });
    }
}
