package ligao.auth.model;

import lombok.Data;

/**
 * 登录请求
 *
 * @author Zhouyin.Wei
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
