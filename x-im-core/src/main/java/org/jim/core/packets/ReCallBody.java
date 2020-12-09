/**
 *
 */
package org.jim.core.packets;

import com.alibaba.fastjson.JSONObject;

/**
 * 版本: [1.0]
 * 功能说明:
 *
 */
public class ReCallBody extends Message {

    /**
     * 发送用户id;
     */
    private String from;
    /**
     * 目标用户id;
     */
    private String to;
    /**
     * 聊天类型;(如公聊、私聊)
     */
    private Integer chatType;
    /**
     * 消息发到哪个群组;
     */
    private String groupId;

    private ReCallBody()
    {
    }

    private ReCallBody(String id, String from, String to, Integer chatType, String groupId, Integer cmd, Long createTime, JSONObject extras)
    {
        this.id = id;
        this.from = from;
        this.to = to;
        this.chatType = chatType;
        this.groupId = groupId;
        this.cmd = cmd;
        this.createTime = createTime;
        this.extras = extras;
    }

    public static ReCallBody.Builder newBuilder()
    {
        return new ReCallBody.Builder();
    }

    public String getFrom()
    {
        return from;
    }

    public ReCallBody setFrom(String from)
    {
        this.from = from;
        return this;
    }

    public String getTo()
    {
        return to;
    }

    public ReCallBody setTo(String to)
    {
        this.to = to;
        return this;
    }


    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public Integer getChatType()
    {
        return chatType;
    }

    public ReCallBody setChatType(Integer chatType)
    {
        this.chatType = chatType;
        return this;
    }

    public static class Builder extends Message.Builder<ReCallBody, ReCallBody.Builder> {
        /**
         * 来自user_id;
         */
        private String from;
        /**
         * 目标user_id;
         */
        private String to;
        /**
         * 聊天类型;(如公聊、私聊)
         */
        private Integer chatType;
        /**
         * 消息发到哪个群组;
         */
        private String groupId;

        public Builder()
        {
        }

        ;

        public Builder from(String from)
        {
            this.from = from;
            return this;
        }

        public Builder to(String to)
        {
            this.to = to;
            return this;
        }

        public Builder chatType(Integer chatType)
        {
            this.chatType = chatType;
            return this;
        }

        public Builder groupId(String groupId)
        {
            this.groupId = groupId;
            return this;
        }

        @Override
        protected Builder getThis()
        {
            return this;
        }

        @Override
        public ReCallBody build()
        {
            return new ReCallBody(this.id, this.from, this.to, this.chatType, this.groupId, this.cmd, this.createTime, this.extras);
        }
    }
}
