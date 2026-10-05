package vn.iotstar.ktqt03.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.iotstar.ktqt03.dto.CategorySection;
import vn.iotstar.ktqt03.entity.Category;
import vn.iotstar.ktqt03.service.VideoService;
import vn.iotstar.ktqt03.util.WebUtil;

@WebServlet(urlPatterns={"/home", "/products"})
public class HomeServlet extends HttpServlet {
    private final VideoService videoService = new VideoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        List<Category> categories = videoService.listCategories();
        Map<Integer, Integer> requestedPages = new HashMap<>();
        for (Category c : categories) {
            requestedPages.put(c.getCategoryId(), WebUtil.parsePage(req.getParameter("p_" + c.getCategoryId())));
        }

        List<CategorySection> sections = videoService.homePages(requestedPages);
        WebUtil.paginationUrls(req, sections);
        req.setAttribute("sections", sections);
        WebUtil.render(req, res, "user/home");
    }
}
