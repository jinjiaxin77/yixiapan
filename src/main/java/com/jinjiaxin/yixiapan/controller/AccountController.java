package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.CreateImageCode;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.service.EmailCodeService;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * 用户信息 Controller
 */
@RestController("userInfoController")
public class AccountController extends ABaseController{

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private EmailCodeService emailCodeService;

	@GetMapping("/checkCode")
	public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
		CreateImageCode vCode = new CreateImageCode(130,80,5,10);
		response.setHeader("Pragma","no-cache");
		response.setHeader("Cache-Control","no-cache");
		response.setDateHeader("Expires",0);
		response.setContentType("image/jpeg");
		String code = vCode.getCode();
		if( type == null || type == 0){
			session.setAttribute(Constants.CHECK_CODE_KEY,code);
		}else{
			session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
		}
		vCode.write(response.getOutputStream());
	}

	@PostMapping("/sendEmailCode")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO sendEmailCode(HttpSession session,@VerifyParam(required = true,max = 150,min = 8) String email, @VerifyParam(required = true, max = 5, min = 5) String checkCode, @VerifyParam(required = true, max = 1, min = 1) Integer type){
		String code = (String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL);
		try{
			if(checkCode.equals(code)){
				emailCodeService.sendEmailCode(email,type);

				return getSuccessResponseVO(null);
			}else{
				throw new BusinessException("验证码不正确");
			}
		}finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
		}

	}
}