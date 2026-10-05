package vn.iotstar.ktqt03.controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import vn.iotstar.ktqt03.dto.VideoCard;
import vn.iotstar.ktqt03.service.VideoService;
import vn.iotstar.ktqt03.exception.*;
import vn.iotstar.ktqt03.util.*;

@WebServlet("/admin/videos")
public class AdminVideoServlet extends HttpServlet {
    private final VideoService videos=new VideoService();
    protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        WebUtil.consumeFlash(req);
        String action=Validation.text(req.getParameter("action"));
        try {
            switch(action) {
                case "", "list" -> {
                    req.setAttribute("videoPage",videos.adminPage(WebUtil.parsePage(req.getParameter("page"))));
                    WebUtil.render(req,res,"admin/video-list");
                }
                case "create" -> {
                    VideoCard form=new VideoCard(); form.setActive(true); form.setPoster(Validation.DEFAULT_POSTER);
                    form(req,res,form,"create");
                }
                case "edit", "view" -> {
                    VideoCard video=videos.detail(Validation.id(req.getParameter("id")));
                    if(video==null) throw new NotFoundException("Không tìm thấy video");
                    if(action.equals("edit")) form(req,res,video,"update");
                    else {req.setAttribute("video",video);WebUtil.render(req,res,"user/video-detail");}
                }
                default -> res.sendError(400);
            }
        } catch(ValidationException ex) { res.sendError(400); }
          catch(NotFoundException ex) { res.sendError(404); }
    }
    protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        String action=Validation.text(req.getParameter("action"));
        if(!java.util.Set.of("create","update","delete").contains(action)) {res.sendError(400);return;}
        VideoCard value=new VideoCard();
        value.setVideoId(req.getParameter("id"));
        value.setTitle(req.getParameter("title")); value.setPoster(req.getParameter("poster"));
        value.setDescription(req.getParameter("description"));
        value.setActive("1".equals(req.getParameter("active")));
        try {
            if(action.equals("delete")) {
                videos.delete(Validation.id(value.getVideoId()));
                WebUtil.flash(req,"message","Đã xóa video và các lượt thích/chia sẻ liên quan.");
                res.sendRedirect(req.getContextPath()+"/admin/videos?page="+WebUtil.parsePage(req.getParameter("page")));
                return;
            }
            value.setCategoryId(Validation.nonNegativeInt(req.getParameter("categoryId"),"Category"));
            value.setViews(Validation.nonNegativeInt(req.getParameter("views"),"Lượt xem"));
            // Validate the path, then require the resource to exist in this web application.
            String poster=Validation.poster(value.getPoster());
            if(getServletContext().getResource(poster)==null)
                throw new ValidationException("Không tìm thấy ảnh poster trong ứng dụng.");
            String id;
            if(action.equals("create")) id=videos.create(value);
            else {id=Validation.id(value.getVideoId());videos.update(id,value);}
            WebUtil.flash(req,"message",action.equals("create")?"Đã thêm video.":"Đã cập nhật video.");
            res.sendRedirect(req.getContextPath()+"/admin/videos?action=view&id="+URLEncoder.encode(id,StandardCharsets.UTF_8));
        } catch(NotFoundException ex) {res.sendError(404);}
          catch(ValidationException ex) {
            res.setStatus(400);req.setAttribute("error",ex.getMessage());
            if(action.equals("delete")) {res.sendError(400);return;}
            req.setAttribute("viewsInput",req.getParameter("views"));
            form(req,res,value,action);
        } catch(RuntimeException ex) {
            log("Video mutation failed",ex);res.setStatus(500);
            req.setAttribute("error","Không thể lưu thay đổi. Vui lòng thử lại.");
            if(action.equals("delete")) {res.sendError(500);return;}
            form(req,res,value,action);
        }
    }
    private void form(HttpServletRequest req,HttpServletResponse res,VideoCard value,String mode)
            throws ServletException,IOException {
        req.setAttribute("video",value);req.setAttribute("mode",mode);
        req.setAttribute("categories",videos.listCategories());
        WebUtil.render(req,res,"admin/video-form");
    }
}
