package edu.uestc.core;

import edu.uestc.annotation.MyController;
import edu.uestc.annotation.MyRequestMapping;
import edu.uestc.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * springmvc 中的控制器
 */
@MyController
@MyRequestMapping("/plain")
public class TestController {

    /**
     * 测试
     *
     * @param request
     * @param response
     * @param param
     */
    @MyRequestMapping("/doTest")
    public void test1(HttpServletRequest request,
                      HttpServletResponse response,
                      @MyRequestParam("param") String param) throws IOException {
        System.out.println(param);
        response.getWriter().write("doTest method success! param:" + param);
    }

    @MyRequestMapping("/doTest2")
    public void test2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println("doTest2 method success!");
    }

}
