package Response;

public class Code {

    public static final int QUIT_SUCCESS = 221;  //成功退出
    public static final int NOOP_TEST_SUCCESS = 200; //NOOP测试成功

    //登录相关：
    public static final int LOGIN_SUCCESS_CODE = 230;  //登陆成功
    public static final int USER_EXIST_CODE = 201;  //用户名存在,需要密码
    public static final int USER_NOT_EXIST_CODE = 403;  //用户名不存在
    public static final int PASSWORD_ERROR_CODE = 530;  //密码错误
    public static final int USER_NOT_LOGIN = 401; //用户未登录

    //命令
    public static final int COMMAND_NOT_EXIST = 202;  //找不到命令
    public static final int COMMAND_EXECUTE_SUCCESS = 200; //命令执行成功

    public static final int PARAMETER_ERROR = 501;

    //文件传输
    public static final int FILE_NOT_FOUND = 550 ; // 请求的操作无法执行，文件不可用（例如找不到文件，无访问权）。
    public static final int FILE_IS_OK = 150; // 文件状态没问题，准备打开数据连接。
    public static final int CANNOT_OPEN_DATA_CONNECT = 425;// 无法打开数据连接。
    public static final int DATA_CONNECT_IS_OPEN = 125;// 数据连接已打开，传输启动。
    public static final int FILE_TRANSFER_SUCCESS = 226; // 关闭数据连接，请求的文件操作已成功。

    public static final int FILE_OPERATION_SUCCESS = 250; // 请求的文件操作正常进行，已完成。



}
