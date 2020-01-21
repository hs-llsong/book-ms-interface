package cn.zealon.book.system.org.service;

import cn.zealon.book.common.domain.Params;
import cn.zealon.book.common.result.PageVO;
import cn.zealon.book.common.result.Result;
import cn.zealon.book.common.result.util.ResultUtil;
import cn.zealon.book.core.cache.RedisService;
import cn.zealon.book.system.org.dao.OrgUserMapper;
import cn.zealon.book.system.org.entity.OrgUser;
import cn.zealon.book.system.security.shiro.util.ShiroUserPwdUtil;
import cn.zealon.book.system.security.shiro.util.UserUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 用户服务
 * @author zealon
 */
@Service
public class OrgUserService {

	protected final Logger logger = LoggerFactory.getLogger(OrgUserService.class);

	@Autowired
	private RedisService redisService;

	@Autowired
	private OrgUserMapper orgUserMapper;
	
	/**
	 * 获取当前用户所有角色
	 */
	public Set<String> getRolesByUser(String userId) {
		// todo
		/*List<String> list =orgRoleMapper.getRolesByUser(userId);
		Set<String> set = new HashSet<String>();
		for(String str : list){
			set.add(str);
		}
		return set;*/
		return null;
	}

	/**
	 * 获取当前用户所有权限
	 * @param userId
	 * @return
	 */
	public Set<String> getPermissionsByUser(String userId) {
		/*List<String> list =menuMapper.getPermissionsByUser(userId);
		Set<String> set = new HashSet<String>();
		for(String str : list){
			set.add(str);
		}
		return set;*/
		return null;
	}

	public PageVO<OrgUser> getPageList(Params params) {
		Integer page = params.getInt("page");
        Integer limit = params.getInt("limit");
        Integer deptId = params.getInt("deptId");
		String keyword = params.getString("keyword");
		PageHelper.startPage(page, limit);
        Page<OrgUser> pageList = (Page<OrgUser>) orgUserMapper.findPageWithResult(keyword,deptId);
        return new PageVO<>(pageList.getTotal(),200,"",pageList);
	}
	
	/***
	 * 根据用户id获取用户信息
	 */
	public OrgUser selectByUserId(String userId){
		return orgUserMapper.selectByUserId(userId);
	}


	public Result insert(OrgUser record) {
		// 查询id是否重复
		OrgUser user = orgUserMapper.selectByUserId(record.getUserId());
		if(user != null){
			return ResultUtil.verificationFailed().buildMessage("添加失败,用户ID重复!");
		}

		Date date = new Date();
		record.setCreateTime(date);
		record.setUpdateTime(date);
		record.setCreater(UserUtil.getCurrentUserId());
		record.setUpdater(UserUtil.getCurrentUserId());
		String newPwd = ShiroUserPwdUtil.generateEncryptPwd(record.getUserId(), record.getUserPwd());
		record.setUserPwd(newPwd);

		// 添加用户角色
		//orgUserServiceImpl.createUserRoles(record.getUserid(),record.getRoleId().toString());
		try{
			this.orgUserMapper.insert(record);
			return ResultUtil.success();
		} catch (Exception ex){
			logger.error("添加用户异常:{}",ex.getMessage());
			return ResultUtil.fail();
		}
	}

	public Result deleteByUserId(String userId){
		Result r = ResultUtil.success();
		if(userId.equals("admin") || userId.equals("designer")){
			r = ResultUtil.verificationFailed().buildMessage("不能删除系统管理员哦！");
		}else{
			try{
				// 删除用户角色
				// userRoleMapper.deleteByUserid(userId);

				// 删除用户
				orgUserMapper.deleteByPrimaryKey(userId);
			}catch (Exception ex){
				ex.printStackTrace();
				r = ResultUtil.fail();
			}
		}
		return r;
	}
	
	/**
	 * 修改用户密码
	 * @param userId
	 * @param newPassword
	 * @return
	 */
	public Result updatePassword(String userId, String newPassword) {
		OrgUser user = this.selectByUserId(userId);
		if (user == null) {
			return ResultUtil.notFound().buildMessage("操作失败！找不到该用户（"+userId+"）！");
		}

		//密码加密
		String password = ShiroUserPwdUtil.generateEncryptPwd(user.getUserId(), newPassword);
		try{
			OrgUser updateUser = new OrgUser();
			Date date = new Date();
			updateUser.setUserId(user.getUserId());
			updateUser.setUserPwd(password);
			updateUser.setUpdateTime(date);
			updateUser.setUpdater(UserUtil.getCurrentUserId());
			this.orgUserMapper.updateByPrimaryKey(updateUser);
			return ResultUtil.success();
		} catch (Exception ex){
			logger.error("修改用户密码异常:{}",ex.getMessage());
			return ResultUtil.fail();
		}
	}

	/**
	 * 修改用户
	 * @param record
	 * @return
	 */
	public Result update(OrgUser record){
		record.setUpdateTime(new Date());
		record.setUpdater(UserUtil.getCurrentUserId());
		// 禁止修改密码
		record.setUserPwd(null);
		// 添加用户角色
		//orgUserServiceImpl.createUserRoles(record.getUserid(),record.getRoleId().toString());
		try{
			this.orgUserMapper.updateByPrimaryKey(record);
			return ResultUtil.success();
		} catch (Exception ex){
			logger.error("修改用户密码异常:{}",ex.getMessage());
			return ResultUtil.fail();
		}
	}

	/**
	 * 用户详情
	 * @param userId
	 * @return
	 */
	public Result findByUserId(String userId){
		OrgUser user = this.orgUserMapper.selectByUserId(userId);
		return ResultUtil.success(user);
	}
}