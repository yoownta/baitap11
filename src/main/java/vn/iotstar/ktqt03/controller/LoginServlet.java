package vn.iotstar.ktqt03.controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.ktqt03.dto.SessionUser;
import vn.iotstar.ktqt03.service.AuthService;
import vn.iotstar.ktqt03.util.WebUtil;
import vn.iotstar.ktqt03.exception.ValidationException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final AuthService auth=new AuthService();
    protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        WebUtil.consumeFlash(req); WebUtil.render(req,res,"auth/login");
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        HttpSession old=req.getSession(false);
        if(old!=null) old.removeAttribute("authUser");
        req.setAttribute("username",req.getParameter("username"));
        try {
            SessionUser user=auth.login(req.getParameter("username"),req.getParameter("password"));
            if(user==null) throw new ValidationException("Sai tài khoản hoặc mật khẩu.");
            HttpSession session=req.getSession();
            req.changeSessionId(); session.setAttribute("authUser",user);
            session.removeAttribute("pendingOtp");
            if (user.isAdmin()) {
                res.sendRedirect(req.getContextPath()+"/admin/videos");
            } else {
                res.sendRedirect(req.getContextPath()+"/home");
            }
            return;
        } catch(ValidationException ex) {
            req.setAttribute("error",ex.getMessage());
        } catch(RuntimeException ex) {
            log("Login failed",ex); res.setStatus(500);
            req.setAttribute("error","Chưa thể đăng nhập. Vui lòng thử lại.");
        }
        WebUtil.render(req,res,"auth/login");
    }
}
