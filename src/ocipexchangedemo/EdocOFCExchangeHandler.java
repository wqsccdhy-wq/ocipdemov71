package ocipexchangedemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.seeyon.oa.exchange.OCIPServicesServlet;
import com.seeyon.ocip.common.IConstant;
import com.seeyon.ocip.common.entry.Address;
import com.seeyon.ocip.common.license.OcipKeyMananger;
import com.seeyon.ocip.common.org.OcipOrgMember;
import com.seeyon.ocip.common.org.OcipOrgRelation;
import com.seeyon.ocip.common.org.OcipOrgUnit;
import com.seeyon.ocip.common.org.OrgDepartment;
import com.seeyon.ocip.common.org.OrgUnit;
import com.seeyon.ocip.common.organization.IOrganizationManager;
import com.seeyon.ocip.common.utils.LogUtils;
import com.seeyon.ocip.configuration.OcipConfiguration;
import com.seeyon.ocip.exchange.api.IBussinessHandler;
import com.seeyon.ocip.exchange.api.IBussinessService;
import com.seeyon.ocip.exchange.exceptions.BussinessException;
import com.seeyon.ocip.exchange.model.AttachmentFile;
import com.seeyon.ocip.exchange.model.BIZContentType;
import com.seeyon.ocip.exchange.model.BIZExchangeData;
import com.seeyon.ocip.exchange.model.BIZMessage;
import com.seeyon.ocip.exchange.model.BussinessResult;
import com.seeyon.ocip.exchange.model.Organization;
import com.seeyon.ocip.exchange.model.PropertyValue;
import com.seeyon.ocip.exchange.model.edoc.EdocOperation;
import com.seeyon.ocip.exchange.model.edoc.OFCEdocObject;
import com.seeyon.ocip.exchange.model.edoc.SeeyonEdoc;
/**
 * 公文交换数据封装参考DEMO
 * @author Administrator
 *
 */
public class EdocOFCExchangeHandler implements IBussinessHandler {
	
	private IOrganizationManager organizationManager;
	
	//private static List<String> recUnitIdList = new ArrayList<String>(1);
	//公文发文单位信息Map
	private static Map<String, String> recUnitMap = new HashMap<String, String>();

	/**
	 * 接收数据
	 */
	@Override
	public List<BussinessResult> exchangeReceive(BIZExchangeData in) throws BussinessException {
		List<BussinessResult> bussinessResults = new ArrayList<BussinessResult>();
		BIZMessage bussnissMessage = in.getBussnissMessage();
		/**
		 * TODO，收到文件时，需要保存
		 * exchNo 交换号ID，需要保存
		 * groupId 需要保存，groupId为公文ID
		 * myDetail 需要保存
		 */
		Map<Long , Long> myDetailMap = new HashMap<Long , Long>();
		String exchNo = in.getIdentifier();//交换号ID，需要保存
		String groupId = in.getGroupId();//需要保存，groupId为公文ID
		Organization organization = in.getSender();
		//发文人员信息
		Address creater = organization.getIdentification();
		BIZContentType contentType = bussnissMessage.getContentType();
		List<Organization> reciList = in.getRecivers();//收文单位信息
		for (Organization organization2 : reciList) {
			BussinessResult result = new BussinessResult();
			result.setCode("0");
			result.setIdentifier(in.getIdentifier());
			result.setOrganization(organization2);
			result.setMessage("公文接受成功");
			bussinessResults.add(result);
		}
		Long mainId = null;
		HashMap<Long, Long> detailIds = null;
		Long relationId = null;
		String unitLocalId = null;
		try {
			
			if (BIZContentType.OFC.equals(contentType)) {
				String id = creater.getId();
				OcipOrgMember ocipMember = organizationManager.getMember(id);
				List<OcipOrgRelation> relations = ocipMember.getRelations();
				if (relations!=null && !relations.isEmpty()) {
					//发文单位信息
					OcipOrgRelation ocipOrgRelation = relations.get(0);
					String unitId = ocipOrgRelation.getUnitId();
					OcipOrgUnit account = organizationManager.getAccount(unitId);
					if (account != null) {
						/**
						 * TDDO
						 * 注意:实际开发中，公文发起者所属单位对于的OCIP单位ID，该ID需要保存下来，在进行公文的签收、回退以及回执时会用到，demo中用集合保存
						 * 实际开发中应该保存到数据库和公文有映射关系
						 * 
						 */
						String accountID = account.getId();//公文发起者所属单位对于的OCIP单位，该ID需要保存下来，在进行公文的签收、回退以及回执时会用到
						recUnitMap.clear();
						String accountName = account.getName();// 发文单位名称
						recUnitMap.put(accountID, accountName);// demo中用集合保存
						System.out.println("accountID：" + accountID + "|accountName:" + accountName);
					}
				}
				
				OFCEdocObject ofcEdocObject = (OFCEdocObject) bussnissMessage.getContent();
				//公文元素实例
				SeeyonEdoc seeyonEdoc = (SeeyonEdoc)ofcEdocObject.getExtendAttr();
				String subject = seeyonEdoc.getSubject();
				//公文文号
				PropertyValue docMark = seeyonEdoc.getDocMark();
				String display = docMark.getDisplay();
				
				relationId = Long.parseLong(seeyonEdoc.getDocumentIdentifier());
				mainId = Long.parseLong(seeyonEdoc.getMainId());
				detailIds = seeyonEdoc.getDetailIds();
				//签发人
				Organization issuer = seeyonEdoc.getIssuer();
				if (issuer != null) {
					String name = issuer.getName();
				}
				
				
				/**
				 * detailIds中包含了交换过来的所有单位的myDetail，中筛选出属于本单位的myDetail值
				 * detailIds的key为OCIP平台单位的ID，
				 * value为myDetail
				 */
				//String localSystemCode = OcipKeyMananger.getSysCode();//本系统注册id值
				//将本地单位的单位ID转换为OCIP平台对应的单位ID
				//OcipOrgUnit account = organizationManager.getAccount(OCIPServicesServlet.sendOrgLocalId , localSystemCode);//OCIP单位实体
				//String unitId = account.getId();
				
				for (Organization recOrganization : reciList) {
					Address address = recOrganization.getIdentification();
					String redId = address.getId();
					String type = address.getType();
					if (IConstant.AddressType.account.name().equals(type)) {
						OrgUnit localAccount = organizationManager.getLocalAccount(String.valueOf(redId));
						if (localAccount != null) {
							//String localAccountId = localAccount.getId();
							//获取到本地单位的单位id
							String localAccountId = localAccount.getObjectId();
							/**
							 * 筛选出属于本系统的单位数据
							 * 注意：在第三方系统接入时，应该是拿到localAccountId在第三方系统的数据库查询改单位是否属于本系统
							 */
							if (OCIPServicesServlet.sendOrgLocalId.equals(localAccountId)) {// 在detailIds中筛选出属于本单位的myDetail值
								Long myDetail = detailIds.get(redId);// 该myDetail需要保存下来，在签收、回退公文时会用到
								//Long myDetail = entry.getValue();// 该myDetail需要保存下来，在签收、回退公文时会用到
								myDetailMap.put(Long.valueOf(localAccountId), myDetail);
							}
						}
					}else if(IConstant.AddressType.department.name().equals(type)){
						OrgDepartment localDepartment = organizationManager.getLocalDepartment(String.valueOf(redId));
						if (localDepartment != null) {
							//获取到本地单位的单位id
							String localDepId = localDepartment.getObjectId();
							/**
							 * 筛选出属于本系统的单位数据
							 * 注意：在第三方系统接入时，应该是拿到localAccountId在第三方系统的数据库查询改单位是否属于本系统
							 */
							if (OCIPServicesServlet.sendOrgLocalId.equals(localDepId)) {// 在detailIds中筛选出属于本单位的myDetail值
								Long myDetail = detailIds.get(redId);// 该myDetail需要保存下来，在签收、回退公文时会用到
								//Long myDetail = entry.getValue();// 该myDetail需要保存下来，在签收、回退公文时会用到
								myDetailMap.put(Long.valueOf(localDepId), myDetail);
							}
						}
					}
				}
				/*if (detailIds != null && !detailIds.isEmpty()) {
					for (Entry<Long, Long> entry : detailIds.entrySet()) {
						Long key = entry.getKey();// OCIP的单位id
						//将OCIP的单位ID转换为本地对应的单位ID
						OrgUnit localAccount = organizationManager.getLocalAccount(String.valueOf(key));
						if (localAccount != null) {
							//String localAccountId = localAccount.getId();
							//获取到本地单位的单位id
							String localAccountId = localAccount.getObjectId();
							*//**
							 * 筛选出属于本系统的单位数据
							 * 注意：在第三方系统接入时，应该是拿到localAccountId在第三方系统的数据库查询改单位是否属于本系统
							 *//*
							if (OCIPServicesServlet.sendOrgLocalId.equals(localAccountId)) {// 在detailIds中筛选出属于本单位的myDetail值
								Long myDetail = entry.getValue();// 改myDetail需要保存下来，在签收、回退公文时会用到
								myDetailMap.put(Long.valueOf(localAccountId), myDetail);
							}
						}
					}
				}*/
				
				//附件
				List<AttachmentFile> atts = bussnissMessage.getAttachments();
				for (AttachmentFile attachmentFile : atts) {
					//byte[] data = attachmentFile.getData();//数据
					//数据
					DataHandler dataHandler = attachmentFile.getDataHandler();
					if (dataHandler != null) {
						try {
							String path = this.getClass().getClassLoader().getResource("").getPath() + File.separator + "file";
							String srcPath = path + File.separator + UUID.randomUUID();
							System.out.println("附件路径：" + srcPath);
							File file = new File(srcPath);
							//FileOutputStream outputStream = new FileOutputStream(file);
							//BufferedOutputStream stream = new BufferedOutputStream(outputStream);
							OutputStream stream = FileUtils.openOutputStream(file);
							dataHandler.writeTo(stream);
							stream.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					String name = attachmentFile.getName();//名称
					Long size = attachmentFile.getSize();//大小
				}
				
				
				/**
				 * 正文格式为PDF，contentType="Pdf"
				 * 正文格式为Officeword，contentType="OfficeWord"
				 * 正文格式为WpsWord,contentType="WpsWord"
				 * 正文格式为ofd,contentType="Ofd"
				 * 
				 * 正文格式为Officeword： mimeType=application/msword
				 * 正文格式为PDF：mimeType=application/pdf
				 * 正文格式为WpsWord,mimeType=msoffice
				 * 正文格式为ofd： mimeType=application/ofd
				 */
				//正文			
				DataHandler dataHandler = ofcEdocObject.getContentDataHandler();
				if (dataHandler != null) {
					try {
						String path = this.getClass().getClassLoader().getResource("").getPath() + File.separator + "file";
						String srcPath = path + File.separator + UUID.randomUUID();
						System.out.println("正文路径：" + srcPath);
						File file = new File(srcPath);
						OutputStream output = FileUtils.openOutputStream(file);
						dataHandler.writeTo(output);
						output.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//byte[] content = ofcEdocObject.getContent();//正文数据
				String contentTypeStr = ofcEdocObject.getContentType();//正文格式
				String contentMimeType = ofcEdocObject.getContentMimeType();//正文格式
				
				for (Entry<Long, Long> entry : myDetailMap.entrySet()) {
					Long accountId = entry.getKey();
					Long myDetail = entry.getValue();
					System.out.println("单位id为：" + accountId + "的单位接收到公文:" + subject + " 公文ID为:" + groupId + " myDetail:" + myDetail + " exchNo:" + exchNo);
				}
				
				//
			}
		} catch (NumberFormatException e) {
			//TODO 记录日志
			e.printStackTrace();
			for (BussinessResult bussinessResult : bussinessResults) {
				bussinessResult.setCode("2");
				bussinessResult.setMessage("公文交换接收方接收数据处理失败！");
			}
		} finally {
			if (myDetailMap != null && !myDetailMap.isEmpty() ) {
				//第三方系统收到OCIP发送来的公文后需要给OCIP发送消息，告诉OCIP第三方系统已经收到的数据
				for (Entry<Long, Long> entry : myDetailMap.entrySet()) {
					Long value = entry.getValue();
					Long accountId = entry.getKey();
					receiptEdoc(mainId, value, "0", exchNo, groupId , unitLocalId , accountId);
				}
			}

		}
		//String jsonString = JSONObject.toJSONString(in, SerializerFeature.WriteMapNullValue);
		//System.out.println("jsonString:" + jsonString);
		return bussinessResults;
	}
	
	/**
	 * 异构系统接收到公文后，需要给OCIP发送一个回执信息，告知OCIP异构系统收到的数据
	 * @param mainId
	 * @param detailId
	 * @param exchangeStatus
	 */
	private void receiptEdoc(long mainId, long detailId,String exchangeStatus ,String relationId , String groupId , String unitLocalId , long sendUnitId) {
		try {
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("mainId", mainId);
			param.put("sendUnitId", String.valueOf(sendUnitId));
			param.put("detailId", detailId);
			param.put("exchangeStatus", exchangeStatus);
			param.put("relationId", relationId);
			param.put("groupId", groupId);
			param.put("recUnitId", unitLocalId);//回执信息接收单位本地ID
			param.put("edocOperation",  EdocOperation.RECEIVED);
			getBussinessService().fireExchange(BIZContentType.RET, param);
		} catch (Exception e) {
			LogUtils.error(OCIPServicesServlet.class, "发送回执消息异常", e);
		}
		
	}
	
	private IBussinessService getBussinessService() {
		return OcipConfiguration.getInstance().getExchangeSpi().getBussinessService();
	}

	/**
	 * 发送数据
	 */
	@Override
	public BIZExchangeData exchangeSend(Map<String, Object> map) throws BussinessException {
		//封装公文数据包
		BIZExchangeData bizData = new BIZExchangeData();
		//String accountID = "-151236088483985161";
		//OrgUnit localAccount = organizationManager.getLocalAccount(accountID);
		bizData.setIdentifier(UUID.randomUUID().toString().replaceAll("-", ""));
		
		//公文发送方信息
		Organization sender = new Organization();
		Address createUser = new Address();
		String localSystemCode = OcipKeyMananger.getSysCode();//本系统注册id值
		createUser.setResource(localSystemCode);
		//String userID = "138913789043295577";
		String userID = OCIPServicesServlet.sendMemLocalId;
		//userID = "5725175934914479525";
		createUser.setId(userID);//发文人员本地ID
		createUser.setType(IConstant.AddressType.member.name());
		//公文发送人员名称
		String userName = OCIPServicesServlet.sendMemName;
		createUser.setName(userName);
		//String userPlatID = "-8467766985147790299";
		//TODO 将本地的人员ID转换为OCIP平台的人员ID
		String userPlatID = organizationManager.getPlatformId(createUser);
		createUser.setId(userPlatID);
		createUser.setResource("0");
		sender.setIdentification(createUser);
		sender.setName(createUser.getName());
		bizData.setSender(sender);
		
		HashMap<Long, Long> orgIdAndDetailId = new HashMap<Long, Long>();
		
		//TODO 需要传递公文的ID
		//公文id
		int intFlag = (int)(Math.random() * 1000000);
		String colSummaryId = String.valueOf(intFlag);
		String groupId = colSummaryId;
		bizData.setGroupId(groupId);//groupId的值设置为公文ID
		
		//公文接收者，接收者可以有多个,DEMO中只封装了一个接收者
		List<Organization> recivers = new ArrayList<Organization>();
		Organization reciverOrg = new Organization();
		//接收单位信息
		//接收单位本地ID
		String recOrgID = OCIPServicesServlet.recOrgLocalId;
		//接收单位名称
		String recOrgName = OCIPServicesServlet.recOrgName;
		Address recAdd = new Address();
		recAdd.setResource("0");
		recAdd.setName(recOrgName);
		//IConstant.AddressType.department;
		//IConstant.AddressType.account
		recAdd.setType(IConstant.AddressType.account.name());//接受者类型设置为account
		//接收单位的ID
		//将接收单位的本地ID转换为OCIP对应的单位ID
		//OcipOrgUnit account = organizationManager.getAccount(recOrgID);
		//organizationManager.getDepartment(arg0, arg1)
		OcipOrgUnit account = organizationManager.getAccount(recOrgID);//OCIP单位实体
		String accountId = account.getId();
		recOrgName = account.getName();
		recAdd.setId(accountId);
		reciverOrg.setIdentification(recAdd);
		//orgIdAndDetailId必须设置，格式如下，key为接受者的id，value为detailId,注意，当接受单位有多个时，detailId不能重复
		/**
		 * detailId说明：
		 * 比如给3家单位发送了公文，A表存储公文，B表存储发送记录，detailId1的值就应该传B的id
		 */
		int detailId = (int)(Math.random() * 1000000);
		orgIdAndDetailId.put(Long.valueOf(recAdd.getId()), Long.valueOf(detailId));
		reciverOrg.setName(recOrgName);
		
		Organization reciverOrg1 = new Organization();
		//接收单位信息
		//接收单位本地ID
		String recOrgID1 = OCIPServicesServlet.recOrgLocalId1;
		//接收单位名称
		String recOrgName1 = OCIPServicesServlet.recOrgName1;
		Address recAdd1 = new Address();
		recAdd1.setResource("0");
		recAdd1.setName(recOrgName1);
		recAdd1.setType("account");//接受者类型设置为account
		//接收单位的ID
		//将接收单位的本地ID转换为OCIP对应的单位ID
		OcipOrgUnit account1 = organizationManager.getAccount(recOrgID1);//OCIP单位实体
		String accountId1 = account1.getId();
		recOrgName1 = account1.getName();
		recAdd1.setId(accountId1);
		reciverOrg1.setIdentification(recAdd1);
		//orgIdAndDetailId必须设置，格式如下，key为接受者的id，value为detailId,注意，当接受单位有多个时，detailId不能重复
		/**
		 * detailId1说明：
		 * 比如给3家单位发送了公文，A表存储公文，B表存储已发送的记录，detailId1的值就应该传B的id
		 */
		int detailId1 = (int)(Math.random() * 1000000);
		orgIdAndDetailId.put(Long.valueOf(recAdd1.getId()), Long.valueOf(detailId1));
		reciverOrg1.setName(recOrgName1);
		
		
		//接收者可以有多个，按照上面的格式封装
		recivers.add(reciverOrg);
		//recivers.add(reciverOrg1);
		
		OFCEdocObject object = new OFCEdocObject();
		SeeyonEdoc seeyonEdoc = new SeeyonEdoc();
		BIZMessage bizMessage = new BIZMessage();
		seeyonEdoc.setDetailIds(orgIdAndDetailId);
		
		//发送者所在单位OCIP平台id
		//发文单位本地单位id
		//String unitID = "2556842553182670622";
		String unitID = OCIPServicesServlet.sendOrgLocalId;
		//将发文者所在本地单位id转换为OCIP平台对应的单位ID
		OcipOrgUnit unit = organizationManager.getAccount(unitID , localSystemCode);
		//发文者所在单位对应的平台id
		String ocipOrgUnitId = unit.getId().toString();
		//String ocipOrgUnitId = "2723437946474089882";
		object.setIssueOrganization(ocipOrgUnitId);
		
		seeyonEdoc.setDocumentIdentifier(colSummaryId);
		//mainID设置为公文ID
		seeyonEdoc.setMainId(colSummaryId);
		//初始化设置form表单中的值
		String title = (String) map.get("title");
		System.out.println("公文标题:" + title + " ID:" + colSummaryId);
		initFormData(object, seeyonEdoc, title, colSummaryId);
		//设置公文正文
		initGovdocContentAll(object);
		//设置附件
		initGovdocAttachment(bizMessage);
		
		bizData.setRecivers(recivers);
		
		//设置公文标题 colSummary.getSubject()
		String subject = title;
		bizData.setSubject(subject);
		
		bizMessage.setContentType(BIZContentType.OFC);
		object.setTitle(subject);
		object.setExtendAttr(seeyonEdoc);
		bizMessage.setContent(object);
		
		//发送源地址信息
		Address source = new Address();
		source.setId(localSystemCode);
		source.setName(localSystemCode);
		bizData.setSource(source);
		bizData.setBussnissMessage(bizMessage);
		
		String jsonString = JSONObject.toJSONString(bizData, SerializerFeature.WriteMapNullValue);
		//System.out.println("jsonString:" + jsonString);
		//LogUtils.info(EdocOFCExchangeHandler.class, "jsonString:" + jsonString);
		return bizData;
	}
	

	//初始化设置附件
	private void initGovdocAttachment(BIZMessage bizMessage) {
		String path = this.getClass().getClassLoader().getResource("").getPath();
		// 可以设置多个附件
		List<AttachmentFile> attfiles = new ArrayList<AttachmentFile>();
		AttachmentFile att = new AttachmentFile();
		File file = new File(path + "file" + File.separator + "file1.docx");
		int intFlag = (int)(Math.random() * 1000000);
		att.setIdentification(String.valueOf(intFlag));
		att.setName(file.getName());
		att.setSize(file.length());
		try {
			byte[] bs = FileUtils.readFileToByteArray(file);
			//att.setDataHandler(new DataHandler(new FileDataSource(file)));
			att.setData(bs);
			attfiles.add(att);
			bizMessage.setAttachments(attfiles);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	//初始化设置公文正文
	private void initGovdocContentAll(OFCEdocObject object) {
		String path = this.getClass().getClassLoader().getResource("").getPath();
		File file = new File(path + "file" + File.separator + "help.pdf");
		// OfficeWord或者Pdf，正文格式为PDF，contentType="Pdf"，
		//正文格式为word，contentType="OfficeWord",
		//正文格式为WpsWord,contentType="WpsWord"
		// 正文格式为ofd：contentType="Ofd"
		String contentType = "Pdf";
		object.setContentType(contentType);
		//正文格式为OfficeWord： mimeType=application/msword, 
		//正文格式为PDF：mimeType=application/pdf,
		//正文格式为WpsWord,mimeType=msoffice
		//正文格式为ofd： mimeType=application/ofd
		String mimeType = "application/pdf";
		object.setContentMimeType(mimeType);
		byte[] bs;
		try {
			bs = FileUtils.readFileToByteArray(file);
			//object.setContentDataHandler(new DataHandler(new FileDataSource(file)));
			object.setContentDataHandler(new DataHandler(new FileDataSource(file)));
			//object.setContent(bs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//初始化设置form表单中的值
	private void initFormData(OFCEdocObject object, SeeyonEdoc seeyonEdoc, String title, String summaryId) {
		// 紧急程度
		String urgentLevel = "普通";
		seeyonEdoc.setUrgentLevel(urgentLevel);
		// 标题
		seeyonEdoc.setSubject(title);
		// 发文单位名称
		seeyonEdoc.setSendUnit(OCIPServicesServlet.sendOrgName);
		// 送往单位名称
		seeyonEdoc.setSendTo(OCIPServicesServlet.recOrgName);
		// 密级
		seeyonEdoc.setSecretLevel("普通");
		seeyonEdoc.setDocumentIdentifier(summaryId);
		PropertyValue docMark = new PropertyValue();
		docMark.setDisplay("222");
		docMark.setValue("222");
		docMark.setInnerValue("222");
		// 公文文号
		seeyonEdoc.setDocMark(docMark);
		

	}

	@Override
	public BIZContentType type() {
		return BIZContentType.OFC;
	}

	public IOrganizationManager getOrganizationManager() {
		return organizationManager;
	}

	public void setOrganizationManager(IOrganizationManager organizationManager) {
		this.organizationManager = organizationManager;
	}

	public static Map<String, String> getRecUnitMap() {
		return recUnitMap;
	}

}
