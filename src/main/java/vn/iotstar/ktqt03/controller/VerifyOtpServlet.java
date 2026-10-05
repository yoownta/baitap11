package vn.iotstar.ktqt03.controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.ktqt03.service.*;
import vn.iotstar.ktqt03.util.WebUtil;
import vn.iotstar.ktqt03.exception.ValidationException;

@WebServlet("/verify-otp")
public class VerifyOtpServlet extends HttpServlet {
    private final AuthService auth=new AuthService();
    private final OtpService otp=new OtpService();
    private final MailService mail=new MailService();
    protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        HttpSession session=req.getSession(false);
        if(session==null || session.getAttribute("pendingOtp")==null) {
            res.sendRedirect(req.getContextPath()+"/register"); return;
        }
        WebUtil.consumeFlash(req); WebUtil.render(req,res,"auth/verify-otp");
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        HttpSession session=req.getSession(false);
        if(session==null) {res.sendRedirect(req.getContextPath()+"/register");return;}
        String action=req.getParameter("action");
        if(action!=null && !action.isBlank() && !action.equals("resend")) {res.sendError(400);return;}
        synchronized(session) {
            OtpState state=(OtpState)session.getAttribute("pendingOtp");
            if(state==null) {res.sendRedirect(req.getContextPath()+"/register");return;}
            try {
                if("resend".equals(action)) {
                    OtpState next=otp.resendOtp(state);
                    mail.sendOtp(next.getEmail(),next.getCode());
                    otp.markSent(next); session.setAttribute("pendingOtp",next);
                    WebUtil.flash(req,"message","Đã gửi OTP mới; mã cũ không còn hiệu lực.");
                } else {
                    otp.verify(state,req.getParameter("otp"));
                    auth.activate(state.getUsername(),state.getEmail());
                    session.removeAttribute("pendingOtp");
                    WebUtil.flash(req,"message","Kích hoạt thành công. Đăng nhập quản trị cần quyền admin; bạn có thể xem Trang Chủ.");
                    res.sendRedirect(req.getContextPath()+"/login");return;
                }
            } catch(ValidationException ex) {
                WebUtil.flash(req,"error",ex.getMessage());
            } catch(Exception ex) {
                log("OTP verification/delivery failed",ex);
                WebUtil.flash(req,"error","Chưa thể xử lý OTP. Vui lòng kiểm tra kết nối và thử lại.");
            }
        }
        res.sendRedirect(req.getContextPath()+"/verify-otp");
    }
}
