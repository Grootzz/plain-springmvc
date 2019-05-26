package edu.uestc.servlet;

import edu.uestc.annotation.MyController;
import edu.uestc.annotation.MyRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * springmvc 中的 DispatcherServlet
 *
 * @author noodle
 */
public class MyDispatcherServlet extends HttpServlet {

    /**
     * 存储web.xml中的初始化参数，即springmvc的配置文件
     */
    private Properties properties = new Properties();

    /**
     * 根据配置文件解析出 xxxController.class
     */
    List<String> classNames = new ArrayList<>();

    /**
     * 所以controller，也就是spring中的容器
     */
    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, Method> handlerMapping = new HashMap<>();

    private Map<String, Object> controllerMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2.初始化所有相关联的类,扫描用户设定的包下面所有的类
        doScanner(properties.getProperty("scanPackage"));
        //3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
        doInstance();
        //4.初始化HandlerMapping(将url和method对应上)
        initHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //处理请求
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }


    /**
     * 初始化HandlerMapping(将url和method对应上)
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty())
            return;

        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {

                Class<?> aClass = entry.getValue().getClass();

                if (!aClass.isAnnotationPresent(MyController.class))
                    continue;

                /** 拼接url，包含类上的url可方法上的url */
                String baseUrl = "";
                // 1. 提取类上的url
                if (aClass.isAnnotationPresent(MyRequestMapping.class)) {
                    MyRequestMapping annotation = aClass.getAnnotation(MyRequestMapping.class);
                    baseUrl = annotation.value();
                }
                // 2. 提取方法上的url
                Method[] methods = aClass.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(MyRequestMapping.class))
                        continue;

                    MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                    String value = annotation.value();
                    String url = (baseUrl + "/" + value).replaceAll("/+", "/");

                    // 将url对应的controller实例和示例中的方法对应起来
                    controllerMap.put(url, aClass.newInstance());
                    handlerMapping.put(url, method);
                    System.out.println(url + ": " + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写
     */
    private void doInstance() {
        if (classNames.isEmpty())
            return;
        for (String className : classNames) {
            try {
                // 提取出只包含 @MyController 注解的类，并实例化
                Class<?> aClass = Class.forName(className);
                if (aClass.isAnnotationPresent(MyController.class))
                    ioc.put(aClass.getSimpleName(), aClass.newInstance());
                else
                    continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理请求
     *
     * @param req
     * @param resp
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (handlerMapping.isEmpty())
            return;

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();

        //拼接url并把多个/替换成一个
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        if (!handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = handlerMapping.get(url);

        //获取注解@RequestMapping上方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();

        //获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        //保存参数值
        Object[] paramValues = new Object[parameterTypes.length];

        //方法的参数列表
        for (int i = 0; i < parameterTypes.length; i++) {
            //根据参数名称，做某些处理
            String requestParam = parameterTypes[i].getSimpleName();


            if (requestParam.equals("HttpServletRequest")) {
                //参数类型已明确，这边强转类型
                paramValues[i] = req;
                continue;
            }

            if (requestParam.equals("HttpServletResponse")) {
                paramValues[i] = resp;
                continue;
            }

            if (requestParam.equals("String")) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue())
                            .replaceAll("\\[|\\]", "")
                            .replaceAll(",\\s", ",");
                    paramValues[i] = value;
                }
            }
        }
        //利用反射机制来调用实例方法，即url对应的示例的方法
        method.invoke(controllerMap.get(url), paramValues);
    }

    /**
     * 初始化所有相关联的类, 扫描用户设定的包下面所有的类
     *
     * @param packageName
     */
    private void doScanner(String packageName) {
        //把所有的.替换成/
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                //递归读取包
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 加载 springmvc 配置文件
     *
     * @param contextConfigLocation 配置文件位置
     */
    private void doLoadConfig(String contextConfigLocation) {
        // 把web.xml中的contextConfigLocation对应value值的文件加载到流里面
        InputStream resource = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            // 用Properties文件加载文件里的内容
            properties.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
