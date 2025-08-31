/**
 * Cealus Li 2025/7/5
 * Copyright
 */
package org.geniusSociety.codelooms.common.base;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.geniusSociety.codelooms.common.constant.BaseConstant;
import org.geniusSociety.codelooms.common.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础Controller
 *
 * @author Cealus Li
 * @date 2025/7/5
 */
public abstract class BaseController {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    protected HttpServletRequest getRequest() {
        return request;
    }

    protected HttpServletResponse getResponse() {
        return response;
    }

    /**
     * 获取用户
     *
     * @return
     */
    protected UserDTO getUser() {
        return (UserDTO) this.request.getSession().getAttribute(BaseConstant.SESSION_USER_KEY);
    }

    /**
     * 获取用户ID
     *
     * @return
     */
    protected Integer getUserId() {
        UserDTO user = (UserDTO) this.request.getSession().getAttribute(BaseConstant.SESSION_USER_KEY);
        if (null != user) {
            return user.getId();
        }
        return BaseConstant.BASE_ID;
    }
}
