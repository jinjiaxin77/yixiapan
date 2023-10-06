package com.jinjiaxin.yixiapan.controller;

import com.jinjiaxin.yixiapan.annotation.GlobalInterceptor;
import com.jinjiaxin.yixiapan.annotation.VerifyParam;
import com.jinjiaxin.yixiapan.component.RedisComponent;
import com.jinjiaxin.yixiapan.entity.config.AppConfig;
import com.jinjiaxin.yixiapan.entity.constants.Constants;
import com.jinjiaxin.yixiapan.entity.dto.CreateImageCode;
import com.jinjiaxin.yixiapan.entity.dto.SessionWebUserDto;
import com.jinjiaxin.yixiapan.entity.dto.UserSpaceDto;
import com.jinjiaxin.yixiapan.entity.enums.VerifyRegexEnum;
import com.jinjiaxin.yixiapan.entity.pojo.User;
import com.jinjiaxin.yixiapan.entity.vo.ResponseVO;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import com.jinjiaxin.yixiapan.service.EmailCodeService;
import com.jinjiaxin.yixiapan.service.UserInfoService;
import com.jinjiaxin.yixiapan.utils.StringTools;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * 用户信息 Controller
 */
@RestController("userInfoController")
@Slf4j
public class AccountController extends ABaseController{

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private EmailCodeService emailCodeService;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private RedisComponent redisComponent;

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
	public ResponseVO sendEmailCode(HttpSession session, @VerifyParam(required = true, max = 150, regex = VerifyRegexEnum.EMAIL) String email,String checkCode, @VerifyParam(required = true, max = 1, min = 1) Integer type){
		String code = (String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL);
		try{
			if(checkCode.equalsIgnoreCase(code)){
				emailCodeService.sendEmailCode(email,type);

				return getSuccessResponseVO(null);
			}else{
				throw new BusinessException("验证码不正确");
			}
		}finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
		}
	}

	@PostMapping("/register")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO register(HttpSession session, @VerifyParam(required = true) String email, @VerifyParam(required = true) String emailCode, @VerifyParam(required = true) String nickName, @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 10) String password, @VerifyParam(required = true) String checkCode){
		try{
			if(checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))){
				userInfoService.register(email,nickName,password,emailCode);

				return getSuccessResponseVO(null);
			}else{
				throw new BusinessException("图片验证码错误");
			}
		}finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	@PostMapping("/login")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO login(HttpSession session, @VerifyParam(required = true, max = 150, regex = VerifyRegexEnum.EMAIL) String email, @VerifyParam(required = true) String password, @VerifyParam(required = true) String checkCode ){
		try{
			if(!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))){
				throw new BusinessException("图片验证码错误");
			}

			SessionWebUserDto userDto = userInfoService.login(email, password);
			session.setAttribute(Constants.SESSION_KEY,userDto);
			return getSuccessResponseVO(userDto);
		}finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	@PostMapping("/resetPwd")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO resetPwd(HttpSession session, @VerifyParam(required = true, max = 150, regex = VerifyRegexEnum.EMAIL) String email, @VerifyParam(required = true) String password, @VerifyParam(required = true) String checkCode, @VerifyParam(required = true) String emailCode){
		try{
			if(!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))){
				throw new BusinessException("图片验证码错误");
			}

			userInfoService.resetPwd(email,password,emailCode);
			return getSuccessResponseVO(null);
		}finally {
			session.removeAttribute(Constants.CHECK_CODE_KEY);
		}
	}

	@GetMapping("/getAvatar/{userId}")
	@GlobalInterceptor(checkParams = true)
	public void getAvatar(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId){
		String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
		File folder = new File(appConfig.getProjectFolder() + avatarFolderName);
		if(!folder.exists()){
			folder.mkdirs();
		}
		String avatarPath = appConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
		File file = new File(avatarPath);
		if(!file.exists()){
			File defaultAvatar = new File(appConfig.getProjectFolder() + avatarFolderName + Constants.DEFAULT_AVATAR);
			if(!defaultAvatar.exists()){
				printNoDefaultImage(response);
			}
			avatarPath = appConfig.getProjectFolder() + avatarFolderName + Constants.DEFAULT_AVATAR;
		}
		response.setContentType("image/jpg");
		readFile(response,avatarPath);
	}

	private void printNoDefaultImage(HttpServletResponse response) {
		response.setHeader(Constants.CONTENT_TYPE,Constants.CONTENT_TYPE_VALUE);
		response.setStatus(HttpStatus.OK.value());
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.println("请在头像目录下放置默认头像default_avatar.jpg");
			writer.close();
		} catch (IOException e) {
            log.error("无默认图",e);
        }finally {
			writer.close();
		}
    }

	@GetMapping("/getUserInfo")
	@GlobalInterceptor(checkParams = true,checkLogin = true)
	public ResponseVO getUserInfo(HttpSession session){
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
		return getSuccessResponseVO(sessionWebUserDto);
	}

	@GetMapping("/getUseSpace")
	@GlobalInterceptor(checkParams = true,checkLogin = true)
	public ResponseVO getUseSpace(HttpSession session){
		SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);

		UserSpaceDto userSpaceDto = redisComponent.getUserSpaceDto(sessionWebUserDto.getUserId());

		return getSuccessResponseVO(userSpaceDto);
	}

	@PostMapping("/logout")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO logout(HttpSession session){
		session.invalidate();
		return getSuccessResponseVO(null);
	}

	@PostMapping("/updateUserAvatar")
	@GlobalInterceptor(checkParams = true,checkLogin = true)
	public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);

		String basePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
		String avatarFolder = basePath + Constants.FILE_FOLDER_AVATAR_NAME;
		File targetFileFolder = new File(avatarFolder);
		if(!targetFileFolder.exists()){
			targetFileFolder.mkdirs();
		}
		File targetAvatarFile = new File(targetFileFolder.getPath() + "/" + webUserDto.getUserId() + Constants.AVATAR_SUFFIX);

		try{
			avatar.transferTo(targetAvatarFile);
		} catch (IOException e) {
            log.error("上传头像失败",e);
        }

		User user = new User();
		user.setQqAvatar("");
		userInfoService.updateUserAvatarById(webUserDto.getUserId(),user);
		webUserDto.setAvatar(null);
		session.setAttribute(Constants.SESSION_KEY,webUserDto);
		return getSuccessResponseVO(null);
    }

	@PostMapping("updatePassword")
	@GlobalInterceptor(checkParams = true,checkLogin = true)
	public ResponseVO updatePassword(HttpSession session, @VerifyParam(required = true,max = 18, min = 8, regex = VerifyRegexEnum.PASSWORD) String password){
		SessionWebUserDto webUserDto = getUserInfoFromSession(session);

		User user = new User();
		user.setPassword(StringTools.encodeByMd5(password));
		userInfoService.updateUserPasswordById(webUserDto.getUserId(),user);
		return getSuccessResponseVO(null);
	}

	@PostMapping("/qqlogin")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO qqLogin(HttpSession session, String callbackUrl) throws UnsupportedEncodingException {
		String state = StringTools.getRandomNumber(Constants.LENGTH_30);
		if(!StringTools.isEmpty(callbackUrl)){
			session.setAttribute(state,callbackUrl);
		}
		String url = String.format(appConfig.getQqUrlAuthorization(),appConfig.getQqAppId(), URLEncoder.encode(appConfig.getQqUrlRedirect(),"utf-8"),state);

		return getSuccessResponseVO(url);
	}

	@PostMapping("/qqlogin/callback")
	@GlobalInterceptor(checkParams = true)
	public ResponseVO qqLoginCallBAck(HttpSession session, @VerifyParam(required = true) String code, @VerifyParam(required = true) String state){
		Map<String,Object> result = new HashMap<>();
		SessionWebUserDto webUserDto = userInfoService.qqLogin(code);
		session.setAttribute(Constants.SESSION_KEY,webUserDto);

		result.put("callbackUrl",session.getAttribute(state));
		result.put("userInfo",webUserDto);

		return getSuccessResponseVO(result);
	}

}