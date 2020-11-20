package org.jim.client;

import org.jim.client.config.ImClientConfig;
import org.jim.core.packets.ChatBody;
import org.jim.core.packets.ChatType;
import org.jim.core.packets.Command;
import org.jim.core.packets.LoginReqBody;
import org.jim.core.tcp.TcpPacket;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientTioConfig;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.Tio;
import org.tio.core.ssl.SslConfig;
import org.tio.utils.lock.SetWithLock;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 版本: [1.0]
 * 功能说明:
 * 作者: WChao 创建时间: 2017年8月30日 下午1:05:17
 */
public class HelloClientStarter {

    /**
     * 启动程序入口
     */
    public static void main(String[] args) throws Exception
    {
        //服务器节点
        Node serverNode = new Node("im.commchina.net", 7070);
        //构建客户端配置信息
        ImClientConfig imClientConfig = ImClientConfig.newBuilder()
                //客户端业务回调器,不可以为NULL
                .clientHandler(new HelloImClientHandler())
                //客户端事件监听器，可以为null，但建议自己实现该接口
                .clientListener(new HelloImClientListener())
                //心跳时长不设置，就不发送心跳包
                .heartbeatTimeout(60000)
                //断链后自动连接的，不想自动连接请设为null
//				.reConnConf(new ReconnConf(5000L))
                .sslConfig(SslConfig.forClient())
                .build();
        //生成客户端对象;
        JimClient jimClient = new JimClient(imClientConfig);
        //连接服务端
        jimClient.connect(serverNode);

        //获取连接
        Optional<ChannelContext> channelContext = getChannelContexts(imClientConfig);

        if (channelContext.isPresent()) {

            ChannelContext channel = channelContext.get();
            //先认证
            login(channel);

            //发送测试消息
            send(channel);
        }
    }

    /**
     * 认证
     *
     * @param channelContext
     */
    private static void login(ChannelContext channelContext)
    {
        byte[] loginBody = new LoginReqBody("Bearer e992f1fe-48ca-461e-b119-b1fac31acb81").toByte();
        TcpPacket loginPacket = new TcpPacket(Command.COMMAND_LOGIN_REQ, loginBody);
        Tio.send(channelContext, loginPacket);
    }

    /**
     * 发送消息
     *
     * @param channelContext
     * @throws Exception
     */
    private static void send(ChannelContext channelContext) throws Exception
    {
        ChatBody chatBody = ChatBody.newBuilder()
                .from("hello_client")
                .to("admin")
                .msgType(0)
                .chatType(ChatType.CHAT_TYPE_PUBLIC.getNumber())
                .groupId("100")
                .content("Socket普通客户端消息测试!").build();
        TcpPacket chatPacket = new TcpPacket(Command.COMMAND_CHAT_REQ, chatBody.toByte());
        Tio.send(channelContext, chatPacket);
    }

    /**
     * 获取原始连接上下文
     *
     * @param imClientConfig
     * @return
     */
    private static Optional<ChannelContext> getChannelContexts(ImClientConfig imClientConfig)
    {
        ClientTioConfig clientTioConfig = (ClientTioConfig) imClientConfig.getTioConfig();
        SetWithLock<ChannelContext> setWithLock = clientTioConfig.connecteds;
        ReentrantReadWriteLock.ReadLock readLock = setWithLock.readLock();
        readLock.lock();
        Set<ChannelContext> set = setWithLock.getObj();

        Optional<ChannelContext> context = set.stream().filter(channelContext -> !(channelContext.isClosed || channelContext.isRemoved)).findFirst();
        return context;
    }
}
