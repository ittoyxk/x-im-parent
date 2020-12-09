package org.jim.server.command.handler;

import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.ImStatus;
import org.jim.core.config.ImConfig;
import org.jim.core.exception.ImException;
import org.jim.core.packets.*;
import org.jim.core.utils.JsonKit;
import org.jim.server.ImServerChannelContext;
import org.jim.server.JimServerAPI;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.config.ImServerConfig;
import org.jim.server.protocol.ProtocolManager;
import org.jim.server.queue.MsgQueueRunnable;
import org.jim.server.util.ChatKit;

import java.util.Objects;

/**
 * 版本: [1.0]
 * 功能说明: 撤回请求cmd消息命令处理器
 * @author : xiaokang
 */
public class ReCallReqHandler extends AbstractCmdHandler {

	@Override
	public ImPacket handler(ImPacket packet, ImChannelContext channelContext) throws ImException {
		ImServerChannelContext imServerChannelContext = (ImServerChannelContext)channelContext;
		if (packet.getBody() == null) {
			throw new ImException("body is null");
		}
		ReCallBody chatBody = JsonKit.toBean(packet.getBody(),ReCallBody.class);
		packet.setBody(chatBody.toByte());
		//聊天数据格式不正确
		if(chatBody == null || ChatType.forNumber(chatBody.getChatType()) == null){
			RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CANCEL_MSG_RESP, ImStatus.C10002);
			ImPacket respChatPacket = ProtocolManager.Converter.respPacket(chatDataInCorrectRespPacket, channelContext);
			respChatPacket.setStatus(ImStatus.C10002);
			return respChatPacket;
		}
		//异步调用业务处理消息接口
		MsgQueueRunnable msgQueueRunnable = getMsgQueueRunnable(imServerChannelContext);
		msgQueueRunnable.addMsg(chatBody);
		msgQueueRunnable.executor.execute(msgQueueRunnable);
		ImPacket chatPacket = new ImPacket(Command.COMMAND_CANCEL_MSG_REQ,new RespBody(Command.COMMAND_CANCEL_MSG_REQ,chatBody).toByte());
		//设置同步序列号;
		chatPacket.setSynSeq(packet.getSynSeq());
		ImServerConfig imServerConfig = ImConfig.Global.get();
		boolean isStore = ImServerConfig.ON.equals(imServerConfig.getIsStore());
		//私聊
		if(ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatBody.getChatType()){
			String toId = chatBody.getTo();
			if(ChatKit.isOnline(toId, isStore)){
				JimServerAPI.sendToUser(toId, chatPacket);
				//发送成功响应包

				RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CANCEL_MSG_RESP, ImStatus.C10000);
				ImPacket respPacket = ProtocolManager.Converter.respPacket(chatDataInCorrectRespPacket, channelContext);
				respPacket.setStatus(ImStatus.C10000);
				return respPacket;
			}else{
				//用户不在线响应包
				RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CANCEL_MSG_RESP,ImStatus.C10001);
				ImPacket respPacket = ProtocolManager.Converter.respPacket(chatDataInCorrectRespPacket, channelContext);
				respPacket.setStatus(ImStatus.C10001);
				return respPacket;
			}
		//群聊
		}else if(ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatBody.getChatType()){
			String groupId = chatBody.getGroupId();
			JimServerAPI.sendToGroup(groupId, chatPacket);
			//发送成功响应包

			RespBody chatDataInCorrectRespPacket = new RespBody(Command.COMMAND_CANCEL_MSG_RESP, ImStatus.C10000);
			ImPacket respPacket = ProtocolManager.Converter.respPacket(chatDataInCorrectRespPacket, channelContext);
			respPacket.setStatus(ImStatus.C10000);
			return respPacket;
		}
		return null;
	}

	@Override
	public Command command() {
		return Command.COMMAND_CANCEL_MSG_REQ;
	}

	/**
	 * 获取聊天业务处理异步消息队列
	 * @param imServerChannelContext IM通道上下文
	 * @return
	 */
	private MsgQueueRunnable getMsgQueueRunnable(ImServerChannelContext imServerChannelContext){
		MsgQueueRunnable msgQueueRunnable = (MsgQueueRunnable)imServerChannelContext.getMsgQue();
		if(Objects.nonNull(msgQueueRunnable.getProtocolCmdProcessor())){
			return msgQueueRunnable;
		}
		synchronized (MsgQueueRunnable.class){
			msgQueueRunnable.setProtocolCmdProcessor(this.getSingleProcessor());
		}
		return msgQueueRunnable;
	}

}
