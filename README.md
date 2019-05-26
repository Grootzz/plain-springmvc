# 仿写SpringMVC框架

- [初始化阶段](#初始化阶段)
- [执行阶段](#执行阶段)
- [目录结构](#目录结构)

这是一个SpringMVC的仿写示例，实现了`@Controller`、`@RequestMapping`、`@RequestParam`注解的处理，以及`DispatcherServlet`的执行逻辑，旨在加强对SpringMVC的理解。

实现逻辑分为两个阶段：

- 初始化阶段
- 执行阶段

## 初始化阶段

初始化阶段的主要工作为设置SpringMVC的运行时上下环境，配置IOC容器、实例化bean和设置web工作环境。

SpringMVC的`DispatcherServlet`的`initStrategies`方法会初始化9大组件，包括：

- HandlerMapping
- HandlerAdapter
- HandlerExceptionResolver
- ViewResolver
- RequestToViewNameTranslator
- LocaleResolver
- ThemeResolver
- MultipartResolver
- FlashMapManager

对于本示例来讲，初始化阶段包括：

- 加载springMVC配置文件（用一个properties模拟）；

- 扫描用户配置包下面所有的类；

- 拿到扫描到的类，通过反射机制，实例化。并且放到ioc容器中（一个Map，键为class的名称，值为该class的实例）；

- 初始化HandlerMapping，这里其实就是把url和method对应起来放在一个k-v的Map中，在运行阶段取出。

## 执行阶段

每一次请求将会调用doGet或doPost方法，所以运行阶段都放在`doDispatch`方法里处理，它会根据url请求去HandlerMapping中匹配到对应的Method，然后利用反射机制调用Controller中的url对应的方法，并得到结果返回。按顺序包括以下功能：

- 异常的拦截
- 获取请求传入的参数并处理参数
- 通过初始化好的handlerMapping中拿出url对应的方法名，反射调用

## 目录结构

```xml
src
  └─main
      ├─java
      │  └─edu
      │      └─uestc
      │          ├─annotation # 定义@Controller、@RequestMapping、@RequestParam
      │          ├─core # Controller处理器
      │          └─servlet # DispatcherServlet请求分发逻辑
      ├─resources # mvc配置（模拟SpringMVC配置）
      └─webapp
          └─WEB-INF	# web配置文件
```

