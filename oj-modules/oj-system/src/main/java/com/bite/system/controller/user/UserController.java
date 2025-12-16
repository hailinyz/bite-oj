package com.bite.system.controller.user;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.system.domain.user.dto.UserDTO;
import com.bite.system.domain.user.dto.UserQueryDTO;
import com.bite.system.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService userService;

    /*
    * 获取用户列表
     */
    @GetMapping("/list")
    public TableDataInfo list(UserQueryDTO userQueryDTO){ //因为涉及到分页，所以返回值是TableDataInfo
        return getTableDataInfo(userService.list(userQueryDTO));
    }

    /*
    * 修改用户状态
     */
    // todo 拉黑/接近：限制/放开用户操作票
    @PutMapping("updateStatus")
    public R<Void> updateStatus(@RequestBody UserDTO userDTO){
        return toR(userService.updateStatus(userDTO));
    }

}
