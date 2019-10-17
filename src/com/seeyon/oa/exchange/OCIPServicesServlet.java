package com.seeyon.oa.exchange;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Strings;
import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.exceptions.InterfaceException;
import com.seeyon.ocip.common.org.OcipOrgUnit;
import com.seeyon.ocip.common.org.OcipView;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.LogUtils;
import com.seeyon.ocip.common.view.IViewManager;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IBussinessService;
import com.seeyon.ocip.exchange.exceptions.ExchangeException;
import com.seeyon.ocip.exchange.model.BIZContentType;
import com.seeyon.ocip.exchange.model.edoc.EdocOperation;
import com.seeyon.ocip.org.agent.BaseAgent;
import com.seeyon.ocip.org.agent.UnitAgent;
import com.seeyon.ocip.org.agent.UserAgent;
import com.seeyon.ocip.org.entity.OcipUnit;
import com.seeyon.ocip.org.entity.OcipUser;

public class OCIPServicesServlet extends HttpServlet {
	
	private static final long serialVersionUID = 3670842542658827130L;
	
	//根节点ID，测试用
	private static final String rootID = "-4730833917365171643";
	/**
	 * 发文单位名称
	 */
	public static String sendOrgName = "黔南州大数据管理局";
	
	/**
	 * 发文单位本地ID
	 */
	public static String sendOrgLocalId = "4556842553182670622";
	
	/**
	 * 发文单位本地ID对应的OCIP单位ID
	 */
	//public static String sendOrgOCIPId = "2723437946474089882";
	
	/**
	 * 公文发送人员名称
	 */
	public static String sendMemName = "黔南州公文收发员";
	
	/**
	 * 发文人员本地ID，测试用
	 */
	public static String sendMemLocalId = "-7227808391876620996";
	
	/**
	 * 发文人员本地ID对应的OCIP人员ID
	 */
	//public static String sendMemOCIPId = "-8467766985147790299";
	
	/**
	 * 接收单位名称
	 */
	public static String recOrgName = "毕节运维单位";
	
	/**
	 * 接收单位名称
	 */
	public static String recOrgName1 = "清镇市人民政府";
	
	
	/**
	 * 接收单位本地ID
	 */
	public static String recOrgLocalId = "-4702257219655679862";
	
	/**
	 * 接收单位本地ID
	 */
	public static String recOrgLocalId1 = "-835417512878194520";
	
	/**
	 * 接收单位本地ID对应的OCIP单位ID
	 */
	//public static String recOrgOCIPId = "3027673676430002247";
	
	/**
	 * 接收单位本地ID对应的OCIP单位ID
	 */
	//public static String recOrgOCIPId1 = "3305073131001430348";
	
	//TODO， 注意:惠智注册到OCIP的id，真实值由致远提供,8185024610305461530为测试环境惠智注册到OCIP的id
	//public static String hzSystenCode = "8185024610305461530";
	
	/**
	 * 公文接收人员名称
	 */
	public static String recMemName = "";
	
	/**
	 * 接收人员本地ID
	 */
	public static String recMemLocalId = "";
	
	/**
	 * 接收人员本地ID对应的OCIP人员ID
	 */
	public static String recMemOCIPId = "";
	
	

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/*WebApplicationContext wac1 = ContextLoader.getCurrentWebApplicationContext();
		IOrganizationManager bean = wac1.getBean("organizationManager", IOrganizationManager.class);

		OcipOrgUnit account = bean.getAccount("-8577900545884927029");*/
		 
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		String param = request.getParameter("type");
		param = new String(param.getBytes("ISO-8859-1"), "UTF-8");
		PrintWriter outt = response.getWriter();
		JSONObject jsonobj = new JSONObject();
		//上传组织机构
		if ("org".equals(param)) {
			boolean upLoadOrgIUnit = upLoadOrgIUnit();
			boolean upLoadOrgUser = upLoadOrgUser();
			if (upLoadOrgIUnit && upLoadOrgUser) {
				jsonobj.put("msg", "success");
			} else {
				jsonobj.put("msg", "error");
			}
			outt.println(jsonobj);
		} else if ("send".equals(param)) {// 发送公文
			String title = request.getParameter("title");
			if (Strings.isNullOrEmpty(title)) {
				title = "公文测试";
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", title);//公文标题
			map.put("recOrgID", recOrgLocalId);//公文接收单位名称
			map.put("recOrgName", recOrgName);//公文接收单位本地ID
			try {
				getBussinessService().fireExchange(BIZContentType.OFC, map);
				jsonobj.put("msg", "success");
				outt.println(jsonobj);
			} catch (ExchangeException e) {
				jsonobj.put("msg", "error");
				outt.println(jsonobj);
				//e.printStackTrace();
				LogUtils.error(OCIPServicesServlet.class, "发送公文异常", e);
			}

		}else if ("revoked".equals(param)) {//撤销公文
			String revokedId = request.getParameter("revokedId");//撤销公文的detailId
			String revokedGWID = request.getParameter("revokedGWID");//撤销公文的公文ID
			String exchNo = request.getParameter("revokedExchNo");//交换号
			String comment = request.getParameter("comment");//撤销原因
			String title = request.getParameter("title");//公文标题
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("detailId", Long.parseLong(revokedId));
			paramMap.put("groupId", Long.parseLong(revokedGWID));
			paramMap.put("subject", title);
			paramMap.put("comment", comment);
			paramMap.put("exchNo", exchNo);//交换号
			paramMap.put("name", sendMemName);//公文撤销人员名称
			//paramMap.put("id", "138913789043295577");//公文撤销人员ID
			paramMap.put("id", sendMemLocalId);//公文撤销人员本地ID
			paramMap.put("edocOperation", EdocOperation.REVOKED);
			try {
				getBussinessService().fireExchange(BIZContentType.RET, paramMap);
				jsonobj.put("msg", "success");
				outt.println(jsonobj);
			} catch (ExchangeException e) {
				jsonobj.put("msg", "error");
				outt.println(jsonobj);
				//e.printStackTrace();
				LogUtils.error(OCIPServicesServlet.class, "撤销公文异常", e);
			}
			
		}else if ("accepted".equals(param)) {//签收公文
			String mainId = request.getParameter("acceptedId");
			String detailId = request.getParameter("detailId");
			String exchNo = request.getParameter("exchNo");
			String title = request.getParameter("title");
			String comment = request.getParameter("comment");
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("groupId", mainId);
			paramMap.put("subject", title);
			paramMap.put("detailId", detailId);
			paramMap.put("exchNo", exchNo);
			paramMap.put("comment", comment);
			paramMap.put("name", sendMemName);//公文签收人员名称
			paramMap.put("id", sendMemLocalId);//公文签收人员本地ID
			paramMap.put("edocOperation",  EdocOperation.ACCEPTED);
			try {
				getBussinessService().fireExchange(BIZContentType.RET, paramMap);
				jsonobj.put("msg", "success");
				outt.println(jsonobj);
			} catch (ExchangeException e) {
				jsonobj.put("msg", "error");
				outt.println(jsonobj);
				//e.printStackTrace();
				LogUtils.error(OCIPServicesServlet.class, "签收公文异常", e);
			}
			
		}else if ("stepBack".equals(param)) {//回退公文
			String mainId = request.getParameter("stepBackId");
			String title = request.getParameter("title");
			String detailId = request.getParameter("detailId");
			String exchNo = request.getParameter("exchNo");
			String comment = request.getParameter("comment");
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("groupId", mainId);
			paramMap.put("detailId", detailId);
			paramMap.put("exchNo", exchNo);
			paramMap.put("comment", comment);
			paramMap.put("subject", title);
			paramMap.put("name", sendMemName);//公文签收人员名称
			paramMap.put("id", sendMemLocalId);//公文签收人员本地ID
			paramMap.put("edocOperation",  EdocOperation.STEPBACK);
			
			try {
				getBussinessService().fireExchange(BIZContentType.RET, paramMap);
				
			} catch (ExchangeException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				LogUtils.error(OCIPServicesServlet.class, "回退公文异常", e);
			}
			jsonobj.put("msg", "success");
			outt.println(jsonobj);
			
		}else if ("getOrg".equals(param)) {
			/**
			 * 说明：在获取OCIP视图时，不会加载全部视图
			 */
			WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext(); 
			IViewManager viewManager = wac.getBean("viewManager", IViewManager.class);
			String loginAccountId = OCIPServicesServlet.sendOrgLocalId;
			List<OcipView> allRootView;
			try {
				//获取根节点视图
				allRootView = viewManager.getAllRootView(loginAccountId);
				if (allRootView != null && allRootView.size() > 0) {
					OcipView ocipView = allRootView.get(0);
					/**
					 * 注意：在界面点根节点数据展开时，调用viewManager.getChildrenView方法获取根节点视图的下一级视图
					 */
					List<OcipView> childrenView = viewManager.getChildrenView(ocipView.getId(), false);
					for (OcipView ocipView2 : childrenView) {
						//ID为OCIP平台的单位ID
						String objectId = ocipView2.getObjectId();
					}
					System.out.println(childrenView);
				}
				//System.out.println(allRootView);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsonobj.put("msg", "success");
			outt.println(jsonobj);
		}
		

		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	
	/**
	 * 上报单位
	 * @return
	 */
	private boolean upLoadOrgIUnit() {
		boolean result = true;
		List<OcipUnit> unitInfos = new ArrayList<OcipUnit>();
		//根节点必须要上传
		OcipUnit root = new OcipUnit();
		root.setObjectId(rootID);//单位id,注意：根节点ID在上传时不要用这个，请用代码生成一个随机的id，以免不同的单位出现相同根节点的问题
		root.setName("异构单位根节点");//名称
		root.setForeignName("");
		root.setShortName("异构单位根节点");
		root.setAliasName("");
		root.setSortId(1);//排序号
		root.setCode(rootID);//单位id,注意：根节点ID在上传时不要用这个，请用代码生成一个随机的id，以免不同的单位出现相同根节点的问题
		root.setIsEnable(IConstant.ENABLE);
		root.setParentId("0");//上级单位ID，根节点没有上级单位，设置为0
		//unitInfos.add(root);
		
		OcipUnit unit1 = new OcipUnit();
		//unit1.setObjectId("2556842553182670622");//单位id
		unit1.setObjectId(sendOrgLocalId);//单位id
		unit1.setName(sendOrgName);//名称
		unit1.setForeignName("");
		unit1.setShortName("异构单位1");
		unit1.setAliasName("");
		unit1.setSortId(2);//排序号
		unit1.setCode(sendOrgLocalId);//单位id
		unit1.setIsEnable(IConstant.ENABLE);
		unit1.setParentId(rootID);//上级单位ID
		
		/*String account2 = "4556842553182670222";
		OcipUnit unit2 = new OcipUnit();
		//unit1.setObjectId("2556842553182670622");//单位id
		unit2.setObjectId(account2);//单位id
		unit2.setName("异构单位2");//名称
		unit2.setForeignName("");
		unit2.setShortName("异构单位2");
		unit2.setAliasName("");
		unit2.setSortId(2);//排序号
		unit2.setCode(account2);//单位id
		unit2.setIsEnable(IConstant.ENABLE);
		unit2.setParentId(rootID);//上级单位ID
*/		
		/*String account3 = "556842553182670222";
		OcipUnit unit3 = new OcipUnit();
		//unit1.setObjectId("2556842553182670622");//单位id
		unit3.setObjectId(account3);//单位id
		unit3.setName("异构单位3");//名称
		unit3.setForeignName("");
		unit3.setShortName("异构单位3");
		unit3.setAliasName("");
		unit3.setSortId(2);//排序号
		unit3.setCode(account3);//单位id
		unit3.setIsEnable(IConstant.ENABLE);
		unit3.setParentId(rootID);//上级单位ID
*/		
		unitInfos.add(root);
		unitInfos.add(unit1);
		//unitInfos.add(unit2);
		//unitInfos.add(unit3);
		JSONObject req = new JSONObject();
		req.put("units", unitInfos);
		
		try {
			//上传单位
			String rtn = BaseAgent.getInstance(UnitAgent.class).uploadFull(JSONObject.toJSONString(req, SerializerFeature.WriteMapNullValue));

			if (rtn == null || rtn.equals("")) {
				System.out.println("上报单位失败,代理或平台网络连接出错");
				result = false;
			}
			System.out.println("同步单位结果：" + rtn);
		} catch (InterfaceException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			LogUtils.error(OCIPServicesServlet.class, "同步单位异常", e);
			result = false;
		}
		
		return result;
	}
	
	/**
	 * 上报人员
	 * @return
	 */
	private boolean upLoadOrgUser() {
		boolean result = true;
		List<OcipUser> ocipUsers = new ArrayList<OcipUser>();
		OcipUser user = new OcipUser();
		user.setObjectId(sendMemLocalId);// 人员ID
		user.setName(sendMemName);// 异构人员名称
		user.setCode(sendMemLocalId);// 设置为人员ID
		user.setLoginName("csry1");// 登陆名
		user.setPassword("12345");// 密码
		user.setSex(OcipUser.SEX_MALE);// 性别
		user.setBirthday(new Date());// 生日
		user.setNation("");
		user.setTelNumber("13541246000");// 手机号
		user.setSortId(1);// 排序号
		user.setIsEnable(IConstant.ENABLE);//启用
		user.setLevelId("");
		user.setIsAdmin(1);//设置为非单位管理员
		List<OcipUser.Relation> relations = new ArrayList<OcipUser.Relation>();
		OcipUser.Relation relation = new OcipUser.Relation();
		relation.setType(OcipUser.Relation.RELATION_TYPE_POST_MAIN);
		relation.setUnitId(sendOrgLocalId);// 单位ID
		relations.add(relation);
		user.setRelations(relations);
		ocipUsers.add(user);
		JSONObject req = new JSONObject();
		req.put("users", ocipUsers);

		try {
			// 上传人员
			String rtn = BaseAgent.getInstance(UserAgent.class)
					.uploadFull(JSONObject.toJSONString(req, SerializerFeature.WriteMapNullValue));

			if (rtn == null || rtn.equals("")) {
				System.out.println("上报人员失败,代理或平台网络连接出错");
				result = false;
			}
			System.out.println("同步人员结果：" + rtn);
		} catch (InterfaceException e) {
			LogUtils.error(OCIPServicesServlet.class, "同步部门异常", e);
			//e.printStackTrace();
			result = false;
		}

		return result;
	}
	
	private IBussinessService getBussinessService() {
		return OcipConfiguration.getInstance().getExchangeSpi().getBussinessService();
	}

}
