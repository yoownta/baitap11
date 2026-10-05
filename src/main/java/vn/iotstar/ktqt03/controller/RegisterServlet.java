package vn.iotstar.ktqt03.controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.ktqt03.service.*;
import vn.iotstar.ktqt03.util.*;
import vn.iotstar.ktqt03.exception.ValidationException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final AuthService auth=new AuthService();
    private final OtpService otp=new OtpService();
    private final MailService mail=new MailService();
    protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        WebUtil.render(req,res,"auth/register");
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        for(String key:new String[]{"username","fullName","email","phone"}) req.setAttribute(key,req.getParameter(key));
        try {
            String password=req.getParameter("password");
            Validation.password(password);
            if(!password.equals(req.getParameter("confirmPassword")))
                throw new ValidationException("Mật khẩu xác nhận không khớp.");
            String username=Validation.username(req.getParameter("username"));
            String email=Validation.email(req.getParameter("email"));
            HttpSession session=req.getSession();
            synchronized(session) {
                auth.register(username,password,req.getParameter("fullName"),email,req.getParameter("phone"));
                OtpState old=(OtpState)session.getAttribute("pendingOtp");
                boolean same=old!=null && old.getUsername().equals(username);
                OtpState state=same?otp.resendOtp(old):otp.createOtp(username,email);
                if(!same) session.setAttribute("pendingOtp",state);
                try {
                    mail.sendOtp(state.getEmail(),state.getCode());
                    otp.markSent(state);
                    session.setAttribute("pendingOtp",state);
                    WebUtil.flash(req,"message","Đã gửi OTP. Kiểm tra email và xác minh trong 5 phút.");
                } catch(Exception ex) {
                    log("OTP delivery failed",ex);
                    WebUtil.flash(req,"error","Tài khoản đang chờ kích hoạt nhưng chưa gửi được OTP. Kiểm tra cấu hình email và chọn Gửi lại.");
                }
            }
            res.sendRedirect(req.getContextPath()+"/verify-otp");
        } catch(ValidationException ex) {
            res.setStatus(400); req.setAttribute("error",ex.getMessage());
            WebUtil.render(req,res,"auth/register");
        } catch(RuntimeException ex) {
            log("Registration failed",ex); res.setStatus(500);
            req.setAttribute("error","Chưa thể đăng ký. Vui lòng thử lại.");
            WebUtil.render(req,res,"auth/register");
        }
    }
}
