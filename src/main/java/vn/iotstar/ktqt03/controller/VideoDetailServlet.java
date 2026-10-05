package vn.iotstar.ktqt03.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import vn.iotstar.ktqt03.dto.VideoCard;
import vn.iotstar.ktqt03.service.VideoService;
import vn.iotstar.ktqt03.util.WebUtil;

@WebServlet("/video")
public class VideoDetailServlet extends HttpServlet {
    private final VideoService videoService = new VideoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (id == null || id.isBlank() || id.length() > 50) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        VideoCard video = videoService.detail(id);
        if (video == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if(!video.isActive()) { res.sendError(404); return; }
        req.setAttribute("mediaUrl",new vn.iotstar.ktqt03.service.ShopService().mediaFor(id));
        req.setAttribute("video", video);
        WebUtil.render(req, res, "user/video-detail");
    }
}
