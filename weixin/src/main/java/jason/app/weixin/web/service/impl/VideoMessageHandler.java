package jason.app.weixin.web.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import jason.app.weixin.common.constant.MediaType;
import jason.app.weixin.common.model.CreateUserCommand;
import jason.app.weixin.common.model.SaveMediaCommand;
import jason.app.weixin.common.service.IAmazonS3Service;
import jason.app.weixin.common.service.IWeixinService;
import jason.app.weixin.social.model.SocialUser;
import jason.app.weixin.social.service.ISocialService;
import jason.app.weixin.web.controller.weixin.model.WeixinHeader;
import jason.app.weixin.web.controller.weixin.model.WeixinParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class VideoMessageHandler extends MessageHandler {
	
	private static Logger logger = LoggerFactory.getLogger(SubscribeEventHandler.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Override
	public boolean canHandle(WeixinParam params, WeixinHeader header) {
		// TODO Auto-generated method stub
		return super.canHandle(params, header) && ("shortvideo".equalsIgnoreCase(params.getMsgType()));
	}
	
	@Override
	public WeixinParam handle(final WeixinParam params, WeixinHeader header) {
		final SaveMediaCommand command = new SaveMediaCommand(params.getMediaId(),params.getThumbMediaId(),params.getFromUserName());
		command.setMediaType(MediaType.VIDEO);
		jmsTemplate.send(new MessageCreator() {
	            public Message createMessage(Session session) throws JMSException {
	              //  return session.createTextMessage("hello queue world");  
	            	logger.info("save video from "+params.getFromUserName());
	            	return session.createObjectMessage(command);
	              }
	        });
        WeixinParam response = new WeixinParam();
        response.setMsgType("text");
        response.setFromUserName(params.getToUserName());
        response.setCreateTime(params.getCreateTime());
        response.setToUserName(params.getFromUserName());       
        response.setContent("图片已保存，发消息可以点击图片按钮，在列表中选择插入图片");
        return response;
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return super.getOrder()+98;
	}
}
