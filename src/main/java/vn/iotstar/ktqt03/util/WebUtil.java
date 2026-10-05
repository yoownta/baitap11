package vn.iotstar.ktqt03.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import vn.iotstar.ktqt03.dto.CategorySection;

public final class WebUtil {
    private WebUtil() {}

    public static int parsePage(String raw) {
        if (raw == null || raw.isBlank()) return 1;
        try {
            int page = Integer.parseInt(raw);
            return page <= 0 ? 1 : page;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static String buildUrl(HttpServletRequest request, List<CategorySection> sections, int targetCategoryId, int targetPage) {
        String base = request.getContextPath() + request.getServletPath();
        String params = sections.stream()
                .map(s -> "p_" + s.getCategoryId() + "=" + (s.getCategoryId() == targetCategoryId ? targetPage : s.getVideoPage().getPage()))
                .collect(Collectors.joining("&"));
        return base + "?" + params + "#cat-" + targetCategoryId;
    }

    public static void paginationUrls(HttpServletRequest request, List<CategorySection> sections) {
        for (CategorySection sec : sections) {
            Map<Integer, String> urls = new LinkedHashMap<>();
            for (int i = 1; i <= sec.getVideoPage().getTotalPages(); i++)
                urls.put(i, buildUrl(request, sections, sec.getCategoryId(), i));
            sec.setPageUrls(urls);
            sec.setFirstUrl(urls.get(1));
            sec.setLastUrl(urls.get(sec.getVideoPage().getTotalPages()));
            sec.setPreviousUrl(urls.get(Math.max(1, sec.getVideoPage().getPage() - 1)));
            sec.setNextUrl(urls.get(Math.min(sec.getVideoPage().getTotalPages(), sec.getVideoPage().getPage() + 1)));
        }
    }

    public static void render(HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res, String view)
            throws java.io.IOException, jakarta.servlet.ServletException {
        res.setContentType("text/html;charset=UTF-8");
        // include avoids the Tomcat 11 forward/wrapper interaction with SiteMesh 3.2.
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").include(req, res);
    }

    public static void flash(HttpServletRequest req, String key, String message) {
        req.getSession().setAttribute("flash." + key, message);
    }
    public static void consumeFlash(HttpServletRequest req) {
        var session = req.getSession(false);
        if (session == null) return;
        for (String key : List.of("message", "error")) {
            Object value = session.getAttribute("flash." + key);
            if (value != null) {
                req.setAttribute(key, value);
                session.removeAttribute("flash." + key);
            }
        }
    }
}
